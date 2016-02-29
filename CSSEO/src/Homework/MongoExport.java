package Homework;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ParallelScanOptions;
import com.mongodb.ServerAddress;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Element;

public class MongoExport {
	
	public static void main(String[] args) throws IOException
	{
		
		
		MongoClient mongoClient = new MongoClient( "localhost" , 27017);
		DB db = mongoClient.getDB( "mydb" );
		
		JSONArray records = new JSONArray();

		JSONObject jsonobj;
		DBCursor cursor = db.getCollection("Crawled_URL").find();

		  while(cursor.hasNext()) {
		    BasicDBObject obj = (BasicDBObject) cursor.next();
		    jsonobj = new JSONObject();
		    BasicDBObject images = (BasicDBObject) obj.get("images");
		    jsonobj.put("Base_URL", obj.getString("Base_URL"));
		    jsonobj.put("title", obj.getString("title"));
		    jsonobj.put("html_size", obj.getString("html_size"));
		    jsonobj.put("Last_modified", obj.getString("Last_modified"));
		    jsonobj.put("Extracted_Date", obj.getString("Extracted_Date"));
		    jsonobj.put("Page_html", obj.getString("Page_html"));
		    jsonobj.put("images", obj.getString("images"));
		    jsonobj.put("outgoing_Links", obj.getString("outgoing_Links"));
		    jsonobj.put("Meta_data", obj.getString("Meta_data"));
		    records.put(jsonobj);
		  //  System.out.println("Data :" +jsonobj);
		  }
		  
		  File file = new File("data_dump.json");
      				
			BufferedWriter bw1 = new BufferedWriter(new FileWriter(file));
			
			bw1.write(records.toString());
			
			bw1.close();
			System.out.println("Data is dumped!!!");

	}
	

}
