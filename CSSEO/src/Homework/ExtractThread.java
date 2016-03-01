package Homework;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class ExtractThread extends Thread
{
	MongoClient mongoClient;
	File filePath;
	String uuid;

	public ExtractThread(MongoClient mongoClient, File filePath , String uuid) throws UnknownHostException
   {
      this.mongoClient = mongoClient;
      this.filePath = filePath;
      this.uuid=uuid;
   }

   @Override
   public void run()
   {
	   try{
		   	System.out.println("extracting: "+filePath);
		  
			 Document doc = Jsoup.parse(filePath, "UTF-8");
			 Elements linksOnPage = doc.select("a[href]");
			 Elements metas = doc.select("META");
			 Elements media = doc.select("[src]");
         
			DB db = mongoClient.getDB( "mydb" );
			
			DBCollection collection1=db.getCollection("Extracted_URL");
		
			BasicDBObject document = new BasicDBObject();
			//document.put("Base_URL",url.html());
			document.put("title", doc.title().toString());
		//	document.put("html_size",(float)content.getContentLength());
		//	document.put("Last_modified",Last_update_Date);
			document.put("Extracted_Date",new Date());
		//	document.put("Page_html",htmlDocument.html() );
			document.put("Page_Location",filePath.getAbsolutePath() );
			document.put("Uuid",uuid );
			BasicDBObject documenturl = new BasicDBObject();
			int l=0;
			for(Element u : linksOnPage )
			{
				if(u.absUrl("href")!=""){
					documenturl.put("url"+l,u.absUrl("href").toString());				
					l++;
				}
			}
			document.put("outgoing_Links", documenturl);
			BasicDBObject documenturl1 = new BasicDBObject();
			documenturl1.put(metas.first().attr("name"), metas.first().attr("content").toString());
			document.put("Meta_data", documenturl1);
		   
			BasicDBObject query = new BasicDBObject("Page_Location",filePath.getAbsolutePath());
          
			collection1.update(query,document,true,false);
  							
			}
			catch(Exception e)
			{
				System.out.println(e);
				/*System.out.println("url: "+url);
				System.out.println("exception: "+e.getMessage());*/
			} 
		  
	   
		   /*try {
			   indexing index=new indexing();			   
			   index.StopWords(filePath,uuid);
		   } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		   }*/
	   }

}

