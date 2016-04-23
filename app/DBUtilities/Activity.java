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

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static DBUtilities.DBConfig.EMPTY_JSON_OBJECT;
import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.Update.update;
import static com.couchbase.client.java.query.dsl.functions.ArrayFunctions.arrayAppend;

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
    public static Observable<JsonObject> createActivity(String activityId,JsonObject activityObject){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }
        JsonDocument activityDocument = JsonDocument.create (activityId,activityObject);

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
     * Adds an activity to the user's activities list.
     * @param projectId The project id which the user made the activity on.
     * @param activityId The id of the activity document related to this user.
     * @param activityObject The activity content.
     * @return An observable of Json object containing the activity which was appended to the list and the id of the activity document.
     */
    public static Observable<JsonObject> addActivity(String projectId,String activityId,JsonObject activityObject){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        return Project.getProjectName (projectId)
        .flatMap (projectObject -> embedProjectNameInActivity (projectObject,activityObject))
        .flatMap (activity -> mBucket.query (N1qlQuery.simple (update(Expression.x (DBConfig.BUCKET_NAME + " activity"))
        .useKeys (Expression.s (activityId)).set (Expression.x ("activities"),
            arrayAppend(Expression.x ("activities"),Expression.x (activity)))
        .returning (Expression.x ("activities[-1] as activity,meta(activity).id"))))).timeout (1000,TimeUnit.MILLISECONDS)
        .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
        .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
        .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
        .onErrorResumeNext (throwable -> {
            if (throwable instanceof CASMismatchException){
                //// TODO: 4/1/16 needs more accurate handling in the future.
                Logger.info (String.format ("DB: Failed to add a new activity with contents: %s to activity with id: %s",activityObject,activityId));

                return Observable.error (new CASMismatchException (String.format ("DB: Failed to add a new activity with contents: %s to activity with id: %s, General DB exception.",activityObject.toString (),activityId)));
            } else {
                Logger.info (String.format ("DB: Failed to add a new activity with contents: %s to activity with id: %s",activityObject,activityId));

                return Observable.error (new CouchbaseException (String.format ("DB: Failed to add a new activity with contents: %s to activity with id: %s, General DB exception.",activityObject.toString (),activityId)));
            }
        }).defaultIfEmpty(JsonObject.create().put("id", DBConfig.EMPTY_JSON_OBJECT));
    }
    /**
     * Get activities of a user using its id. can error with {@link CouchbaseException} and {@link BucketClosedException}.
     * @param activityId the id of the activities document to get.
     * @param offset an index to determine where how much result to omit from the beginning.
     * @param limit the maximum number of document returned.
     * @return an observable of the json document if it was found , if it wasn't found it returns an empty json document with id DBConfig.EMPTY_JSON_OBJECT .
     */
    public static Observable<JsonObject> getActivityWithId(String activityId,int offset,int limit){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }
        Logger.info (String.format ("DB: Getting activity with id: %s ,limit: %s and offset: %s",activityId,limit,offset));

        int endIndex = offset + limit ;

        return mBucket.query (N1qlQuery.simple (select(Expression.x ("activities[" + offset + ":array_min([(array_length(activities))," + endIndex + "])]")
            .as ("activities")).from (DBConfig.BUCKET_NAME)
            .useKeys (Expression.s (activityId)))).timeout (1000,TimeUnit.MILLISECONDS)
        .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
                .filter (object -> !object.getArray ("activities").isEmpty ())
                .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
        .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
        .onErrorResumeNext (throwable -> {
            Logger.info (String.format ("DB: failed to get activity with id: %s ,limit: %s and offset: %s",activityId,limit,offset));

            return Observable.error (new CouchbaseException (String.format ("DB: failed to get activity with id: %s ,limit: %s and offset: %s",activityId,limit,offset)));
        })
                .defaultIfEmpty(JsonObject.create().put("id", EMPTY_JSON_OBJECT));

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

    private static Observable<JsonObject> embedProjectNameInActivity(JsonObject projectObject,JsonObject activityObject){
        UUID activityId = UUID.randomUUID ();
        String projectName = projectObject.getString ("name");
        activityObject.getObject ("project").put ("name",projectName);
        return Observable.just (activityObject.put ("id",activityId.toString ()));
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
