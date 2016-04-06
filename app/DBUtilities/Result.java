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
import com.couchbase.client.java.util.retry.RetryBuilder;
import play.Logger;
import rx.Observable;

import java.util.concurrent.TimeUnit;

/**
 * Created by rashwan on 3/29/16.
 */
public class Result {
    private static AsyncBucket mBucket;
    private static final Logger.ALogger logger = Logger.of (Result.class.getSimpleName ());

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
        logger.info (String.format ("DB: Adding a result document with ID: $1 ,to the DB ",resultId));
        JsonDocument resultDocument = JsonDocument.create (resultId,resultObject);

        return mBucket.insert (resultDocument).single ().timeout (500, TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentAlreadyExistsException) {
                    logger.info (String.format ("DB: Failed to add a result document with ID: $1 ,to the DB ",resultId));

                    return Observable.error (new DocumentAlreadyExistsException (String.format ("Failed to create result document with ID: $1 , ID already exists",resultId)));
                } else {
                    return Observable.error (new CouchbaseException (String.format ("Failed to create result document with ID: $1 , General DB exception ",resultId)));
                }
            }).flatMap (jsonDocument -> Observable.just (jsonDocument.content ().put ("id",jsonDocument.id ())));
    }

    /**
     * Get results for a project using its id. can error with {@link CouchbaseException} and {@link BucketClosedException}.
     * @param resultId the id of the results document to get.
     * @return an observable of the json document if it was found , if it wasn't found it returns an empty json document with id DBConfig.EMPTY_JSON_DOC .
     */
    public static Observable<JsonObject> getResultWithId(String resultId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        logger.info (String.format ("DB: Getting a result document with ID: $1",resultId));

        return mBucket.get (resultId).timeout (500,TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {

                logger.info (String.format ("DB: Failed to get a result document with ID: $1",resultId));
                return Observable.error (new CouchbaseException (String.format ("Failed to get result with ID: $1, General DB exception",resultId)));
            })
            .defaultIfEmpty (JsonDocument.create (DBConfig.EMPTY_JSON_DOC,JsonObject.create ()))
            .flatMap (jsonDocument -> Observable.just (jsonDocument.content ().put ("id",jsonDocument.id ())));
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
