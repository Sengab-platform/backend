package DBUtilities;

import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.AsyncN1qlQueryRow;
import play.Logger;
import rx.Observable;


public class DBConfig {

    public static final int OPEN_BUCKET_OK = 0;
    public static final int OPEN_BUCKET_ERROR = -1;
    public static final String EMPTY_JSON_OBJECT = "empty_object";
    public static final String ALREADY_ENROLLED = "already_enrolled";
    public static final String NOT_ENROLLED = "not_enrolled";
    public static final String ALREADY_CONTRIBUTED = "already_contributed";
    public static final String WRONG_TEMPLATE_NUMBER = "wrong_template_number";
    public static final String BUCKET_NAME = "default";
    private static final String ID_JSON_KEY = "id";
    public static AsyncBucket bucket;
    private static Cluster cluster;

    /**
     * Initialize the Couchbase cluster and open the app's bucket.
     * @return DBConfig.OPEN_BUCKET_OK . if it succeeds and DBConfig.OPEN_BUCKET_ERROR} if it fails.
     */
    public static int initDB(){
        Logger.info ("DB: Trying to initialize DB");
        if (bucket != null && !bucket.isClosed ()){
            Logger.info ("DB: DB already initialized");
            return OPEN_BUCKET_OK;
        }
        try{
            cluster = CouchbaseCluster.create();
            bucket = cluster.openBucket().async();
            Logger.info ("DB: DB initialized");
            return OPEN_BUCKET_OK;
        }catch (CouchbaseException e){
            Logger.info ("DB: DB to initialize");
            return OPEN_BUCKET_ERROR;
        } catch (Exception e) {
            Logger.info ("DB: DB failed to initialize");
            return OPEN_BUCKET_ERROR;
        }
    }

    public static Observable<JsonObject> embedIdAndCategoryIntoProject (String projectId, AsyncN1qlQueryRow queryRow) {
        String categoryId = queryRow.value ().getObject ("project").getString ("category_id");
        JsonObject categoryObject = queryRow.value ().getObject ("category");
        JsonObject userCategoryObject = JsonObject.create ()
                .put ("name",categoryObject.getString ("name")).put ("category_id",categoryId);
        JsonObject projectObject = queryRow.value ().getObject ("project").removeKey ("category_id");
        return Observable.just (projectObject.put ("id",projectId).put ("category",userCategoryObject));
    }

    static String stripIdFromPrefix (String id){
        return id.split("::")[1];
    }
     static String getIdFromJson(JsonObject object){
        return object.getString (ID_JSON_KEY);
    }

     static JsonObject removeIdFromJson(JsonObject object){
        return  object.removeKey (ID_JSON_KEY);
    }
}
