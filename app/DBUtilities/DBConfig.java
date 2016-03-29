package DBUtilities;

import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.json.JsonObject;


public class DBConfig {

    public static final int OPEN_BUCKET_OK = 0;
    public static final int OPEN_BUCKET_ERROR = -1;
    public static final String EMPTY_JSON_DOC = "empty_doc";
    private static final String ID_JSON_KEY = "id";
    public static AsyncBucket bucket;
    private static Cluster cluster;

    /**
     * Initialize the Couchbase cluster and open the app's bucket.
     * @return DBConfig.OPEN_BUCKET_OK . if it succeeds and DBConfig.OPEN_BUCKET_ERROR} if it fails.
     */
    public static int initDB(){
        if (bucket != null && !bucket.isClosed ()){
            return OPEN_BUCKET_OK;
        }
        try{
            cluster = CouchbaseCluster.create();
            bucket = cluster.openBucket().async();
            return OPEN_BUCKET_OK;
        }catch (CouchbaseException e){
            return OPEN_BUCKET_ERROR;
        } catch (Exception e) {
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
