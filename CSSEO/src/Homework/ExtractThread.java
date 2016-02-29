package Homework;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

	public ExtractThread(File filePath) throws UnknownHostException
	   {
	      this.mongoClient = new MongoClient( "localhost" , 27017);;
	      this.filePath = filePath;
	   }

	   @Override
	   public void run()
	   {
		   try{
			   System.out.println("extracting: "+filePath);
				 Document doc = Jsoup.parse(filePath, "UTF-8");
				 Elements linksOnPage = doc.select("a[href]");
				 Elements metas = doc.select("META");
				 Element url = doc.select("url").get(0);
				
				
      	HttpURLConnection content = (HttpURLConnection) new URL(url.html()).openConnection();        
			
    
      	Date Last_update_Date= new Date(content.getLastModified());
      	
			
          Elements media = doc.select("[src]");
         
      	Connection connection = Jsoup.connect(url.html());
			Document htmlDocument = connection
					.ignoreContentType(true)
					.get();
         
         
			DB db = mongoClient.getDB( "mydb" );
			
			DBCollection collection1=db.getCollection("Crawled_URL");
		
			BasicDBObject document = new BasicDBObject();
			document.put("Base_URL",url.html());
			document.put("title", doc.title().toString());
			document.put("html_size",(float)content.getContentLength());
			document.put("Last_modified",Last_update_Date);
			document.put("Extracted_Date",new Date());
			document.put("Page_html",htmlDocument.html() );
			BasicDBObject documenturl = new BasicDBObject();
			int l=0;
			for(Element u : linksOnPage )
			{
				if(u.absUrl("href")!="")	{
				documenturl.put("url"+l,u.absUrl("href").toString());
				
				l++;}
			}
			document.put("outgoing_Links", documenturl);
			BasicDBObject documenturl1 = new BasicDBObject();
			int m=0;
			for(Element u : metas )
			{
				if(u.attr("name")!="")	{
				documenturl1.put(u.attr("name"),u.attr("content").toString());
				
				l++;}
			}
			document.put("Meta_data", documenturl1);
			
			BasicDBObject document1 = new BasicDBObject();

			
          for (Element src : media) {
              if (src.tagName().equals("img"))
              {
              	if(src.attr("abs:src").contains(url.html())){
              		BasicDBObject document2 = new BasicDBObject();
              
              	
                  HttpURLConnection content1 = (HttpURLConnection) new URL(src.attr("abs:src")).openConnection();
              
              	document2.put("image_url",src.attr("abs:src") );
              	document2.put("image_size",(float)content1.getContentLength() );
              	document2.put("image_alt",src.attr("alt").trim()) ;
              	document1.put("image", document2);
                }                	
              }
      	}
          document.put("images",document1);
          
          BasicDBObject query = new BasicDBObject("Base_URL",url.html());
          
          collection1.update(query,document,true,false);
  							
			}
			catch(Exception e)
			{
				System.out.println(e);
				/*System.out.println("url: "+url);
				System.out.println("exception: "+e.getMessage());*/
			} 
		   finally
		   {
			   mongoClient.close();
		   }
	   }

}

