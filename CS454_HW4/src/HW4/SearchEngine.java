package HW4;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.*;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class SearchEngine {
	
	public String search(String text) throws UnknownHostException, JSONException{
		
		MongoClient mongoclient= new MongoClient();
		mongoclient=new MongoClient("localhost" , 27017);
		DB db = mongoclient.getDB( "mydb" );		
		
		DBCollection docCollection=db.getCollection("SE_Words");
		
		BasicDBObject query = new BasicDBObject("text", text);
		
		DBCursor cursor = docCollection.find(query);	
		
		return "testing";
	}
	
	
}
