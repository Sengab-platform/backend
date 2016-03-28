package DBUtilities;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.json.JsonObject;


public class DBConfig {

    private static final int OPEN_BUCKET_OK = 0;
    private static final int OPEN_BUCKET_ERROR = -1;
    private static final String ID_JSON_KEY = "id";

    private static Cluster cluster;
    static AsyncBucket bucket;

    public static int initDB(){
        if (bucket != null){
            return OPEN_BUCKET_OK;
        }
        cluster = CouchbaseCluster.create ();
        bucket = cluster.openBucket ().async ();
        if (bucket != null){
            return OPEN_BUCKET_OK;
        }else {
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
