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
import com.couchbase.client.java.query.dsl.Sort;
import com.couchbase.client.java.util.retry.RetryBuilder;
import play.Logger;
import rx.Observable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static DBUtilities.DBConfig.EMPTY_JSON_DOC;
import static DBUtilities.DBConfig.bucket;
import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.Update.update;

public class Project {
    private static AsyncBucket mBucket;
    private static String ownerIdKey = "id";
    private static String ownerImageKey = "image";
    private static String ownerNameKey = "name";
    private static String userImageKey = "image";
    private static String userFirstNameKey = "first_name";
    private static String userLastNameKey = "last_name";
    private static String ownerKey = "owner";
    private static String resultsKey = "results";
    private static String statsKey = "stats";
    private static final Logger.ALogger logger = Logger.of (Project.class.getSimpleName ());


    /**
     * Create and save a project. can error with {@link CouchbaseException},{@link DocumentAlreadyExistsException} and {@link BucketClosedException}.
     * @param projectJsonObject The Json object to be the value of the document.
     * @return an observable of the created Json document.
     */
    public static Observable<JsonObject> createProject(String userId, JsonObject projectJsonObject){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }
        UUID projectUUID = UUID.randomUUID ();
        String projectId = "project::" + projectUUID;
        String resultsId = "result::" + projectUUID;
        String statsID = "stats::" + projectUUID;

        logger.info ("DB: Creating project with ID: {}" ,projectId);

        return User.getUserWithId (userId).flatMap (userJsonObject -> {

            JsonObject owner = JsonObject.create ().put (ownerIdKey, userId)
                .put (ownerImageKey, userJsonObject.getString (userImageKey))
                .put (ownerNameKey, userJsonObject.getString (userFirstNameKey) + " " + userJsonObject.getString (userLastNameKey));
            projectJsonObject.put (ownerKey, owner).put (statsKey, statsID).put (resultsKey, resultsId);

            return Observable.just (JsonDocument.create (projectId, projectJsonObject));})

        .flatMap (doc -> mBucket.insert (doc)
        ).single ().timeout (500, TimeUnit.MILLISECONDS)
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

                   logger.info ("DB: Another project with same id exists, creating a project with another ID: {}", projectId);

                   projectJsonObject.put (statsKey,newStatsId).put (resultsKey,newResultsId);
                   JsonDocument newProjectDocument = JsonDocument.create (newProjectId,projectJsonObject);

                   return mBucket.insert (newProjectDocument);
               }

               logger.info ("DB: Failed to insert project with ID: {}, General DB exception",projectId);
               return Observable.error (new CouchbaseException ("Failed to insert project, General DB exception"));
           }).flatMap (jsonDocument -> Observable.just (jsonDocument.content ().put ("id",jsonDocument.id ())));

    }

    /**
     * Get a project using its id. can error with {@link CouchbaseException} and {@link BucketClosedException}.
     * @param projectId the id of the project to get.
     * @return an observable of json object the resulted project merged with its category and with the id field added, if the document was not found returns an empty json object.
     */
    public static Observable<JsonObject> getProjectWithId(String projectId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        logger.info ("DB: Getting project with ID: {}", projectId);

        return mBucket.query (N1qlQuery.simple (select("*").from (Expression.x (DBConfig.BUCKET_NAME + " project"))
            .join (Expression.x (DBConfig.BUCKET_NAME + " category")).onKeys (Expression.x ("project.category_id"))
            .where (Expression.x ("meta(project).id").eq (Expression.s (projectId)))))
            .timeout (500,TimeUnit.MILLISECONDS)
            .flatMap (AsyncN1qlQueryResult::rows).flatMap (queryRow -> DBConfig.embedIdAndCategoryIntoProject (projectId, queryRow))
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
            .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {

                logger.info ("DB: Failed to Get project with ID: {}, General DB exception",projectId);

                return Observable.error (new CouchbaseException (String.format ("Failed to get project with ID: $1, General DB exception",projectId)));
            }).defaultIfEmpty (JsonObject.create ().put ("id",DBConfig.EMPTY_JSON_DOC));
    }

    /**
     * Bulk gets projects sorts them using any document field and sets a limit and offset for the results.
     * @param sortBy A Json field in the project document to sort the results with.
     * @param limit the maximum number of document returned.
     * @param offset an index to determine where how much result to omit from the beginning.
     * @return an observable of json object that contains all the resulted projects merged with their categories and with id field added.
     */

    public static Observable<JsonObject> bulkGetProjects(String sortBy, int offset, int limit){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        logger.info ("DB: bulk getting projects sorted by: {} and with limit: {} and offset: {}",sortBy,limit,offset);

        return bucket.query (N1qlQuery.simple (select(Expression.x ("meta(project).id, *")).from (Expression.x (DBConfig.BUCKET_NAME + " project"))
            .join (Expression.x (DBConfig.BUCKET_NAME + " category")).onKeys (Expression.x ("project.category_id"))
            .orderBy (Sort.desc (Expression.x ("project." + Expression.x (sortBy))))
            .limit (limit).offset (offset)))
            .timeout (1000,TimeUnit.MILLISECONDS)
            .flatMap (AsyncN1qlQueryResult::rows).flatMap (queryRow -> DBConfig.embedIdAndCategoryIntoProject (queryRow.value ().getString ("id"), queryRow))
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                     .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                     .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                logger.info ("DB: failed to bulk get projects sorted by: {} and with limit: {} and offset: {}",sortBy,limit,offset);

                return Observable.error (new CouchbaseException (String.format ("DB: failed to bulk get projects sorted by: $1 and with limit: $2 and offset: $3",sortBy,limit,offset)));
            })
            .defaultIfEmpty (JsonObject.create ().put ("id",DBConfig.EMPTY_JSON_DOC));
    }

    /**
     * Bulk gets projects sorts them by popularity and sets a limit and offset for the results.
     * @param limit the maximum number of document returned.
     * @param offset an index to determine where how much result to omit from the beginning.
     * @return an observable of json object that contains all the resulted projects merged with their categories and with id field added.
     */

    public static Observable<JsonObject> getFeaturedProjets(int offset,int limit){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        logger.info ("DB: bulk getting featured projects with limit: {} and offset: {}",limit,offset);

        return mBucket.query (N1qlQuery.simple (select(Expression.x ("meta(project).id, *")).from (Expression.x (DBConfig.BUCKET_NAME + " project"))
                .join (Expression.x (DBConfig.BUCKET_NAME + " category")).onKeys (Expression.x ("project.category_id"))
                .where(Expression.x ("project.is_featured")).orderBy (Sort.desc (Expression.x ("project.enrollments_count")))
                .limit (limit).offset (offset)))
                .timeout (1000,TimeUnit.MILLISECONDS)
                .flatMap (AsyncN1qlQueryResult::rows).flatMap (queryRow -> DBConfig.embedIdAndCategoryIntoProject (queryRow.value ().getString ("id"), queryRow))
                .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                        .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
                .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                        .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
                .onErrorResumeNext (throwable -> {
                    logger.info ("DB: failed to bulk get featured projects with limit: {} and offset: {}",limit,offset);

                    return Observable.error (new CouchbaseException (String.format ("DB: failed to bulk get featured projects with limit: $1 and offset: $2",limit,offset)));
                })
                .defaultIfEmpty (JsonObject.create ().put ("id",EMPTY_JSON_DOC));
    }

    /**
     * Bulk gets projects that have a specific category_id sorts them by popularity and sets a limit and offset for the results.
     * @param categoryId The category id to get projects for.
     * @param limit the maximum number of document returned.
     * @param offset an index to determine where how much result to omit from the beginning.
     * @return An observable of json object that contains all the resulted projects merged with their categories and with id field added.
     */
    public static Observable<JsonObject> getProjectWithSpecificCategory(String categoryId, int offset, int limit){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        logger.info ("DB: Bulk getting projects with category_id: {} ,limit: {} and offset: {}",categoryId,limit,offset);

        return mBucket.query (N1qlQuery.simple (select(Expression.x ("meta(project).id, *")).from (Expression.x (DBConfig.BUCKET_NAME + " project"))
                .join (Expression.x (DBConfig.BUCKET_NAME + " category")).onKeys (Expression.x ("project.category_id"))
                .where (Expression.x ("project.category_id").eq (Expression.s (categoryId)))
                .orderBy (Sort.desc (Expression.x ("project.enrollments_count"))).limit (limit).offset (offset)))
                .timeout (1000,TimeUnit.MILLISECONDS)
                .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> DBConfig.embedIdAndCategoryIntoProject (row.value ().getString ("id"),row))
                .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                        .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
                .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                        .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
                .onErrorResumeNext (throwable -> {
                    logger.info ("DB: failed to bulk get projects with category_id: {} ,limit: {} and offset: {}",categoryId,limit,offset);

                    return Observable.error (new CouchbaseException (String.format ("DB: failed to bulk get projects with category_id: $1 ,limit: $2 and offset: $3",categoryId,limit,offset)));
                })
                .defaultIfEmpty (JsonObject.create ().put ("id",EMPTY_JSON_DOC));

    }

    /**
     * Searches for projects with name containing the provided string sorts them by popularity and sets a limit and offset for the results.
     * @param searchText the String to look for in the projects name.
     * @param limit the maximum number of document returned.
     * @param offset an index to determine where how much result to omit from the beginning.
     * @return an observable of json object that contains all the resulted projects merged with their categories and with id field added.
     */

    public static Observable<JsonObject> searchForProjectsByName(String searchText,int offset,int limit){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }
        logger.info ("DB: Searching for projects with name containing: {} with limit: {} and offset: {}",searchText,limit,offset);

        return mBucket.query (N1qlQuery.simple (select(Expression.x ("meta(project).id, *")).from (Expression.x (DBConfig.BUCKET_NAME + " project"))
            .join (Expression.x (DBConfig.BUCKET_NAME + " category")).onKeys (Expression.x ("project.category_id"))
            .where(Expression.x ("project.name").like (Expression.s ("%" + searchText + "%")))
            .orderBy (Sort.desc (Expression.x ("project.enrollments_count"))).limit (limit).offset (offset)))
            .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> DBConfig.embedIdAndCategoryIntoProject (row.value ().getString ("id"),row))
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                logger.info ("DB: failed to search for projects with name containing: {} with limit: {} and offset: {}",searchText,limit,offset);

                return Observable.error (new CouchbaseException (String.format ("DB: failed to search for projects with name containing: $1 with limit: $2 and offset: {}",searchText,limit,offset)));
            })
            .defaultIfEmpty (JsonObject.create ());

    }

    public static Observable<JsonObject> getProjectName(String projectId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        Logger.info ("DB: Getting project name for project with ID: {}",projectId);

        return mBucket.query (N1qlQuery.simple (select (Expression.x ("name")).from (DBConfig.BUCKET_NAME)
        .useKeys (Expression.s (projectId)))).timeout (1000,TimeUnit.MILLISECONDS)
        .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
        .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
        .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
        .onErrorResumeNext (throwable -> {

            logger.info ("DB: Failed to Get project name for project with ID: {}, General DB exception",projectId);

            return Observable.error (new CouchbaseException (String.format ("Failed to get project naem for project  with ID: $1, General DB exception",projectId)));
        }).defaultIfEmpty (JsonObject.create ().put ("id",DBConfig.EMPTY_JSON_DOC));

    }

    /**
     * Adds 1 to the contributions count of the project with the provided ID.
     * @param projectId The ID of the project to update.
     * @return An observable of Json object containing the project id and the new contributions count.
     */
    public static Observable<JsonObject> add1ToProjectContributionCount(String projectId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        Logger.info ("DB: Adding 1 to contributions count of project with id: {}",projectId);

        return mBucket.query (N1qlQuery.simple (update (Expression.x (DBConfig.BUCKET_NAME + " project")).useKeys (Expression.s (projectId))
        .set ("contributions_count",Expression.x ("contributions_count + " + 1 ))
        .returning (Expression.x ("contributions_count, meta(project).id"))))
        .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
        .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
        .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
        .onErrorResumeNext (throwable -> {
            if (throwable instanceof CASMismatchException){
                //// TODO: 4/1/16 needs more accurate handling in the future.
                logger.info ("DB: Failed to add 1 to contributions count of project with id: {}",projectId);

                return Observable.error (new CASMismatchException (String.format ("DB: Failed to add 1 to contributions count of project with id: $1, General DB exception.",projectId)));
            } else {
                logger.info ("DB: Failed to add 1 to contributions count of project with id: {}",projectId);

                return Observable.error (new CouchbaseException (String.format ("DB: Failed to add 1 to contributions count of project with id: $1, General DB exception.",projectId)));
            }
        }).defaultIfEmpty (JsonObject.create ().put ("id",DBConfig.EMPTY_JSON_DOC));
    }


    public static Observable<JsonObject> add1ToProjectEnrollmentsCount(String projectId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        Logger.info ("DB: Adding 1 to contributions count of project with id: {}",projectId);

        return mBucket.query (N1qlQuery.simple (update (Expression.x (DBConfig.BUCKET_NAME + " project")).useKeys (Expression.s (projectId))
        .set ("enrollments_count",Expression.x ("enrollments_count + " + 1 ))
        .returning (Expression.x ("enrollments_count, meta(project).id"))))
        .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
        .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
        .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
        .onErrorResumeNext (throwable -> {
            if (throwable instanceof CASMismatchException){
                //// TODO: 4/1/16 needs more accurate handling in the future.
                logger.info ("DB: Failed to add 1 to enrollments count of project with id: {}",projectId);

                return Observable.error (new CASMismatchException (String.format ("DB: Failed to add 1 to enrollments count of project with id: $1, General DB exception.",projectId)));
            } else {
                logger.info ("DB: Failed to add 1 to enrollments count of project with id: {}",projectId);

                return Observable.error (new CouchbaseException (String.format ("DB: Failed to add 1 to enrollments count of project with id: $1, General DB exception.",projectId)));
            }
        }).defaultIfEmpty (JsonObject.create ().put ("id",DBConfig.EMPTY_JSON_DOC));
    }

    public static Observable<JsonObject> remove1FromProjectEnrollmentsCount(String projectId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        Logger.info ("DB: Removing 1 from contributions count of project with id: {}",projectId);

        return mBucket.query (N1qlQuery.simple (update (Expression.x (DBConfig.BUCKET_NAME + " project")).useKeys (Expression.s (projectId))
        .set ("enrollments_count",Expression.x ("enrollments_count - " + 1 ))
        .returning (Expression.x ("enrollments_count, meta(project).id"))))
        .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
        .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
        .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
        .onErrorResumeNext (throwable -> {
            if (throwable instanceof CASMismatchException){
                //// TODO: 4/1/16 needs more accurate handling in the future.
                logger.info ("DB: Failed to remove 1 from enrollments count of project with id: {}",projectId);

                return Observable.error (new CASMismatchException (String.format ("DB: Failed to remove 1 from enrollments count of project with id: $1, General DB exception.",projectId)));
            } else {
                logger.info ("DB: Failed to remove 1 from enrollments count of project with id: {}",projectId);

                return Observable.error (new CouchbaseException (String.format ("DB: Failed to remove 1 from enrollments count of project with id: $1, General DB exception.",projectId)));
            }
        }).defaultIfEmpty (JsonObject.create ().put ("id",DBConfig.EMPTY_JSON_DOC));
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
