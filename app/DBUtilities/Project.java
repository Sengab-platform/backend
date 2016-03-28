package DBUtilities;

import com.couchbase.client.core.time.Delay;
import com.couchbase.client.deps.io.netty.handler.timeout.TimeoutException;
import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.error.TemporaryFailureException;
import com.couchbase.client.java.util.retry.RetryBuilder;
import rx.Observable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Project {
    private static AsyncBucket mBucket;

    public static Observable<JsonDocument> createProject(JsonObject projectJsonObject){
        if (DBConfig.bucket == null){
            if(DBConfig.initDB () == 0){
                mBucket = DBConfig.bucket;
            }else{
                return null;
            }
        }


        String projectId = "project::" + UUID.randomUUID ();
        JsonDocument projectDocument = JsonDocument.create (projectId,DBConfig.removeIdFromJson (projectJsonObject));

        return mBucket.insert (projectDocument).single ().timeout (500, TimeUnit.MILLISECONDS)
                .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class)
                        .delay (Delay.fixed (1, TimeUnit.SECONDS)).max (3).build ())
                .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                        .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
                .onErrorResumeNext (throwable -> {
                    if (throwable instanceof DocumentAlreadyExistsException){
                        String newUserId = "project::"+ UUID.randomUUID ();
                        JsonDocument newUserDocument = JsonDocument.create (newUserId,DBConfig.removeIdFromJson (projectJsonObject));
                        return mBucket.insert (newUserDocument);
                    }
                    return Observable.error (throwable);
                });
    }
}
