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

public class Project {
    private static AsyncBucket mBucket;

    public static Observable<JsonDocument> createProject(JsonObject projectJsonObject){
        checkDBStatus ();


        String projectId = "project::" + UUID.randomUUID ();
        JsonDocument projectDocument = JsonDocument.create (projectId,DBConfig.removeIdFromJson (projectJsonObject));

        return mBucket.insert (projectDocument).single ().timeout (500, TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentAlreadyExistsException){
                    String newUserId = "project::"+ UUID.randomUUID ();
                    JsonDocument newUserDocument = JsonDocument.create (newUserId,DBConfig.removeIdFromJson (projectJsonObject));
                    return mBucket.insert (newUserDocument);
                }
                throw new CouchbaseException ("Failed to insert project, General exception");
            });
    }

    public static Observable<JsonDocument> getProjectWithId(String projectId){
        checkDBStatus ();

        return mBucket.get (projectId).single ().timeout (500,TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ());
    }

    public static Observable<JsonDocument> updateProjectWithId(String projectId, JsonObject projectJsonObject){
        checkDBStatus ();

        JsonDocument projectDocument = JsonDocument.create (projectId,DBConfig.removeIdFromJson (projectJsonObject));

        return mBucket.replace (projectDocument).timeout (500,TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorReturn (throwable -> {
                if (throwable instanceof DocumentDoesNotExistException){
                    throw new DocumentDoesNotExistException ("Failed to update project, ID dosen't exist in DB");
                }else if (throwable instanceof CASMismatchException){
                    //// TODO: 3/28/16 needs more accurate handling in the future.
                    throw  new CASMismatchException ("Failed to update project, CAS value is changed");
                }
                else {
                    throw new CouchbaseException ("Failed to update project, General exception ");
                }
            });
    }


    public static Observable<JsonDocument> deleteProject(String projectId) {
        checkDBStatus ();

        return mBucket.remove (projectId).timeout (500, TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500, TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorReturn (throwable -> {
                if (throwable instanceof DocumentDoesNotExistException) {
                    throw new DocumentDoesNotExistException ("Failed to delete project, ID dosen't exist in DB");
                } else {
                    throw new CouchbaseException ("Failed to update project, General exception ");
                }
            });
    }

    private static void checkDBStatus () {
        if (DBConfig.bucket.isClosed ()){
            if(DBConfig.initDB () == DBConfig.OPEN_BUCKET_OK){
                mBucket = DBConfig.bucket;
            }else{
                throw new BucketClosedException ("Failed to open bucket due to timeout or backpressure");
            }
        }else {
            mBucket = DBConfig.bucket;
        }
    }
}
