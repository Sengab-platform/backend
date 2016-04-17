package DBUtilities;

import com.couchbase.client.core.BackpressureException;
import com.couchbase.client.core.BucketClosedException;
import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.core.time.Delay;
import com.couchbase.client.deps.io.netty.handler.timeout.TimeoutException;
import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.CASMismatchException;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.error.TemporaryFailureException;
import com.couchbase.client.java.query.AsyncN1qlQueryResult;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.dsl.Expression;
import com.couchbase.client.java.util.retry.RetryBuilder;
import play.Logger;
import rx.Observable;

import java.util.concurrent.TimeUnit;

import static com.couchbase.client.java.query.Insert.insertInto;
import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.Update.update;
import static com.couchbase.client.java.query.dsl.functions.ArrayFunctions.arrayAppend;
import static com.couchbase.client.java.query.dsl.functions.ArrayFunctions.arrayContains;


/**
 * Created by rashwan on 3/29/16.
 */
public class Contribution {
    private static AsyncBucket mBucket;

    /**
     * Create and save a user's contribution of a project. can error with {@link CouchbaseException} and {@link BucketClosedException}.
     * @param projectId The ID of the project that the user contributed to.
     * @param userId The ID of the contributing user.
     * @param contributionJsonObject The Json object containing the contribution content.
     * @return An observable of Json object containing the contribution id and the added contribution object.
     */
    public static Observable<JsonObject> createContribution(String projectId,String userId,JsonObject contributionJsonObject) {
        try {
            checkDBStatus ();
        } catch (BucketClosedException e) {
            return Observable.error (e);
        }

        String contributionId = "contribution::" + DBConfig.stripIdFromPrefix (projectId) + "::" + DBConfig.stripIdFromPrefix (userId);

        Logger.info (String.format ("DB: Adding contribution with ID: %s", contributionId));

        return mBucket.query (N1qlQuery.simple (select (Expression.x ("meta(aUser).id")).from (Expression.x (DBConfig.BUCKET_NAME + " aUser"))
            .useKeys (Expression.s (userId))
            .where (arrayContains (Expression.x ("enrolled_projects"), Expression.s (projectId)))))
            .flatMap (result -> result.rows ().isEmpty ())
            .flatMap (isEmpty -> {
                if (isEmpty) {
                    Logger.info (String.format ("DB: User with ID: %s is not enrolled in project with ID: %s", userId, projectId));

                    return Observable.just (JsonObject.create ().put ("id", DBConfig.NOT_ENROLLED));
                } else {

                    return mBucket.exists (contributionId).flatMap (exist -> {
                        if (!exist) {
                            Logger.info (String.format ("Contribution document with ID : %s dosen't exist, Creating a new document and adding the contribution to it.", contributionId));
                            JsonObject contributions = JsonObject.create ().put ("contributions", JsonArray.create ().add (contributionJsonObject));
                            return mBucket.query (N1qlQuery.simple (insertInto (DBConfig.BUCKET_NAME)
                                    .values (contributionId, contributions)
                                    .returning ("contributions[-1] as contribution," + Expression.s (contributionId).as ("id"))));
                        } else {
                            Logger.info (String.format ("Contribution document with ID : %s already exists, Appending the contribution to the contributions array.", contributionId));
                            return mBucket.query (N1qlQuery.simple (update (Expression.x (DBConfig.BUCKET_NAME + " contribution"))
                                    .useKeys (Expression.s (contributionId)).set (Expression.x ("contributions"),
                                            arrayAppend (Expression.x ("contributions"), Expression.x (contributionJsonObject)))
                                    .returning (Expression.x ("contributions[-1] as contribution,meta(contribution).id"))));
                        }
                    })
                            .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
                            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
                            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                                    .delay (Delay.fixed (500, TimeUnit.MILLISECONDS)).once ().build ())
                            .onErrorResumeNext (throwable -> {
                                Logger.info (String.format ("DB: Failed to add contribution with id: %s and contents: %s", contributionId, contributionJsonObject.toString ()));
                                return Observable.error (new CouchbaseException (String.format ("DB: Failed to add contribution with id: %s and contents: %s, General DB exception.", contributionId, contributionJsonObject.toString ())));
                            }).defaultIfEmpty(JsonObject.create().put("id", DBConfig.EMPTY_JSON_OBJECT));

                }
            });
    }

    /**
     * Get a contribution of a project using its id. can error with {@link CouchbaseException} and {@link BucketClosedException}.
     * @param contributionId the id of the contribution to get.
     * @return an observable of the json document if it was found , if it wasn't found it returns an empty json document with id DBConfig.EMPTY_JSON_OBJECT .
     */
    public static Observable<JsonObject> getContributionWithId(String contributionId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        return mBucket.get (contributionId).timeout (500,TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                return Observable.error (new CouchbaseException ("Failed to get contribution, General DB exception"));
            })
                .defaultIfEmpty(JsonDocument.create(DBConfig.EMPTY_JSON_OBJECT))
            .flatMap (jsonDocument -> Observable.just (jsonDocument.content ().put ("id",jsonDocument.id ())));
    }

    /**
     * Update a contribution of a project. can error with {@link CouchbaseException},{@link DocumentDoesNotExistException},{@link CASMismatchException} and {@link BucketClosedException} .
     * @param contributionId The id of the contribution to be updated .
     * @param contributionJsonObject The updated Json object to be used as the value of updated document.
     * @return an observable of the updated Json document .
     */
    private static Observable<JsonDocument> updateContribution(String contributionId,JsonObject contributionJsonObject){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        JsonDocument contributionDocument = JsonDocument.create (contributionId,DBConfig.removeIdFromJson (contributionJsonObject));

        return mBucket.replace (contributionDocument).timeout (500,TimeUnit.MILLISECONDS)
                .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                        .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
                .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                        .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
                .onErrorResumeNext (throwable -> {
                    if (throwable instanceof DocumentDoesNotExistException){
                        return Observable.error (new DocumentDoesNotExistException ("Failed to update contribution, ID dosen't exist in DB"));

                    }else if (throwable instanceof CASMismatchException){
                        //// TODO: 3/28/16 needs more accurate handling in the future.
                        return Observable.error (new CASMismatchException ("Failed to update contribution, CAS value is changed"));
                    }
                    else {
                        return Observable.error (new CouchbaseException ("Failed to update contribution, General DB exception "));
                    }
                });
    }


    /**
     * Delete a contribution for a user of a project using its id. can error with {@link CouchbaseException}, {@link DocumentDoesNotExistException} and {@link BucketClosedException} .
     * @param contributionId The id of the contribution document to be deleted.
     * @return An observable with Json document containing only the id .
     */
    public static Observable<JsonDocument> deleteContribution(String contributionId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        return mBucket.remove (contributionId).timeout (500, TimeUnit.MILLISECONDS)
                .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                        .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
                .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                        .delay (Delay.fixed (500, TimeUnit.MILLISECONDS)).once ().build ())
                .onErrorResumeNext (throwable -> {
                    if (throwable instanceof DocumentDoesNotExistException) {
                        return Observable.error (new DocumentDoesNotExistException ("Failed to delete contribution, ID dosen't exist in DB"));
                    } else {
                        return Observable.error (new CouchbaseException ("Failed to delete contribution, General DB exception "));
                    }
                });
    }

    private static void checkDBStatus () {
        if (DBConfig.bucket.isClosed ()){
            if (DBConfig.initDB() == DBConfig.OPEN_BUCKET_OK) {
                mBucket = DBConfig.bucket;
            }else{
                throw new BucketClosedException ("Failed to open bucket due to timeout or backpressure");
            }
        }else {
            mBucket = DBConfig.bucket;
        }
    }

}
