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

public class indexing {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		indexing index = new indexing();
		HashSet<Pair> words = new HashSet<Pair>();
        Pair pair=index.new Pair();
		File[] currentfolder=( new File ("C:/Users/bps21/Desktop/cacm/")).listFiles();
		for(File f: currentfolder)
		{
		
			index.StopWords(index.GetFolders(f));
		}
		}
	 public class Pair{
		  String element1;
		  int element2;
		  
		}
	 public File GetFolders(File current_folder)
		{
		 
			if(current_folder.isDirectory()){
				String[] folders= current_folder.list();
				for(String folder: folders)
				{
					 if (new File("C:/Users/bps21/Desktop/cacm/" + folder).isDirectory())
					 {
						 GetFolders(new File("C:/Users/bps21/Desktop/cacm/"+folder));
					 }
					 
						 return (new File("C:/Users/bps21/Desktop/cacm/"+ folder)); 
				}
			}
			else
			{
				return current_folder;
			}
			//try{
			return current_folder;
			
			//return null;
			
		}
	 public double tfCalculator(List<String> temps, String termToCheck,Pair pair, HashSet<Pair>words) {
	        int count = 0; 
	        
	        //to count the overall occurrence of the term termToCheck
	        for (String s : temps) {
	            if (s.equalsIgnoreCase(termToCheck)) {
	                count++;
	                
	            }
	           
	        }
	        pair.element1=termToCheck;
	        pair.element2=count;
	       words.add( pair);
	       System.out.println(words.size());
		    for(Pair p: words)
		       {
		    	   System.out.println(p.element1 +" : "+ p.element2);
		    	   
		       }
	        
	       
	        return count / temps.size();
	    }
	
	 public void StopWords(File new_file) throws IOException
		{
		 indexing ind=new indexing();
				Document doc = Jsoup.parse(new_file, "UTF-8");
				Element e = doc.select("body").first();
				Pair pair = new Pair();
				HashSet<Pair> words = new HashSet<Pair>();
				 UUID uuid = UUID.randomUUID();
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
			   
			    for(String temp : temps)
				{
			    	
					ind.tfCalculator(temps, temp,pair,words);
				}	
			   
			}
			
		
		
	

}
