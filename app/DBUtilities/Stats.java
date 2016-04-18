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
import static com.couchbase.client.java.query.dsl.functions.ArrayFunctions.arrayLength;

/**
 * Created by rashwan on 3/29/16.
 */
public class Stats {
    private static AsyncBucket mBucket;

    /**
     * Create and save a project's stats. can error with {@link CouchbaseException},{@link DocumentAlreadyExistsException} and {@link BucketClosedException}.
     * @param statsId the id of the stats document to create.
     * @return an observable of the created Json document.
     */
    public static Observable<JsonObject> createStats(String statsId,JsonObject statsObject){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }
        Logger.info (String.format ("DB: creating stats document with ID: %s",statsId));
        JsonDocument statsDocument = JsonDocument.create (statsId,statsObject);

        return mBucket.insert (statsDocument).single ().timeout (500, TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentAlreadyExistsException) {
                    Logger.info (String.format ("DB: Failed to create stats document with ID: %s",statsId));

                    return Observable.error (new DocumentAlreadyExistsException (String.format ("DB: Failed to create stats document with ID: %s, ID already exists",statsId)));
                } else {
                    Logger.info (String.format ("DB: Failed to create stats document with ID: %s",statsId));

                    return Observable.error (new CouchbaseException (String.format ("DB: Failed to create stats document with ID: %s, General DB exception ",statsId)));
                }
            }).flatMap (jsonDocument -> Observable.just (jsonDocument.content ().put ("id",jsonDocument.id ())));

    }

    /**
     * Get stats for a project using its id. can error with {@link CouchbaseException} and {@link BucketClosedException}.
     * @param statsId the id of the stats document to get.
     * @return an observable of the json document if it was found , if it wasn't found it returns an empty json document with id DBConfig.EMPTY_JSON_OBJECT .
     */
    public static Observable<JsonObject> getStatsWithId(String statsId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        Logger.info (String.format ("DB: Getting stats document with ID: %s",statsId));

        return mBucket.get (statsId).timeout (500,TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                Logger.info (String.format ("DB: Failed to get stats document with ID: %s",statsId));

                return Observable.error (new CouchbaseException (String.format ("Failed to get stats document with ID: %s, General DB exception")));
            })
                .defaultIfEmpty(JsonDocument.create(DBConfig.EMPTY_JSON_OBJECT, JsonObject.create()))
            .flatMap (jsonDocument -> Observable.just (jsonDocument.content ().put ("id",jsonDocument.id ())));
    }

    /**
     * Adds 1 to the contributions count of the stats with the provided ID.
     * @param statsId The ID of the stats to update.
     * @return An observable of Json object containing the stats id and the new contributions count.
     */
    public static Observable<JsonObject> add1ToStatsContributionCount(String statsId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        Logger.info (String.format ("DB: Adding 1 to contributions count of project with id: %s",statsId));

        return mBucket.query (N1qlQuery.simple (update (Expression.x (DBConfig.BUCKET_NAME + " stats")).useKeys (Expression.s (statsId))
        .set ("contributions_count",Expression.x ("contributions_count + " + 1 ))
        .returning (Expression.x ("contributions_count, meta(stats).id"))))
        .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
        .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
        .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
        .onErrorResumeNext (throwable -> {
            if (throwable instanceof CASMismatchException){
                //// TODO: 4/1/16 needs more accurate handling in the future.
                Logger.info (String.format ("DB: Failed to add 1 to contributions count of stats with id: %s",statsId));

                return Observable.error (new CASMismatchException (String.format ("DB: Failed to add 1 to contributions count of stats with id: %s, General DB exception.",statsId)));
            } else {
                Logger.info (String.format ("DB: Failed to add 1 to contributions count of stats with id: %s",statsId));

                return Observable.error (new CouchbaseException (String.format ("DB: Failed to add 1 to contributions count of stats with id: %s, General DB exception.",statsId)));
            }
        }).defaultIfEmpty(JsonObject.create().put("id", DBConfig.EMPTY_JSON_OBJECT));
    }

    /**
     * Adds 1 to the enrollments count of the stats with the provided ID.
     * @param statsId The ID of the stats to update.
     * @return An observable of Json object containing the stats id and the new enrollments count.
     */
    public static Observable<JsonObject> add1ToStatsEnrollmentsCount(String statsId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        Logger.info (String.format ("DB: Adding 1 to contributions count of stats with id: %s",statsId));

        return mBucket.query (N1qlQuery.simple (update (Expression.x (DBConfig.BUCKET_NAME + " stats")).useKeys (Expression.s (statsId))
        .set ("enrollments_count",Expression.x ("enrollments_count + " + 1 ))
        .returning (Expression.x ("enrollments_count, meta(stats).id"))))
        .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
        .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
        .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
        .onErrorResumeNext (throwable -> {
            if (throwable instanceof CASMismatchException){
                //// TODO: 4/1/16 needs more accurate handling in the future.
                Logger.info (String.format ("DB: Failed to add 1 to enrollments count of stats with id: %s",statsId));

                return Observable.error (new CASMismatchException (String.format ("DB: Failed to add 1 to enrollments count of stats with id: %s, General DB exception.",statsId)));
            } else {
                Logger.info (String.format ("DB: Failed to add 1 to enrollments count of stats with id: %s",statsId));

                return Observable.error (new CouchbaseException (String.format ("DB: Failed to add 1 to enrollments count of stats with id: %s, General DB exception.",statsId)));
            }
        }).defaultIfEmpty(JsonObject.create().put("id", DBConfig.EMPTY_JSON_OBJECT));
    }

    /**
     * Remove 1 from the enrollments count of the stats with the provided ID.
     * @param statsId The ID of the stats to update.
     * @return An observable of Json object containing the stats id and the new enrollments count.
     */
    public static Observable<JsonObject> remove1FromStatsEnrollmentsCount(String statsId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        Logger.info (String.format ("DB: Removing 1 from contributions count of stats with id: %s",statsId));

        return mBucket.query (N1qlQuery.simple (update (Expression.x (DBConfig.BUCKET_NAME + " stats")).useKeys (Expression.s (statsId))
        .set ("enrollments_count",Expression.x ("enrollments_count - " + 1 ))
        .returning (Expression.x ("enrollments_count, meta(stats).id"))))
        .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
        .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
        .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
        .onErrorResumeNext (throwable -> {
            if (throwable instanceof CASMismatchException){
                //// TODO: 4/1/16 needs more accurate handling in the future.
                Logger.info (String.format ("DB: Failed to remove 1 from enrollments count of stats with id: %s",statsId));

                return Observable.error (new CASMismatchException (String.format ("DB: Failed to remove 1 from enrollments count of stats with id: %s, General DB exception.",statsId)));
            } else {
                Logger.info (String.format ("DB: Failed to remove 1 from enrollments count of stats with id: %s",statsId));

                return Observable.error (new CouchbaseException (String.format ("DB: Failed to remove 1 from enrollments count of stats with id: %s, General DB exception.",statsId)));
            }
        }).defaultIfEmpty(JsonObject.create().put("id", DBConfig.EMPTY_JSON_OBJECT));
    }

    /**
     * Update the gender stats for a project when a user contributes to it.
     * @param statsId The ID of the stats document to be updated.
     * @param userId  The ID of the user making the contribution.
     * @param userGender The gender of the contributing user.
     * @return An Observable of Json object containing the stats ID and new gender count.
     */
    public static Observable<JsonObject> updateContibutorsGender(String statsId,String userId, String userGender){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        Logger.info (String.format ("DB: Updating gender stats with ID: %s and gender: %s",statsId,userGender));

        String contributionId = "contribution::" + DBConfig.stripIdFromPrefix (statsId) + "::" + DBConfig.stripIdFromPrefix (userId);

        Logger.info (String.format ("DB: Constructed the contribution ID: %s",contributionId));

        return mBucket.query (N1qlQuery.simple (select(arrayLength(Expression.x ("contributions")).as (Expression.x ("length")))
        .from (Expression.x (DBConfig.BUCKET_NAME) + " contribution").useKeys (Expression.s (contributionId))))
        .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> {
            if (row.value ().getInt ("length") == 1){
                Logger.info (String.format ("DB: First contribution for user with ID: %s ,updating the gender stats.",userId));

                return mBucket.query (N1qlQuery.simple (update (Expression.x (DBConfig.BUCKET_NAME + " stats"))
                .useKeys (Expression.s (statsId))
                .set (Expression.x ("contributors_gender." + userGender),Expression.x ("contributors_gender." + userGender + " +1"))
                .returning (Expression.x ("meta(stats).id, contributors_gender." + userGender))))
                .flatMap (AsyncN1qlQueryResult::rows).flatMap (statsRow -> Observable.just (statsRow.value ()))
                .filter (object -> object.containsKey (userGender));
            }else {
                Logger.info (String.format ("DB: Not the first contribution for user with ID: %s ,not updating the gender stats.",userId));
                return Observable.just (JsonObject.create ().put ("id", DBConfig.ALREADY_CONTRIBUTED));
            }})
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500, TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                Logger.info (String.format ("DB: Failed to update gender stats with id: %s and gender: %s", statsId, userGender));
                return Observable.error (new CouchbaseException (String.format ("DB: Failed to update gender stats with id: %s and gender: %s, General DB exception.", statsId, userGender)));
            }).defaultIfEmpty(JsonObject.create().put("id", DBConfig.EMPTY_JSON_OBJECT));
    }

    /**
     * Update stats of a project. can error with {@link CouchbaseException},{@link DocumentDoesNotExistException},{@link CASMismatchException} and {@link BucketClosedException} .
     * @param statsId The id of the stats document to be updated .
     * @param statsJsonObject The updated Json object to be used as the value of updated document.
     * @return an observable of the updated Json document .
     */
    public static Observable<JsonDocument> updateStats(String statsId,JsonObject statsJsonObject){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        JsonDocument statsDocument = JsonDocument.create (statsId,DBConfig.removeIdFromJson (statsJsonObject));

        return mBucket.replace (statsDocument).timeout (500,TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentDoesNotExistException){
                    return Observable.error (new DocumentDoesNotExistException ("Failed to update stats, ID dosen't exist in DB"));

                }else if (throwable instanceof CASMismatchException){
                    //// TODO: 3/28/16 needs more accurate handling in the future.
                    return Observable.error (new CASMismatchException ("Failed to update stats, CAS value is changed"));
                }
                else {
                    return Observable.error (new CouchbaseException ("Failed to update stats, General DB exception "));
                }
            });
    }

    /**
     * Delete stats of a project using its id. can error with {@link CouchbaseException}, {@link DocumentDoesNotExistException} and {@link BucketClosedException} .
     * @param statsId The id of the stats document to be deleted.
     * @return An observable with Json document containing only the id .
     */

    public static Observable<JsonDocument> deleteStats(String statsId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        return mBucket.remove (statsId).timeout (500, TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500, TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentDoesNotExistException) {
                    return Observable.error (new DocumentDoesNotExistException ("Failed to delete stats, ID dosen't exist in DB"));
                } else {
                    return Observable.error (new CouchbaseException ("Failed to delete stats, General DB exception "));
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
