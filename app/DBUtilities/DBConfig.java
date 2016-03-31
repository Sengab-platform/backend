package DBUtilities;

import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.json.JsonObject;
import play.Logger;


public class DBConfig {

    public static final int OPEN_BUCKET_OK = 0;
    public static final int OPEN_BUCKET_ERROR = -1;
    public static final String EMPTY_JSON_DOC = "empty_doc";
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

     static String getIdFromJson(JsonObject object){
        return object.getString (ID_JSON_KEY);
    }

     static JsonObject removeIdFromJson(JsonObject object){
        return  object.removeKey (ID_JSON_KEY);
    }
}
