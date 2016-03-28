package DBUtilities;

import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.json.JsonObject;


public class DBConfig {

    public static final int OPEN_BUCKET_OK = 0;
    public static final int OPEN_BUCKET_ERROR = -1;
    private static final String ID_JSON_KEY = "id";
    public static AsyncBucket bucket;
    private static Cluster cluster;

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

    public static String getIdFromJson(JsonObject object){
        return object.getString (ID_JSON_KEY);
    }

    public static JsonObject removeIdFromJson(JsonObject object){
        return  object.removeKey (ID_JSON_KEY);
    }
}
