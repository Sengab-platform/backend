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
import com.couchbase.client.java.query.dsl.Sort;
import com.couchbase.client.java.util.retry.RetryBuilder;
import play.Logger;
import rx.Observable;

import java.util.concurrent.TimeUnit;

import static DBUtilities.DBConfig.bucket;
import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.Update.update;
import static com.couchbase.client.java.query.dsl.functions.ArrayFunctions.arrayContains;
import static com.couchbase.client.java.query.dsl.functions.ArrayFunctions.arrayPut;
import static com.couchbase.client.java.query.dsl.functions.ArrayFunctions.arrayRemove;

/**
 * Created by rashwan on 3/28/16.
 */
public class User {
    private static AsyncBucket mBucket;
    private static final Logger.ALogger logger = Logger.of (User.class.getSimpleName ());

    /**
     * Create and save a user. can error with {@link CouchbaseException},{@link DocumentAlreadyExistsException} and {@link BucketClosedException}.
     * @param userJsonObject The Json object to be the value of the document , it also has an Id field to use as the document key.
     * @return an observable of the created Json document.
     */
    public static Observable<JsonObject> createUser(JsonObject userJsonObject){
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
            }).flatMap (jsonDocument -> Observable.just (jsonDocument.content ().put ("id",jsonDocument.id ())));
    }
    /**
     * Get a user using its id. can error with {@link CouchbaseException} and {@link BucketClosedException}.
     * @param userId the id of the user to get.
     * @return an observable of the json document if it was found , if it wasn't found it returns an empty json document with id DBConfig.EMPTY_JSON_DOC .
     */
    public static Observable<JsonObject> getUserWithId(String userId){
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
            .defaultIfEmpty (JsonDocument.create (DBConfig.EMPTY_JSON_DOC,JsonObject.create ()))
            .flatMap (jsonDocument -> Observable.just (jsonDocument.content ().put ("id",jsonDocument.id ())));
    }

    /**
     * Bulk gets enrolled projects for the provided user id, sorts them by name and sets a limit and offset for the results.
     * @param userId The user id to get enrolled projects for.
     * @param offset an index to determine where how much result to omit from the beginning.
     * @param limit the maximum number of document returned.
     * @return an observable of json object that contains all the resulted projects merged with their categories and with id field added.
     */
    public static Observable<JsonObject> getEnrolledProjectsForUser(String userId,int offset, int limit){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }
        logger.info (String.format ("DB: Bulk getting enrolled projects for user with id: $1 with offset: $2 and limit: $3", userId,offset,limit));

        return mBucket.query (N1qlQuery.simple (select(Expression.x ("meta(project).id,project, category"))
            .from (Expression.x (DBConfig.BUCKET_NAME + " aUser")).useKeys (Expression.s (userId))
            .join (Expression.x (DBConfig.BUCKET_NAME + " project")).onKeys (Expression.x ("aUser.enrolled_projects"))
            .join (Expression.x (DBConfig.BUCKET_NAME + " category")).onKeys (Expression.x ("project.category_id"))
            .orderBy (Sort.asc (Expression.x ("project.name"))).limit (limit).offset (offset)))
            .timeout (1000,TimeUnit.MILLISECONDS)
            .flatMap (AsyncN1qlQueryResult::rows)
            .flatMap (row -> DBConfig.embedIdAndCategoryIntoProject (row.value ().getString ("id"),row))
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                logger.info (String.format ("DB: Failed to Bulk get enrolled projects for user with id: $1 with offset: $2 and limit: $3",userId,limit,offset));

                return Observable.error (new CouchbaseException (String.format ("DB: Failed to Bulk get enrolled projects for user with id: $1 with offset: $2 and limit: $3, general DB exception.",userId,offset,limit)));
            })
            .defaultIfEmpty (JsonObject.create ().put ("id",DBConfig.EMPTY_JSON_DOC));
    }

    /**
     * Bulk gets created projects for the provided user id, sorts them by name and sets a limit and offset for the results.
     * @param userId The user id to get created projects for.
     * @param offset an index to determine where how much result to omit from the beginning.
     * @param limit the maximum number of document returned.
     * @return an observable of json object that contains all the resulted projects merged with their categories and with id field added.
     */

    public static Observable<JsonObject> getProjectsCreatedByUser(String userId,int offset,int limit){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }
        logger.info (String.format ("DB: Bulk getting created projects for user with id: $1 with offset: $2 and limit: $3", userId,offset,limit));

        return mBucket.query (N1qlQuery.simple (select(Expression.x ("meta(project).id, *")).from (Expression.x (DBConfig.BUCKET_NAME + " project"))
            .join (Expression.x (DBConfig.BUCKET_NAME + " category")).onKeys (Expression.x ("project.category_id"))
            .where (Expression.x ("project.owner.id").eq (Expression.s (userId))).orderBy (Sort.desc (Expression.x ("project.name")))
            .limit (limit).offset (offset)))
            .timeout (1000,TimeUnit.MILLISECONDS)
            .flatMap (AsyncN1qlQueryResult::rows)
            .flatMap (row -> DBConfig.embedIdAndCategoryIntoProject (row.value ().getString ("id"),row))
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                logger.info (String.format ("DB: Failed to Bulk get created projects for user with id: $1 with offset: $2 and limit: $3",userId,limit,offset));

                return Observable.error (new CouchbaseException (String.format ("DB: Failed to Bulk get created projects for user with id: $1 with offset: $2 and limit: $3, general DB exception.",userId,offset,limit)));
            })
            .defaultIfEmpty (JsonObject.create ().put ("id",DBConfig.EMPTY_JSON_DOC));
    }

    /**
     * Receives updated user profile data from the provider when signing in and update the user profile without affecting the rest of the profile's data. can error with {@link CouchbaseException},{@link DocumentDoesNotExistException},{@link CASMismatchException} and {@link BucketClosedException} .
     * @param userId the id of the user to update
     * @param firstName the updated first name of the user.
     * @param lastName the updated last name of the user.
     * @param imageURL the updated image name of the user.
     * @param about the updated about of the user.
     * @return an Observable of AsyncN1qlQueryRow containing the id of the updated user.
     */
    public static Observable<AsyncN1qlQueryRow> updateSigningInUser(String userId, String firstName, String lastName, String imageURL, JsonObject about){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }
        logger.info (String.format ("DB: Partial updating user with ID: $1", userId));


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
                    logger.info (String.format ("DB: Failed to partial update user with ID: $1, no user exists with this id.",userId));

                    return Observable.error (new DocumentDoesNotExistException (String.format ("DB: Failed to partial update user with ID: $1, no user exists with this id.",userId)));

                }else if (throwable instanceof CASMismatchException){
                    //// TODO: 4/1/16 needs more accurate handling in the future.
                    logger.info (String.format ("DB: Failed to partial update user with ID: $1, CAS value is changed.",userId));

                    return Observable.error (new CASMismatchException (String.format ("DB: Failed to partial update user with ID: $1, CAS value is changed.",userId)));
                } else {
                    logger.info (String.format ("DB: Failed to partial update user with ID: $1, General DB exception.",userId));

                    return Observable.error (new CouchbaseException (String.format ("Failed to partial update user with ID: $1 , General DB exception.",userId)));
                }
            });
    }

    /**
     * Adds 1 to the contributions count of the user with the provided ID.
     * @param userId The ID of the user to update.
     * @return An observable of Json object containing the user id and the new contributions count.
     */
    public static Observable<JsonObject> add1ToUserContributionCount(String userId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        logger.info (String.format ("DB: Adding 1 to contributions count of user with id: $1",userId));

        return mBucket.query (N1qlQuery.simple (update (Expression.x (DBConfig.BUCKET_NAME + " aUser")).useKeys (Expression.s (userId))
        .set ("stats.contributions",Expression.x ("stats.contributions + " + 1 ))
        .returning (Expression.x ("stats.contributions, meta(aUser).id"))))
        .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
        .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
        .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
        .onErrorResumeNext (throwable -> {
            if (throwable instanceof CASMismatchException){
                //// TODO: 4/1/16 needs more accurate handling in the future.
                logger.info (String.format ("DB: Failed to add 1 to contributions count of user with id: $1",userId));

                return Observable.error (new CASMismatchException (String.format ("DB: Failed to add 1 to contributions count of user with id: $1, General DB exception.",userId)));
            } else {
                logger.info (String.format ("DB: Failed to add 1 to contributions count of user with id: $1",userId));

                return Observable.error (new CouchbaseException (String.format ("DB: Failed to add 1 to contributions count of user with id: $1, General DB exception.",userId)));
            }
      }).defaultIfEmpty (JsonObject.create ().put ("id",DBConfig.EMPTY_JSON_DOC));
    }

    /**
     * Adds a project with the provided ID to the user's enrolled projects list.
     * @param userId The ID for the user to add the project to.
     * @param projectId The ID for the project to add to the user's enrolled projects.
     * @return An observable of Json object that contains the ID of the user and the added project ID, if the operation succeeds.
     */
    public static Observable<JsonObject> addProjectToEnrolledProjects(String userId,String projectId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        logger.info (String.format ("DB: Adding project with ID: $1 to enrolled projects for user with id: $2",projectId,userId));


        return mBucket.query (N1qlQuery.simple (select(Expression.x ("meta(aUser).id")).from (Expression.x (DBConfig.BUCKET_NAME + " aUser"))
            .useKeys (Expression.s (userId))
            .where (arrayContains (Expression.x ("enrolled_projects"),Expression.s (projectId)))))
            .flatMap (result -> result.rows ().isEmpty ())
            .flatMap (isEmpty -> {
                if (!isEmpty){
                    logger.info (String.format ("DB: User with ID: $1 is already enrolled in project with ID: $2",userId,projectId));

                    return Observable.just (JsonObject.create ().put ("id",DBConfig.ALREADY_ENROLLED));
                }else {
                    return mBucket.query (N1qlQuery.simple (update(Expression.x (DBConfig.BUCKET_NAME + " project"))
                    .useKeys (Expression.s (userId)).set (Expression.x ("enrolled_projects"),
                            arrayPut (Expression.x ("enrolled_projects"),Expression.s (projectId)))
                    .returning (Expression.x ("enrolled_projects[-1] as project,meta(project).id"))))
                    .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()));
                }})
            .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                    .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
            .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                    .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
            .onErrorResumeNext (throwable -> {
                if (throwable instanceof CASMismatchException){
                    //// TODO: 4/1/16 needs more accurate handling in the future.
                    logger.info (String.format ("DB: Failed to add project with ID: $1 to enrolled projects for user with id: $2",projectId,userId));

                    return Observable.error (new CASMismatchException (String.format ("DB: Failed to add project with ID: $1 to enrolled projects for user with id: $2, General DB exception.",projectId,userId)));
                } else {
                    logger.info (String.format ("DB: Failed to add project with ID: $1 to enrolled projects for user with id: $2",projectId,userId));

                    return Observable.error (new CouchbaseException (String.format ("DB: Failed to add project with ID: $1 to enrolled projects for user with id: $2, General DB exception.",projectId,userId)));
                }
            }).defaultIfEmpty (JsonObject.create ().put ("id",DBConfig.EMPTY_JSON_DOC));

    }

    /**
     * Removes a project with the provided ID from the user's enrolled projects list.
     * @param userId The ID for the user to remove the project from.
     * @param projectId The ID for the project to remove from the user's enrolled projects.
     * @return An observable of Json object that contains the ID of the user, if the operation succeeds.
     */
    public static Observable<JsonObject> removeProjectFromEnrolledProjects(String userId,String projectId){
        try {
            checkDBStatus();
        } catch (BucketClosedException e) {
            return Observable.error(e);
        }

        logger.info (String.format ("DB: Removing project with ID: $1 to enrolled projects for user with id: $2",userId,projectId));

        return mBucket.query (N1qlQuery.simple (update(Expression.x (DBConfig.BUCKET_NAME + " aUser"))
        .useKeys (Expression.s (userId)).set (Expression.x ("enrolled_projects"),
                arrayRemove (Expression.x ("enrolled_projects"),Expression.s (projectId)))
        .returning (Expression.x ("meta(aUser).id," + Expression.s (projectId) + "as projectId"))))
        .timeout (1000,TimeUnit.MILLISECONDS)
        .flatMap (AsyncN1qlQueryResult::rows).flatMap (row -> Observable.just (row.value ()))
        .retryWhen (RetryBuilder.anyOf (TemporaryFailureException.class, BackpressureException.class)
                .delay (Delay.fixed (200, TimeUnit.MILLISECONDS)).max (3).build ())
        .retryWhen (RetryBuilder.anyOf (TimeoutException.class)
                .delay (Delay.fixed (500,TimeUnit.MILLISECONDS)).once ().build ())
        .onErrorResumeNext (throwable -> {
            if (throwable instanceof CASMismatchException){
                //// TODO: 4/1/16 needs more accurate handling in the future.
                logger.info (String.format ("DB: Failed to remove project with ID: $1 to enrolled projects for user with id: $2",userId,projectId));

                return Observable.error (new CASMismatchException (String.format ("DB: Failed to remove project with ID: $1 to enrolled projects for user with id: $2, General DB exception.",userId,projectId)));
            } else {
                logger.info (String.format ("DB: Failed to remove project with ID: $1 to enrolled projects for user with id: $2",userId,projectId));

                return Observable.error (new CouchbaseException (String.format ("DB: Failed to remove project with ID: $1 to enrolled projects for user with id: $2, General DB exception.",userId,projectId)));
            }
        }).defaultIfEmpty (JsonObject.create ().put ("id",DBConfig.EMPTY_JSON_DOC));

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
