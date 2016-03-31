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
import rx.Observable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by rashwan on 3/29/16.
 */
public class Category {

    private static AsyncBucket mBucket;

    /**
     * Create and save a category. can error with {@link CouchbaseException},{@link DocumentAlreadyExistsException} and {@link BucketClosedException}.
     * @param categoryJsonObject The Json object to be the value of the document.
     * @return an observable of the created Json document.
     */
    public static Observable<JsonDocument> createCategory(JsonObject categoryJsonObject){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        String categoryId = "category::" + UUID.randomUUID ();
        JsonDocument categoryDocument = JsonDocument.create (categoryId,categoryJsonObject);

        return mBucket.insert (categoryDocument).single ().timeout (500, TimeUnit.MILLISECONDS)
                .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                        .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
                .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                        .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
                .onErrorResumeNext (throwable -> {
                    if (throwable instanceof DocumentAlreadyExistsException){
                        String newUserId = "category::"+ UUID.randomUUID ();
                        JsonDocument newCategoryDocument = JsonDocument.create (newUserId,DBConfig.removeIdFromJson (categoryJsonObject));
                        return mBucket.insert (newCategoryDocument);
                    }
                    return Observable.error (new CouchbaseException ("Failed to insert category, General DB exception"));
                });
    }

    /**
     * Get a category using its id. can error with {@link CouchbaseException} and {@link BucketClosedException}.
     * @param categoryId the id of the category to get.
     * @return an observable of the json document if it was found , if it wasn't found it returns an empty json document with id DBConfig.EMPTY_JSON_DOC .
     */
    public static Observable<JsonDocument> getCategoryWithId(String categoryId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        return mBucket.get (categoryId).timeout (500,TimeUnit.MILLISECONDS)
                .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                        .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
                .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                        .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
                .onErrorResumeNext (throwable -> {
                    return Observable.error (new CouchbaseException ("Failed to get category, General DB exception"));
                })
                .defaultIfEmpty (JsonDocument.create (DBConfig.EMPTY_JSON_DOC));
    }

    /**
     * Update a category. can error with {@link CouchbaseException},{@link DocumentDoesNotExistException},{@link CASMismatchException} and {@link BucketClosedException} .
     * @param categoryId The id of the category to be updated .
     * @param categoryJsonObject The updated Json object to be used as the value of updated document.
     * @return an observable of the updated Json document .
     */
    public static Observable<JsonDocument> updateCategoryWithId(String categoryId, JsonObject categoryJsonObject){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        JsonDocument categoryDocument = JsonDocument.create (categoryId,DBConfig.removeIdFromJson (categoryJsonObject));

        return mBucket.replace (categoryDocument).timeout (500,TimeUnit.MILLISECONDS)
                .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                        .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
                .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                        .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
                .onErrorResumeNext (throwable -> {
                    if (throwable instanceof DocumentDoesNotExistException){
                        return Observable.error (new DocumentDoesNotExistException ("Failed to update category, ID dosen't exist in DB"));

                    }else if (throwable instanceof CASMismatchException){
                        //// TODO: 3/28/16 needs more accurate handling in the future.
                        return Observable.error (new CASMismatchException ("Failed to update category, CAS value is changed"));
                    }
                    else {
                        return Observable.error (new CouchbaseException ("Failed to update category, General DB exception "));
                    }
                });
    }

    /**
     * Delete a category using its id. can error with {@link CouchbaseException}, {@link DocumentDoesNotExistException} and {@link BucketClosedException} .
     * @param categoryId The id of the category document to be deleted.
     * @return An observable with Json document containing only the id .
     */
    public static Observable<JsonDocument> deleteCategory(String categoryId) {
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        return mBucket.remove (categoryId).timeout (500, TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500, TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentDoesNotExistException) {
                    return Observable.error (new DocumentDoesNotExistException ("Failed to delete category, ID dosen't exist in DB"));
                } else {
                    return Observable.error (new CouchbaseException ("Failed to delete category, General DB exception "));
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