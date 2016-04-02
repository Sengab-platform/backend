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
import com.couchbase.client.java.query.AsyncN1qlQueryRow;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.dsl.Expression;
import com.couchbase.client.java.util.retry.RetryBuilder;
import play.Logger;
import rx.Observable;

import java.util.concurrent.TimeUnit;

import static DBUtilities.DBConfig.bucket;
import static com.couchbase.client.java.query.Update.update;

/**
 * Created by rashwan on 3/28/16.
 */
public class User {
    private static AsyncBucket mBucket;

    /**
     * Create and save a user. can error with {@link CouchbaseException},{@link DocumentAlreadyExistsException} and {@link BucketClosedException}.
     * @param userJsonObject The Json object to be the value of the document , it also has an Id field to use as the document key.
     * @return an observable of the created Json document.
     */
    public static Observable<JsonDocument> createUser(JsonObject userJsonObject){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        String userId =  DBConfig.getIdFromJson (userJsonObject);
        JsonDocument userDocument = JsonDocument.create (userId,DBConfig.removeIdFromJson (userJsonObject));

        return mBucket.insert (userDocument).single ().timeout (500, TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentAlreadyExistsException) {
                    return Observable.error (new DocumentAlreadyExistsException ("Failed to create user, ID already exists"));
                } else {
                    return Observable.error (new CouchbaseException ("Failed to create user, General DB exception "));
                }
            });
    }
    /**
     * Get a user using its id. can error with {@link CouchbaseException} and {@link BucketClosedException}.
     * @param userId the id of the user to get.
     * @return an observable of the json document if it was found , if it wasn't found it returns an empty json document with id DBConfig.EMPTY_JSON_DOC .
     */
    public static Observable<JsonDocument> getUserWithId(String userId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }


        return mBucket.get (userId).timeout (500,TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                return Observable.error (new CouchbaseException ("Failed to get user, General DB exception"));
            })
            .defaultIfEmpty (JsonDocument.create (DBConfig.EMPTY_JSON_DOC));
    }

    /**
     * Receives updated user profile data from the provider when signing in and update the user profile without affecting the rest of the profile's data. can error with {@link CouchbaseException},{@link DocumentDoesNotExistException},{@link CASMismatchException} and {@link BucketClosedException} .
     * @param userId the id of the user to update
     * @param firstName the updated first name of the user.
     * @param lastName the updated last name of the user.
     * @param imageURL the updated image name of the user.
     * @param about the updated about of the user.
     * @return an Observable of the document fragment that was updated that contains updated cas and other meta data about the mutation.
     */
    public static Observable<AsyncN1qlQueryRow> updateSigningInUser(String userId, String firstName, String lastName, String imageURL, JsonObject about){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }
        Logger.info ("DB: Partial updating user with ID: " + userId);


        return mBucket.query (N1qlQuery.simple (update(DBConfig.BUCKET_NAME).useKeys (Expression.s (userId))
                .set ("first_name",firstName).set ("last_name",lastName)
                .set ("image",imageURL).set ("about",about).returning ("meta(default).id")))
                .flatMap (AsyncN1qlQueryResult::rows)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentDoesNotExistException){
                    Logger.info ("DB: Failed to Partial update user with ID: " + userId + " , no user exists with this id.");

                    return Observable.error (new DocumentDoesNotExistException ("Failed to update user, ID dosen't exist in DB."));

                }else if (throwable instanceof CASMismatchException){
                    //// TODO: 4/1/16 needs more accurate handling in the future.
                    Logger.info ("DB: Failed to Partial update user with ID: " + userId + " , CAS value is changed.");

                    return Observable.error (new CASMismatchException ("Failed to update user, CAS value is changed."));
                } else {
                    Logger.info ("DB: Failed to Partial update user with ID: " + userId + " , General DB exception.");

                    return Observable.error (new CouchbaseException ("Failed to update user, General DB exception."));
                }
            });
    }
    /**
     * Update a user. can error with {@link CouchbaseException},{@link DocumentDoesNotExistException},{@link CASMismatchException} and {@link BucketClosedException} .
     * @param userId The id of the user to be updated .
     * @param userJsonObject The updated Json object to be used as the value of updated document.
     * @return an observable of the updated Json document .
     */
    public static Observable<JsonDocument> updateUserWithId(String userId, JsonObject userJsonObject) {
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        JsonDocument userDocument = JsonDocument.create (userId,DBConfig.removeIdFromJson (userJsonObject));

        return mBucket.replace (userDocument).timeout (500,TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentDoesNotExistException){
                    return Observable.error (new DocumentDoesNotExistException ("Failed to update user, ID dosen't exist in DB"));

                }else if (throwable instanceof CASMismatchException){
                    //// TODO: 3/28/16 needs more accurate handling in the future.
                    return Observable.error (new CASMismatchException ("Failed to update user, CAS value is changed"));
                }
                else {
                    return Observable.error (new CouchbaseException ("Failed to update user, General DB exception "));
                }
            });
    }

    /**
     * Delete a user using its id. can error with {@link CouchbaseException}, {@link DocumentDoesNotExistException} and {@link BucketClosedException} .
     * @param userId The id of the user document to be deleted.
     * @return An observable with Json document containing only the id .
     */
    public static Observable<JsonDocument> deleteUser(String userId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        return mBucket.remove (userId).timeout (500, TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500, TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentDoesNotExistException) {
                    return Observable.error (new DocumentDoesNotExistException ("Failed to delete user, ID dosen't exist in DB"));
                } else {
                    return Observable.error (new CouchbaseException ("Failed to delete user, General DB exception "));
                }
            });
    }

    private static void checkDBStatus () {
        if (bucket.isClosed ()){
            if (DBConfig.initDB() == DBConfig.OPEN_BUCKET_OK) {
                mBucket = bucket;
            }else{
                throw new BucketClosedException ("Failed to open bucket due to timeout or backpressure");

            }
        }else {
            mBucket = bucket;
        }
    }
}
