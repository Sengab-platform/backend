package DBUtilities;

import com.couchbase.client.core.BackpressureException;
import com.couchbase.client.core.BucketClosedException;
import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.core.time.Delay;
import com.couchbase.client.deps.io.netty.handler.timeout.TimeoutException;
import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.CASMismatchException;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.error.TemporaryFailureException;
import com.couchbase.client.java.query.AsyncN1qlQueryResult;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.dsl.Expression;
import com.couchbase.client.java.util.retry.RetryBuilder;
import play.Logger;
import rx.Observable;

import java.util.concurrent.TimeUnit;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.Update.update;
import static com.couchbase.client.java.query.dsl.functions.ArrayFunctions.*;

/**
 * Created by rashwan on 3/29/16.
 */
public class Result {
    private static AsyncBucket mBucket;

    /**
     * Create and save a project's results. can error with {@link CouchbaseException},{@link DocumentAlreadyExistsException} and {@link BucketClosedException}.
     * @param resultId The Json object to be the value of the document , it also has an Id field to use as the document key.
     * @return an observable of the created Json document.
     */
    public static Observable<JsonObject> createResult(String resultId,JsonObject resultObject){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }
        Logger.info (String.format ("DB: Adding a result document with ID: %s ,to the DB ",resultId));
        JsonDocument resultDocument = JsonDocument.create (resultId,resultObject);

        return mBucket.insert (resultDocument).single ().timeout (500, TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentAlreadyExistsException) {
                    Logger.info (String.format ("DB: Failed to add a result document with ID: %s ,to the DB ",resultId));

                    return Observable.error (new DocumentAlreadyExistsException (String.format ("Failed to create result document with ID: %s , ID already exists",resultId)));
                } else {
                    return Observable.error (new CouchbaseException (String.format ("Failed to create result document with ID: %s , General DB exception ",resultId)));
                }
            }).flatMap (jsonDocument -> Observable.just (jsonDocument.content ().put ("id",jsonDocument.id ())));
    }

    /**
     * Get results for a project using its id. can error with {@link CouchbaseException} and {@link BucketClosedException}.
     * @param resultId the id of the results document to get.
     * @param offset an index to determine where how much result to omit from the beginning.
     * @param limit the maximum number of results returned.
     * @return an observable of the json object containing the contents of the results document.
     */
    public static Observable<JsonObject> getResultWithId(String resultId,int offset,int limit){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        Logger.info (String.format ("DB: Getting a result document with ID: %s ,offset: %s and limit: %s",resultId,offset,limit));

        String projectId = "project::" + DBConfig.stripIdFromPrefix (resultId);
        Logger.info (String.format ("DB: Getting template ID for project with ID : %s",projectId));

        int endIndex = offset + limit ;

        return mBucket.query (N1qlQuery.simple (select(Expression.x ("template_id")).from (Expression.x (DBConfig.BUCKET_NAME + " project"))
        .useKeys (Expression.s (projectId)))).flatMap (AsyncN1qlQueryResult::rows)
        .flatMap (row -> Observable.just (row.value ())).flatMap (object -> {
            Logger.info (String.format ("DB: Getting results for project with template ID: %s"),object.getInt ("template_id"));
            if (object.getInt ("template_id") == 1){

                Logger.info (String.format ("DB: Getting results for project with template ID: %s"),object.getInt ("template_id"));

                return mBucket.query (N1qlQuery.simple (select ("contributions_count, " +
                    Expression.x ("results.yes[" + offset + ":array_min([(array_length(results.yes))," + endIndex + "])]")
                    .as ("yes") + ", "
                    + Expression.x ("results.no[" + offset + ":array_min([(array_length(results.no))," + endIndex + "])]")
                    .as ("no"))
                .from (Expression.x (DBConfig.BUCKET_NAME)).useKeys (Expression.s (resultId))))
                .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
                .filter (object1 -> object1.getInt ("contributions_count")!=0);

            }else if (object.getInt ("template_id") == 2 || object.getInt ("template_id") == 3 || object.getInt ("template_id") == 4){

                return mBucket.query (N1qlQuery.simple (select ("contributions_count, " + Expression.x ("results[" + offset
                    + ":array_min([(array_length(results))," + endIndex + "])]")
                    .as ("results")).from (DBConfig.BUCKET_NAME).useKeys (Expression.s (resultId))))
                    .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
                    .filter (object1 -> object1.getInt ("contributions_count")!=0);

            }
                return Observable.just (JsonObject.create ().put ("id",DBConfig.WRONG_TEMPLATE_NUMBER));
            })

        .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
        .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
        .onErrorResumeNext (throwable -> {

            Logger.info (String.format ("DB: Failed to Get a result document with ID: %s ,offset: %s and limit: %s",resultId,offset,limit));
            return Observable.error (new CouchbaseException (String.format ("DB: Failed to Get a result document with ID: %s ,offset: %s and limit: %s, General DB exception",resultId,offset,limit)));
        })
        .defaultIfEmpty(JsonObject.create().put ("id",DBConfig.EMPTY_JSON_OBJECT));
    }

    /**
     * Update results of a project. can error with {@link CouchbaseException},{@link DocumentDoesNotExistException},{@link CASMismatchException} and {@link BucketClosedException} .
     * @param resultId The id of the results document to be updated .
     * @param resultJsonObject The updated Json object to be used as the value of updated document.
     * @return an observable of the updated Json document .
     */
    private static Observable<JsonDocument> updateResult(String resultId,JsonObject resultJsonObject){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        JsonDocument resultDocument = JsonDocument.create (resultId,DBConfig.removeIdFromJson (resultJsonObject));

        return mBucket.replace (resultDocument).timeout (500,TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentDoesNotExistException){
                    return Observable.error (new DocumentDoesNotExistException ("Failed to update result, ID dosen't exist in DB"));

                }else if (throwable instanceof CASMismatchException){
                    //// TODO: 3/28/16 needs more accurate handling in the future.
                    return Observable.error (new CASMismatchException ("Failed to update result, CAS value is changed"));
                }
                else {
                    return Observable.error (new CouchbaseException ("Failed to update result, General DB exception "));
                }
            });
    }

    /**
     * Adds 1 to the contributions count of the result with the provided ID.
     * @param resultId The ID of the result to update.
     * @return An observable of Json object containing the result id and the new contributions count.
     */
    public static Observable<JsonObject> add1ToResultsContributionCount(String resultId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        Logger.info (String.format ("DB: Adding 1 to contributions count of result with id: %s",resultId));

        return mBucket.query (N1qlQuery.simple (update (Expression.x (DBConfig.BUCKET_NAME + " result")).useKeys (Expression.s (resultId))
            .set ("contributions_count",Expression.x ("contributions_count + " + 1 ))
            .returning (Expression.x ("contributions_count, meta(result).id"))))
            .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof CASMismatchException){
                    //// TODO: 4/1/16 needs more accurate handling in the future.
                    Logger.info (String.format ("DB: Failed to add 1 to contributions count of result with id: %s",resultId));

                    return Observable.error (new CASMismatchException (String.format ("DB: Failed to add 1 to contributions count of result with id: %s, General DB exception.",resultId)));
                } else {
                    Logger.info (String.format ("DB: Failed to add 1 to contributions count of result with id: %s",resultId));

                    return Observable.error (new CouchbaseException (String.format ("DB: Failed to add 1 to contributions count of result with id: %s, General DB exception.",resultId)));
                }
            }).defaultIfEmpty(JsonObject.create().put("id", DBConfig.EMPTY_JSON_OBJECT));
    }

    /**
     * Adds a result in the results document for projects that use Template 1.
     * @param resultId The Id of the result document to add the result to.
     * @param answer The user's answer to the question in the template (yes or no).
     * @param locationObject A Json object containing coordinates for the user's location.
     * @return An observable of Json object containing the result id and the added location object.
     */
    public static Observable<JsonObject> addResult(String resultId, String answer,JsonObject locationObject){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        Logger.info (String.format ("DB: Adding a new result with answer: %s and contents: %s to activity with id: %s",answer,locationObject.toString (),resultId));

        return mBucket.query (N1qlQuery.simple (update(Expression.x (DBConfig.BUCKET_NAME + " result"))
            .useKeys (Expression.s (resultId)).set (Expression.x ("results." + answer),
                    arrayAppend(Expression.x ("results." + answer),Expression.x (locationObject)))
            .returning (Expression.x ("results." + answer + "[-1] as location,meta(result).id")))).timeout (1000,TimeUnit.MILLISECONDS)
            .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
            .filter (result -> result.containsKey ("location"))
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof CASMismatchException){
                    //// TODO: 4/1/16 needs more accurate handling in the future.
                    Logger.info (String.format ("DB: Failed to add a new result with answer: %s and contents: %s to activity with id: %s",answer,locationObject.toString (),resultId));

                    return Observable.error (new CASMismatchException (String.format ("DB: Failed to add a new result with answer: %s and contents: %s to activity with id: %s, General DB exception.",answer,locationObject.toString (),resultId)));
                } else {
                    Logger.info (String.format ("DB: Failed to add a new result with answer: %s and contents: %s to activity with id: %s",answer,locationObject.toString (),resultId));

                    return Observable.error (new CouchbaseException (String.format ("DB: Failed to add a new result with answer: %s and contents: %s to activity with id: %s, General DB exception.",answer,locationObject.toString (),resultId)));
                }
            }).defaultIfEmpty(JsonObject.create().put("id", DBConfig.EMPTY_JSON_OBJECT));
    }

    /**
     * Adds a result in the results document for projects that use Template 2, 3, 4.
     * @param resultId The Id of the result document to add the result to.
     * @param resultObject A Json object containing the result content.
     * @return An observable of Json object containing the result id and the added result object.
     */
    public static Observable<JsonObject> addResult(String resultId,JsonObject resultObject){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        Logger.info (String.format ("DB: Adding a new result with contents: %s to activity with id: %s",resultObject.toString (),resultId));

        return mBucket.query (N1qlQuery.simple (update(Expression.x (DBConfig.BUCKET_NAME + " result"))
            .useKeys (Expression.s (resultId)).set (Expression.x ("results"),
                    arrayAppend(Expression.x ("results"),Expression.x (resultObject)))
            .returning (Expression.x ("results[-1] as result,meta(result).id")))).timeout (1000,TimeUnit.MILLISECONDS)
            .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof CASMismatchException){
                    //// TODO: 4/1/16 needs more accurate handling in the future.
                    Logger.info (String.format ("DB: Failed to add a new result with contents: %s to activity with id: %s",resultObject.toString (),resultId));

                    return Observable.error (new CASMismatchException (String.format ("DB: Failed to add a new result with contents: %s to activity with id: %s, General DB exception.",resultObject.toString (),resultId)));
                } else {
                    Logger.info (String.format ("DB: Failed to add a new result with contents: %s to activity with id: %s",resultObject.toString (),resultId));

                    return Observable.error (new CouchbaseException (String.format ("DB: Failed to add a new result with contents: %s to activity with id: %s, General DB exception.",resultObject.toString (),resultId)));
                }
            }).defaultIfEmpty(JsonObject.create().put("id", DBConfig.EMPTY_JSON_OBJECT));
    }
    /**
     * Delete results of a project using its id. can error with {@link CouchbaseException}, {@link DocumentDoesNotExistException} and {@link BucketClosedException} .
     * @param resultId The id of the results document to be deleted.
     * @return An observable with Json document containing only the id .
     */
    public static Observable<JsonDocument> deleteResult(String resultId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        return mBucket.remove (resultId).timeout (500, TimeUnit.MILLISECONDS)
                .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                        .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
                .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                        .delay (Delay.fixed (500, TimeUnit.MILLISECONDS)).once ().build ())
                .onErrorResumeNext (throwable -> {
                    if (throwable instanceof DocumentDoesNotExistException) {
                        return Observable.error (new DocumentDoesNotExistException ("Failed to delete result, ID dosen't exist in DB"));
                    } else {
                        return Observable.error (new CouchbaseException ("Failed to delete result, General DB exception "));
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
