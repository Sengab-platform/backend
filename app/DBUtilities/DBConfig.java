package DBUtilities;

import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.json.JsonObject;

import java.util.concurrent.TimeUnit;


public class DBConfig {

    public static final int OPEN_BUCKET_OK = 0;
    public static final int OPEN_BUCKET_ERROR = -1;
    private static final String ID_JSON_KEY = "id";

    private static Cluster cluster;
    static AsyncBucket bucket;

    public static int initDB(){
        if (bucket != null && !bucket.isClosed ()){
            return OPEN_BUCKET_OK;
        }
        cluster = CouchbaseCluster.create ();
        try{
            bucket = cluster.openBucket (1, TimeUnit.SECONDS).async ();
            return OPEN_BUCKET_OK;
        }catch (CouchbaseException e){
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
