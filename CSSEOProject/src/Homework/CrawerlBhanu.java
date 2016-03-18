package Homework;

//import Homework.CrawlThread;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
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


public class CrawerlBhanu   {
	static String currentfolder = (new SimpleDateFormat("yyyy-MM-dd hh-mm a")).format(new Date());
	
	int maxDepth = 4;
	
	boolean isExtractorEnable = false;
	
	ArrayList<String> Links= new ArrayList<String>();
	
	/*public void extracter() throws IOException
	{

		ArrayList<ExtractThread> extractThreads= new ArrayList<ExtractThread>();
		File[] folder = new File (currentfolder).listFiles();
		//try{
			for(File file : folder ){
				ExtractThread thread= new ExtractThread( file);
				extractThreads.add(thread);
				thread.start();				
				}
			
			System.out.println("Total extract thread :" + extractThreads.size());
			int stillWorking = 1;
			while(stillWorking>0)
			{
				stillWorking=0;
				for( ExtractThread thread : extractThreads)
				{
					if(thread.isAlive())
						stillWorking++;
				}
				System.out.println("total extract threads alive: "+stillWorking);
				
				try {
				    Thread.sleep(500);
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}
			}
			
			
	}*/
	public static void main(String[] args) throws IOException, SAXException, TikaException{
					
			CrawerlBhanu crawler= new CrawerlBhanu();
			
			File folder = new File(crawler.currentfolder);
			if(!folder.exists())
				folder.mkdir();
			
			String crawlURL ="";
			int x =0;
			
			for(String arg : args)
			{
				if(arg.contentEquals("-d"))
					crawler.maxDepth = Integer.parseInt(args[x+1]); 				
				if(arg.contentEquals("-u"))
					crawlURL=args[x+1];
				if(arg.contentEquals("-e"))
					crawler.isExtractorEnable = true;			
				x++;
			}
			
			ArrayList<CrawlThread> crawlThreads= new ArrayList<CrawlThread>();
			
			int l =  0;
			
			Connection connection = Jsoup.connect(crawlURL);
			Document htmlDocument = connection
					.ignoreContentType(true)
					.get();
			if(htmlDocument.select("head").first()==null)
			{
			 htmlDocument.select("head").html("<url>"+crawlURL.toString()+"</url>");
					
			}
			//u.text(crawlURL);
			/*else{Element u = htmlDocument.select("head").first().children().first()
					.before("<url>"+crawlURL.toString()+"</url>");}*/
			//u.text(crawlURL);
			
			File filename = new File(folder.toString()+"/01_Main.html");
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			bw.write(htmlDocument.html());
			bw.close(); 
			Elements linksOnPage = htmlDocument.select("a[href]");
			ArrayList<String> Links= new ArrayList<String>();			
			
			for(Element link : linksOnPage)
            	{	
                	Links.add(link.absUrl("href"));
                	//System.out.println(l +" : " +link.absUrl("href"));
                	
                	try{                		
                		crawlThreads.add(new CrawlThread(link.absUrl("href"), folder.toString()+"\\file_0_"+l+".html"));
                	}
        			catch (Exception e)
        			{continue;}
                	l++;
            }
			
			int LenghofIteration = Links.size();
			
			for(int i = 0 ; i < crawler.maxDepth ; i++){
				/*System.out.println();
				System.out.println("Starting for depth " + i);
				System.out.println();*/
				
				for(int j = 0 ; j < LenghofIteration ; j++){
					/*System.out.println("Crawled " + Links.get(j));
					System.out.println("Crawling " + j +":"+Links.get(j)+" at Depth "+i);*/
					
					if(Links.get(j).isEmpty() )
					{
						System.out.println("invalid:" +Links.get(j) );
						continue;
					}

					Connection connectionForSubLinks = Jsoup.connect(Links.get(j));
					connectionForSubLinks.timeout(10000);
					Document htmlDocumentForSubLinks=null;
					try{
					 htmlDocumentForSubLinks = connectionForSubLinks.get();
					}
					catch (Exception e)
					{
						//System.out.println(e);
						continue;
					}			
					
					Elements linksOnPageForSubLinks = htmlDocumentForSubLinks.select("a[href]");
					//System.out.println("sublink");
					
					int counter = 0;
		
					for(Element link : linksOnPageForSubLinks)
	            	{	
						if(link.absUrl("href").isEmpty() )
						{
							System.out.println("invalid:" +link.absUrl("href") );
							continue;
						}
						if(Links.contains(link.absUrl("href"))){
							
						}
						else{
						Links.add(link.absUrl("href"));
						//System.out.println(counter +" : " +link.absUrl("href"));
						try{
							CrawlThread thread = new CrawlThread(link.absUrl("href"), folder.toString()+"\\file_"+i+"_"+counter+".html");
							thread.start();
							crawlThreads.add(thread);
						}
						catch(Exception e)
						{continue;}
	                	counter++;
						}
	            	}
					
					System.out.println("Total thread now :" + crawlThreads.size());
					
					 Links.remove(j);
					
				}
				LenghofIteration = Links.size();
			}
			
			System.out.println(" Total links to crawl: "+ crawlThreads.size());
			
			/*for( CrawlThread thread : crawlThreads)
			{
				thread.start();
			}*/
			
			int stillWorking = 1;
			while(stillWorking>0)
			{
				stillWorking=0;
				for( CrawlThread thread : crawlThreads)
				{
					if(thread.isAlive())
						stillWorking++;
				}
				System.out.println("total crawl threads alive: "+stillWorking);
				
				try {
				    Thread.sleep(500);
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}
			}
			
			System.out.println("Done");
			
			if(crawler.isExtractorEnable)				
			{
				
				System.out.println("Starting Extraction and Indexing Started");
				Date startTime = new Date();
				SE_Index tf = new SE_Index(currentfolder);
				tf.buildAllDocuments();		
				System.out.println("time taken in indexing: "+((new Date()).getTime() - startTime.getTime()));
				
				startTime = new Date();
				tf.calculatePageRanking();
				System.out.println("time taken in page ranking: "+((new Date()).getTime() - startTime.getTime()));
				
				System.out.println("max_tfidf:"+ tf.max_tfid);
				System.out.println("max_page ranking:"+ tf.max_PageRanking);
				
				startTime = new Date();		
				tf.calculatePageRanking();
				System.out.println("time taken in overall page ranking per word: "+((new Date()).getTime() - startTime.getTime()));
						
				MongoClient mongoclient= new MongoClient();
				mongoclient=new MongoClient("localhost" , 27017);
				DB db = mongoclient.getDB( "mydb" );
				
				startTime = new Date();		
				tf.SaveDocuments(db);
				System.out.println("time taken in saving docs: "+((new Date()).getTime() - startTime.getTime()));
				
				startTime = new Date();		
				tf.SaveWords(db);
				System.out.println("time taken in saving words: "+((new Date()).getTime() - startTime.getTime()));
				
				
				String search = "the";
				search=search.toLowerCase();
				SE_WordData wd = tf.allwords.get(search);
				
				System.out.println("Searched word: "+wd.text);
				System.out.println("total docs word found in: "+wd.count);
				System.out.println("-------------------------------------------------------------------");	
			//	new SE_Index(currentfolder);
				//crawler.extracter();
			}
			else
			{
				System.out.println("Extraction is not enabled");
				return;
			}
		} 
		
	}
