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
import com.couchbase.client.java.query.AsyncN1qlQueryRow;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.dsl.Expression;
import com.couchbase.client.java.util.retry.RetryBuilder;
import play.Logger;
import rx.Observable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.couchbase.client.java.query.Select.select;

public class Project {
    private static AsyncBucket mBucket;
    private static String userIdKey = "id";
    private static String userUrlKey = "url";
    private static String userNameKey = "name";
    private static String ownerKey = "owner";
    private static String resultsKey = "results";
    private static String statsKey = "stats";


    /**
     * Create and save a project. can error with {@link CouchbaseException},{@link DocumentAlreadyExistsException} and {@link BucketClosedException}.
     * @param projectJsonObject The Json object to be the value of the document.
     * @return an observable of the created Json document.
     */
    public static Observable<JsonDocument> createProject(String userId, JsonObject projectJsonObject){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }
        UUID projectUUID = UUID.randomUUID ();
        String projectId = "project::" + projectUUID;
        String resultsId = "result::" + projectUUID;
        String statsID = "stats::" + projectUUID;

        Logger.info ("DB: Creating project with ID: " + projectId);

        return User.getUserWithId (userId).flatMap (userDocument -> {

            JsonObject owner = JsonObject.create ().put (userIdKey, userId)
                    .put (userUrlKey, userDocument.content ().get (userUrlKey))
                    .put (userNameKey, userDocument.content ().get (userNameKey));
            projectJsonObject.put (ownerKey, owner).put (statsKey, statsID).put (resultsKey, resultsId);

            return Observable.just (JsonDocument.create (projectId, projectJsonObject));})

        .flatMap (doc -> mBucket.insert (doc).single ().timeout (500, TimeUnit.MILLISECONDS)
           .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                   .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
           .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                   .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
           .onErrorResumeNext (throwable -> {
               if (throwable instanceof DocumentAlreadyExistsException){
                   UUID newProjectUUID = UUID.randomUUID ();
                   String newProjectId = "project::"+ newProjectUUID;
                   String newStatsId = "stats::" + newProjectUUID;
                   String newResultsId = "results::" + newProjectUUID;

                   Logger.info ("DB: Another project with same id exists, creating a project with another ID: " + projectId);


                   projectJsonObject.put (statsKey,newStatsId).put (resultsKey,newResultsId);
                   JsonDocument newProjectDocument = JsonDocument.create (newProjectId,projectJsonObject);

                   return mBucket.insert (newProjectDocument);
               }

               Logger.info ("DB: Failed to insert project with ID: " + projectId + "General DB exception");
               return Observable.error (new CouchbaseException ("Failed to insert project, General DB exception"));
           }));

    }

    /**
     * Get a project using its id. can error with {@link CouchbaseException} and {@link BucketClosedException}.
     * @param projectId the id of the project to get.
     * @return an observable of the json document if it was found , if it wasn't found it returns an empty json document with id DBConfig.EMPTY_JSON_DOC .
     */
    public static Observable<JsonObject> getProjectWithId(String projectId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        Logger.info ("DB: Getting project with ID: " + projectId);

        return mBucket.query (N1qlQuery.parameterized (select("*").from (Expression.x (DBConfig.BUCKET_NAME + " project"))
            .join (Expression.x (DBConfig.BUCKET_NAME + " category")).onKeys (Expression.x ("project.category_id"))
            .where (Expression.x ("meta(project).id").eq (Expression.x ("$1")))
            ,JsonArray.create ().add (projectId))).timeout (500,TimeUnit.MILLISECONDS)
            .flatMap (AsyncN1qlQueryResult::rows).flatMap (queryRow -> embedCategoryintoProject (projectId, queryRow))
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
            .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {

                Logger.info ("DB: Failed to Get project with ID: " + projectId + ", General DB exception");

                return Observable.error (new CouchbaseException ("Failed to get project, General DB exception"));
            }).defaultIfEmpty (JsonObject.create ());
    }



    /**
     * Update a project. can error with {@link CouchbaseException},{@link DocumentDoesNotExistException},{@link CASMismatchException} and {@link BucketClosedException} .
     * @param projectId The id of the project to be updated .
     * @param projectJsonObject The updated Json object to be used as the value of updated document.
     * @return an observable of the updated Json document .
     */
    public static Observable<JsonDocument> updateProjectWithId(String projectId, JsonObject projectJsonObject){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        JsonDocument projectDocument = JsonDocument.create (projectId,DBConfig.removeIdFromJson (projectJsonObject));

        return mBucket.replace (projectDocument).timeout (500,TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentDoesNotExistException){
                    return Observable.error (new DocumentDoesNotExistException ("Failed to update project, ID dosen't exist in DB"));

                }else if (throwable instanceof CASMismatchException){
                    //// TODO: 3/28/16 needs more accurate handling in the future.
                    return Observable.error (new CASMismatchException ("Failed to update project, CAS value is changed"));
                }
                else {
                    return Observable.error (new CouchbaseException ("Failed to update project, General DB exception "));
                }
            });
    }

    /**
     * Delete a project using its id. can error with {@link CouchbaseException}, {@link DocumentDoesNotExistException} and {@link BucketClosedException} .
     * @param projectId The id of the project document to be deleted.
     * @return An observable with Json document containing only the id .
     */
    public static Observable<JsonDocument> deleteProject(String projectId) {
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        return mBucket.remove (projectId).timeout (500, TimeUnit.MILLISECONDS)
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500, TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof DocumentDoesNotExistException) {
                    return Observable.error (new DocumentDoesNotExistException ("Failed to delete project, ID dosen't exist in DB"));
                } else {
                    return Observable.error (new CouchbaseException ("Failed to delete project, General DB exception "));
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

    private static Observable<JsonObject> embedCategoryintoProject (String projectId, AsyncN1qlQueryRow queryRow) {
        String categoryId = queryRow.value ().getObject ("project").getString ("category_id");
        JsonObject categoryObject = queryRow.value ().getObject ("category").put ("category_id",categoryId);
        JsonObject projectObject = queryRow.value ().getObject ("project").removeKey ("category_id");
        return Observable.just (projectObject.put ("id",projectId).put ("category",categoryObject));
    }
}
