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

import static DBUtilities.DBConfig.EMPTY_JSON_DOC;
import static com.couchbase.client.java.query.Select.select;

/**
 * Created by rashwan on 3/29/16.
 */
public class Activity {
    private static AsyncBucket mBucket;

    /**
     * Create and save a user's activities . can error with {@link CouchbaseException},{@link DocumentAlreadyExistsException} and {@link BucketClosedException}.
     * @param activityId The id for the activity document to be created.
     * @return an observable of the created Json document.
     */
    public static Observable<JsonObject> createActivity(String activityId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }
        JsonDocument activityDocument = JsonDocument.create (activityId,JsonObject.create ().put ("activities", JsonArray.create ()));

        return mBucket.insert (activityDocument).single ().timeout (500, TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentAlreadyExistsException) {
                    return Observable.error (new DocumentAlreadyExistsException ("Failed to create activity, ID already exists"));
                } else {
                    return Observable.error (new CouchbaseException ("Failed to create activity, General DB exception "));
                }
            }).flatMap (jsonDocument -> Observable.just (jsonDocument.content ().put ("id",jsonDocument.id ())));

    }

    /**
     * Get activities of a user using its id. can error with {@link CouchbaseException} and {@link BucketClosedException}.
     * @param activityId the id of the activities document to get.
     * @return an observable of the json document if it was found , if it wasn't found it returns an empty json document with id DBConfig.EMPTY_JSON_DOC .
     */
    public static Observable<JsonObject> getActivityWithId(String activityId,int limit,int offset){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }
        Logger.info ("DB: Getting activity with id: $1 ,limit: $2 and offset: $3",activityId,limit,offset);

        int endIndex = offset + limit ;
        Logger.info (endIndex + "");

        return mBucket.query (N1qlQuery.simple (select(Expression.x ("activities[" + offset + ":" + endIndex + "] activities"))
        .from (DBConfig.BUCKET_NAME).useKeys (Expression.s (activityId)))).timeout (1000,TimeUnit.MILLISECONDS)
        .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
        .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
        .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
        .onErrorResumeNext (throwable -> {
            Logger.info ("DB: failed to get activity with id: $1 ,limit: $2 and offset: $3",activityId,limit,offset);

            return Observable.error (new CouchbaseException (String.format ("DB: failed to get activity with id: $1 ,limit: $2 and offset: $3",activityId,limit,offset)));
        })
        .defaultIfEmpty (JsonObject.create ().put ("id",EMPTY_JSON_DOC));

    }

    /**
     * Update activities of a user. can error with {@link CouchbaseException},{@link DocumentDoesNotExistException},{@link CASMismatchException} and {@link BucketClosedException} .
     * @param activityId The id of the activities document to be updated .
     * @param activityJsonObject The updated Json object to be used as the value of updated document.
     * @return an observable of the updated Json document .
     */
    private static Observable<JsonDocument> updateActivity(String activityId,JsonObject activityJsonObject){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        JsonDocument activityDocument = JsonDocument.create (activityId,DBConfig.removeIdFromJson (activityJsonObject));

        return mBucket.replace (activityDocument).timeout (500,TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentDoesNotExistException){
                    return Observable.error (new DocumentDoesNotExistException ("Failed to update activity, ID dosen't exist in DB"));

                }else if (throwable instanceof CASMismatchException){
                    //// TODO: 3/28/16 needs more accurate handling in the future.
                    return Observable.error (new CASMismatchException ("Failed to update activity, CAS value is changed"));
                }
                else {
                    return Observable.error (new CouchbaseException ("Failed to update activity, General DB exception "));
                }
            });
    }

    /**
     * Delete activities of a user using its id. can error with {@link CouchbaseException}, {@link DocumentDoesNotExistException} and {@link BucketClosedException} .
     * @param activityId The id of the activities document to be deleted.
     * @return An observable with Json document containing only the id .
     */
    public static Observable<JsonDocument> deleteActivity(String activityId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }
        return mBucket.remove (activityId).timeout (500, TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500, TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentDoesNotExistException) {
                    return Observable.error (new DocumentDoesNotExistException ("Failed to delete activity, ID dosen't exist in DB"));
                } else {
                    return Observable.error (new CouchbaseException ("Failed to delete activity, General DB exception "));
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
