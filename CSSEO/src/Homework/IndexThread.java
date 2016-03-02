package Homework;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.healthmarketscience.jackcess.Index;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import Homework.indexing.Pair;

public class IndexThread extends Thread {
	MongoClient mongoClient;
	File filePath;
	String uuid;

	public IndexThread(MongoClient mongoClient, File filePath , String uuid) throws UnknownHostException
   {
      this.mongoClient = mongoClient;
      this.filePath = filePath;
      this.uuid=uuid;
   }
	public class Tf_return
	{
		double tf_score;
		double count;
		int doc_count;
		public Tf_return(double tf_score, double count, int doc_count) {
			super();
			this.tf_score = tf_score;
			this.count = count;
			this.doc_count = doc_count;
		}
		public double getTf_score() {
			return tf_score;
		}
		public double getCount() {
			return count;
		}
		public int getDoc_count() {
			return doc_count;
		}
		
	}
	 public Tf_return tfCalculator(List<String> temps, String termToCheck,Pair pair, HashSet<Pair>words,String uuid) {
	        double count = 0; 
	        
	        //to count the overall occurrence of the term termToCheck
	        for (String s : temps) {
	            if (s.equalsIgnoreCase(termToCheck)) {
	                count++;
	                
	            }
	           
	        }
	       
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
		    
	        
	    
	        return new Tf_return(count/temps.size(), count, temps.size())  ;
	    }
	 @Override
   public void run()
   {
	   try{
		   	System.out.println("extracting: "+filePath);

			 indexing ind=new indexing();
			
				 	
					Document doc = Jsoup.parse(filePath, "UTF-8");
					Element e = doc.select("body").first();
					Pair pair = ind.new Pair();
					HashSet<Pair> words = new HashSet<Pair>();
					
					DB db = mongoClient.getDB( "mydb" );
					
					DBCollection collection1=db.getCollection("indices");
					 File newFile = new File("C:/Users/bps21/Desktop/temp/"+filePath.getName()+".txt");
					 String[] ENGLISH_STOP_WORDS ={
							    "a", "an", "and", "are","as","at","be", "but",
							    "by", "for", "if", "in", "into", "is", "it",
							    "no", "not", "of", "on", "or", "s", "such",
							    "that", "the", "their", "then", "there","these",
							    "they", "this", "to", "was", "will", "with", "has", "had" ,"been",
							    " "};
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
						document.put("count", (tfCalculator(temps, temp,pair,words,uuid)).count);
						document.put("doc_word_count", (tfCalculator(temps, temp,pair,words,uuid)).doc_count);
						document.put("UUID", uuid);
						document.put("tf_score", (tfCalculator(temps, temp,pair,words,uuid)).tf_score);
						
						//BasicDBObject query = new BasicDBObject("word",temp);
				          collection1.insert(document);
				         
				          i++;
						
					}	
	   }
				    catch(Exception e)
					{
						System.out.println(e);
						/*System.out.println("url: "+url);
						System.out.println("exception: "+e.getMessage());*/
					} 
				   
				
	   
   }
		
}
