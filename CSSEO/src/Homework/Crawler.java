package Homework;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.tika.exception.TikaException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.xml.sax.SAXException;
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

import java.util.List;
import java.util.Set;


import static java.util.concurrent.TimeUnit.SECONDS;



public class Crawler   {
	
	String mainFolder = "C:/BHANU/CSULA/cs454 hw/CSSEO/Downloads/";
	String currentfolder = ""
			+Calendar.getInstance().get(Calendar.YEAR)+""
			+Calendar.getInstance().get(Calendar.MONTH)+""
			+Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+""
			+Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+""
			+Calendar.getInstance().get(Calendar.MINUTE)+""
			+Calendar.getInstance().get(Calendar.SECOND);

	int maxDepth = 4;
	
	boolean isExtractorEnable = false;
	
	ArrayList<String> Links= new ArrayList<String>();	
	
	public static void main(String[] args) throws IOException, SAXException, TikaException
	{
		Crawler crawler= new Crawler();
		
		File folder = new File(crawler.mainFolder+"\\"+crawler.currentfolder);
		if(!folder.exists())
			folder.mkdir();
		
		String crawlURL ="";
		
		int i =0;
		
		for(String arg : args)
		{
			if(arg.contentEquals("-d"))
				crawler.maxDepth = Integer.parseInt(args[i+1]); 				
			if(arg.contentEquals("-u"))
				crawlURL=args[i+1];
			if(arg.contentEquals("-e"))
				crawler.isExtractorEnable = true;			
			i++;
		}			
		crawler.crawl(crawlURL,0,0);
		crawler.extracter();
	}
	
	public void crawl(String url,int depth,int iteration) throws IOException
	{		
		if(depth>maxDepth)
			return;
		if(Links.contains(url))
			return;
		if(url.isEmpty())
		{
			System.out.println("invalid:" +url );
			return;
		}
		
		System.out.println("Starting for depth " + depth+" and iteration "+iteration);
		System.out.println(url);
		
		try{
			Connection connection = Jsoup.connect(url);
			Document htmlDocument = connection
					.ignoreContentType(true)
					.get();
			htmlDocument.select("head").first().children().first()
					.before("<url>"+url.toString()+"</url>");
			
			File filename = new File(mainFolder+"\\"+currentfolder+"\\file_"+depth+"_"+iteration+".html");

			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			bw.write(htmlDocument.html());
			bw.close(); 
			
			Elements linksOnPage = htmlDocument.select("a[href]");
			
			Links.add(url);
			int i =0;
			for(Element link : linksOnPage)
	    	{	
	        	crawl(link.absUrl("href"),depth+1,i);
	        	i++;
	    	}
		}
		catch(Exception e)
		{
			System.out.println("url: "+url);
			System.out.println("exception: "+e.getMessage());
		}
		
/*		 Document doc = Jsoup.parse(filename, "UTF-8");
		 Elements url = doc.select("url");
		 for(Element e : url)
		 {System.out.println("New Url ::"+e.html());}*/
	}
	
	
	public void extracter() throws MalformedURLException, IOException
	{
		if(!isExtractorEnable)
		{
			System.out.println("Extraction is not enabled");
			return;
		}
		
		File[] folder = new File (mainFolder+"\\"+currentfolder).listFiles();
		MongoClient mongoClient = new MongoClient( "localhost" , 27017);
		for(File file : folder ){


					   System.out.println("extracting: "+file.toString());
						 Document doc = Jsoup.parse(file, "UTF-8");
						 Elements urls = doc.select("url");
						 Element url = null;
						 for(Element eurl:urls)
						 {url=eurl;}
						
						
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
	}
		

		
}
