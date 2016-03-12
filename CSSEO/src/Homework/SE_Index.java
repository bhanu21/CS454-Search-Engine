package Homework;



import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.jsoup.nodes.Element;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;

import opennlp.tools.doccat.DocumentSample;

public class SE_Index {
	
	static String rootFolder = "C:/Temp/en_small/";
	static List<File> files = new ArrayList();
	public TreeMap<String, SE_Document> documents;
	public TreeMap<String, SE_WordData> allwords; //d_j: t_i elem d_j, idf_j
	public boolean corpusUpdated;
	public int docSize;
	
	/**
	 * Filename filter to accept .txt files
	 */
	FilenameFilter filter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			if (name.toLowerCase().endsWith(".html")) return true;
			return false;
		}
	};
	
	
	public static List<File> list(File file) {
	    
		if(!file.isDirectory())
			files.add(file);	
		else{
		File[] children = file.listFiles();
	    for (File child : children) {
	        list(child);
	    }
		}
		return files;
	}
	public SE_Index(String foldername) throws IOException {
		allwords = new TreeMap<String, SE_WordData>();
		documents = new TreeMap<String, SE_Document>();
		docSize = 0;
		
		List<File>datafolder = list(new File(foldername));
		
		for(File f : datafolder){
			docSize++;
			insertDocument(f.getPath());
		}
	
		corpusUpdated = false;
		if (corpusUpdated == false)
		{
			updateCorpus();
		}
		
	}
	
	public void updateCorpus() {
		String word;
		SE_WordData corpusdata;
		for (Iterator<String> it = allwords.keySet().iterator(); it.hasNext(); ) {
			word = it.next();
			corpusdata = allwords.get(word);
			corpusdata.idf = Math.log(docSize / corpusdata.count);

			allwords.put(word, corpusdata);
		}	
		corpusUpdated = true;
	}
	
	
	public void buildAllDocuments() {
		String word;
		for (Iterator<String> it = documents.keySet().iterator(); it.hasNext(); ) {
			word = it.next();
			documents.get(word).calculateTfIdf(this);
		}
	}
	
	public void calculatePageRanking() {
		for(int i=0;i<3;i++){
			SE_Document doc=null;
			for (Iterator<String> it = documents.keySet().iterator(); it.hasNext(); ) {
				double sum = 0.0;
				doc = documents.get(it.next());		
				if(doc.incomingLinks.size()==0)
					sum=1.0/documents.size();
				for(Iterator<String> ibLinks = doc.incomingLinks.iterator(); ibLinks.hasNext();){
					String link = ibLinks.next();
					double rank = documents.get(link).PageRanking;
					sum = sum + (rank==0.0?(1.0/documents.size()):rank)/documents.get(link).outgoingLinks.size();
				}
				doc.PageRanking = sum;
				
			}	
		}
	}
	
	public void calculateRankingforSearch() {
		for (Iterator<String> it = allwords.keySet().iterator(); it.hasNext(); ) {
			SE_WordData wd = allwords.get(it.next());	
			for(Iterator<String> links = wd.docs.keySet().iterator(); links.hasNext();){
				String link = links.next();
				SE_Document doc = documents.get(link);
				Double tfidf = doc.words.get(wd.text)[2];
				Double pageRankperWord =  
						tfidf*.6 // tfidf - 60%
						+(doc.title.toLowerCase().contains(wd.text)?1.0:0.0)*.2 // title match 20%
						+doc.PageRanking*.2; //page ranking 20%
				wd.docs.put(link, pageRankperWord);
			}
		}		
	}
	
	public void insertDocument(String filename) throws IOException {

		SE_Document doc = null;
		
		if(documents.containsKey(filename)&& !documents.get(filename).isComplete){
			documents.get(filename).process(filename, this);
		}
		else if(! documents.containsKey(filename)){		
			doc = new SE_Document(filename, this);
			documents.put(filename, doc);
		}
		//documents.put(filename.substring(filename.lastIndexOf('/') + 1), doc);
		if (corpusUpdated == false) updateCorpus();
		//System.out.println(doc.sumof_n_kj);

	}
	

	public void addWordOccurence(String word,String doc) {
		SE_WordData tempdata = null;
		if (allwords.get(word) == null) {
			tempdata = new SE_WordData();
			tempdata.text = word;
			tempdata.count = 1.0;
			tempdata.docs.put(doc,0.0);
			allwords.put(word, tempdata);
		} else {
			tempdata = allwords.get(word);
			tempdata.count++;
			tempdata.docs.put(doc,0.0);
			allwords.put(word,tempdata);
			
		}
	}
	
	public String getFileUrl (String link,String fileName)
	{
		int count=0;
		File file = (new File(fileName)).getParentFile();
		while(link.contains("../"))
		{
			link = link.replaceFirst("../", "");
			count++;
		}
		
		for(int i = count;i>0;i--){
			if(file.getParentFile()!=null)
				file = file.getParentFile();
			else
				break;
		}
		
		return file.getPath()+"\\"+link.replace("/", "\\");
	}
	
	public <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
            new Comparator<Map.Entry<K,V>>() {
                @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                    int res = e2.getValue().compareTo(e1.getValue());
                    return res != 0 ? res : 1; // Special fix to preserve items with equal values
                }
            }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
	
	public void SaveDocuments(DB db){
		DBCollection docCollection=db.getCollection("SE_Documents");
		
		for (Iterator<String> it = documents.keySet().iterator(); it.hasNext(); ) {
			String url = it.next();
			
			SE_Document doc = documents.get(url);
			
			BasicDBObject dbDoc = new BasicDBObject();
			dbDoc.put("url", url);
			dbDoc.put("title", doc.title);
			dbDoc.put("ranking", doc.PageRanking);
			
			BasicDBList dbDocWords = new BasicDBList();			
			for(Iterator<String> words = doc.words.keySet().iterator(); words.hasNext();){
				String text = words.next();
				BasicDBObject dbDocWord = new BasicDBObject();
				Double[] word = doc.words.get(text);
				dbDocWord.put("text", text);
				dbDocWord.put("count", word[0]);
				dbDocWord.put("tf", word[1]);
				dbDocWord.put("tfidf", word[2]);
				
				dbDocWords.add(dbDocWord);
			}
			dbDoc.put("words", dbDocWords);
			
			BasicDBList dbMetaDatas = new BasicDBList();			
			for(Iterator<String> metas = doc.Meta_data.keySet().iterator(); metas.hasNext();){
				String http_equiv = metas.next();
				BasicDBObject dbMetaData = new BasicDBObject();		
				dbMetaData.put("http-equiv", http_equiv);
				dbMetaData.put("content", doc.Meta_data.get(http_equiv));
				
				dbMetaDatas.add(dbMetaData);
			}
			dbDoc.put("meta_data", dbMetaDatas);
			
			BasicDBList medias= new BasicDBList();
			medias.add(doc.Media);
			dbDoc.put("media", medias);
			
			BasicDBList ins= new BasicDBList();
			ins.add(doc.incomingLinks);
			dbDoc.put("incoming", ins);
			
			BasicDBList outs= new BasicDBList();
			outs.add(doc.outgoingLinks);
			dbDoc.put("outgoing", outs);
			
			docCollection.insert(dbDoc);
		}
	}
	
	public void SaveWords(DB db){
		DBCollection docCollection=db.getCollection("SE_Words");		
		
		for (Iterator<String> it = allwords.keySet().iterator(); it.hasNext(); ) {
			String word = it.next();
			
			SE_WordData wd = allwords.get(word);
			
			BasicDBObject dbDoc = new BasicDBObject();
			dbDoc.put("text", wd.text);
			dbDoc.put("count", wd.count);
			dbDoc.put("idf", wd.idf);
			
			BasicDBList dbDocs = new BasicDBList();	
			
/*			for(Iterator<Entry<String,Double>> docs = entriesSortedByValues(wd.docs).iterator(); docs.hasNext();){
				Entry<String,Double> doc = docs.next();
				BasicDBObject dbDocData = new BasicDBObject();
				dbDocData.put("url", doc.getKey());
				dbDocData.put("rank", doc.getValue());
				dbDocs.add(dbDocData);
			}*/
			
			for(Iterator<String> docs = wd.docs.keySet().iterator(); docs.hasNext();){
				String doc = docs.next();
				BasicDBObject dbDocData = new BasicDBObject();
				dbDocData.put("url", doc);
				dbDocData.put("rank", wd.docs.get(doc));
				dbDocs.add(dbDocData);
			}
			dbDoc.put("docs", dbDocs);
			docCollection.insert(dbDoc);
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException{
		//Test code for TfIdf
		Date startTime = new Date();
		SE_Index tf = new SE_Index(rootFolder);
		tf.buildAllDocuments();		
		System.out.println("time taken in indexing: "+((new Date()).getTime() - startTime.getTime()));
		
		startTime = new Date();
		tf.calculatePageRanking();
		System.out.println("time taken in page ranking: "+((new Date()).getTime() - startTime.getTime()));
		
		startTime = new Date();		
		tf.calculateRankingforSearch();
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
		
		/*for( String fileName : wd.docs.keySet())
		{
			SE_Document doc = tf.documents.get(fileName);
			System.out.println("Page Url: "+doc.Page_Location);
			System.out.println("Title: "+doc.title);
			System.out.println("Word Occurance: "+doc.words.get(search)[0]);
			System.out.println("TFIDF: "+doc.words.get(search)[2]);
			System.out.println("Page Ranking: "+doc.PageRanking);
			System.out.println("Overall Ranking: "+wd.docs.get(fileName).doubleValue());
			System.out.println("-------------------------------------------------------------------");		
			
		}	*/

		
		
	}
	
}

