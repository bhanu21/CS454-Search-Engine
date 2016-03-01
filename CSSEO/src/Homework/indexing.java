package Homework;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class indexing {
	
	static List<File> files = new ArrayList();

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		list(new File("C:/Temp/en/"));
		
		indexing index = new indexing();
		HashSet<Pair> words = new HashSet<Pair>();
        Pair pair=index.new Pair();
		File[] currentfolder=( new File ("C:/Temp/en/articles/5/")).listFiles();
		for(File f: currentfolder)
		{
		
			
			 UUID uuid = UUID.randomUUID();
			 
			 String uu_id=uuid.toString();
			 index.StopWords(index.GetFolders(f),uu_id);
			index.extracter(f,uu_id);
			
			
		}
		
		}
	 public class Pair{
		  String element1;
		  double element2;
		  String uuid;
		  
		}
		public void extracter( File file, String uuid) throws IOException
		{

			ArrayList<ExtractThread> extractThreads= new ArrayList<ExtractThread>();
			File[] currentfolder=( new File ("C:/Temp/en/articles/5/")).listFiles();
			//try{
				
					ExtractThread thread= new ExtractThread( file,uuid);
					extractThreads.add(thread);
					thread.start();				
					
				
			//	System.out.println("Total extract thread :" + extractThreads.size());
				int stillWorking = 1;
				while(stillWorking>0)
				{
					stillWorking=0;
					for( ExtractThread thread1 : extractThreads)
					{
						if(thread1.isAlive())
							stillWorking++;
					}
					//System.out.println("total extract threads alive: "+stillWorking);
					
					try {
					    Thread.sleep(500);
					} catch(InterruptedException ex) {
					    Thread.currentThread().interrupt();
					}
				}
				
	}
	
	public static void list(File file) {
	    
		if(!file.isDirectory())
			files.add(file);		
		File[] children = file.listFiles();
	    for (File child : children) {
	        list(child);
	    }
	}
	
	 public double tfCalculator(List<String> temps, String termToCheck,Pair pair, HashSet<Pair>words,String uuid) {
	        double count = 0; 
	        
	        //to count the overall occurrence of the term termToCheck
	        for (String s : temps) {
	            if (s.equalsIgnoreCase(termToCheck)) {
	                count++;
	                
	            }
	           
	        }
	        System.out.println(count);
	        System.out.println((1%2));
	        pair.element1=termToCheck;
	        pair.element2=count;
	        pair.uuid=uuid;
	       words.add( pair);
	      // System.out.println(words.size());
		    for(Pair p: words)
		       {
		    	 //  System.out.println(p.element1 +" : "+ p.element2+"  UUID:" +p.uuid);
		    	   
		       }
		    String s=count/temps.size()+"";
		    Double d= Double.parseDouble(s);
		    
	        
	       System.out.println(termToCheck +" : "+d);
	        return count/temps.size();
	    }
	
	 public void StopWords(File new_file , String uuid) throws IOException
		{
		 indexing ind=new indexing();
				Document doc = Jsoup.parse(new_file, "UTF-8");
				Element e = doc.select("body").first();
				Pair pair = new Pair();
				HashSet<Pair> words = new HashSet<Pair>();
				MongoClient mg= new MongoClient( "localhost" , 27017);
				DB db = mg.getDB( "mydb" );
				
				DBCollection collection1=db.getCollection("indices");
				 File newFile = new File("C:/Users/bps21/Desktop/temp/"+new_file.getName()+".txt");
				 String[] ENGLISH_STOP_WORDS ={
						    "a", "an", "and", "are","as","at","be", "but",
						    "by", "for", "if", "in", "into", "is", "it",
						    "no", "not", "of", "on", "or", "s", "such",
						    "that", "the", "their", "then", "there","these",
						    "they", "this", "to", "was", "will", "with" };
					String text = e.text().toLowerCase();
					
				
				//System.out.println(e.text());
					 for (String s : ENGLISH_STOP_WORDS)
					 {
					   if (text.contains(" "+s+" "))
					   {
						  text= text.replace(" "+s+" " ," ");
					     
					   }
					 }
				BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
				bw.write(text.replace(","," ").replace(".", " "));
				bw.close();
				String token1 = "";

			    Scanner inFile1 = new Scanner(newFile).useDelimiter(" ");

			    
			    List<String> temps = new ArrayList<String>();
			   
				
			    while (inFile1.hasNext()) {
			     
			      token1 = inFile1.next();
			      temps.add(token1);
			      
					
			    }
			    inFile1.close();
			   int i=1;
			    for(String temp : temps)
				{
			    	BasicDBObject document = new BasicDBObject();
					
					document.put("word", temp);
					BasicDBObject document1 = new BasicDBObject();
					document1.put("UUID", uuid);
					document1.put("tf_score", ind.tfCalculator(temps, temp,pair,words,uuid));
					document.put("File"+i, document1);
					BasicDBObject query = new BasicDBObject("word",temp);
			          
			          collection1.update(query,document,true,false);
			          i++;
					
				}	
			   
			}
			
		
		
	

}
