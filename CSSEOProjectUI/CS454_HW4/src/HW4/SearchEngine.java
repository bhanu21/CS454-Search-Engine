package HW4;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.jasper.tagplugins.jstl.core.Set;
import org.json.*;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import sun.awt.image.ImageWatched.Link;

public class SearchEngine {
	MongoClient mongoclient=null;	
	DB db = null;
	DBCollection docCollection=null;
	DBCollection wordCollection=null;
	double max_cos=0.0;
	double max_tfidf=0.0;
	double max_pageRanking=0.0;
	double pTfidf=0.0;
	double pRanking=0.0;
	double pTitle=0.0;
	
	public SearchEngine() throws UnknownHostException{
		this.mongoclient= new MongoClient();
		this.mongoclient=new MongoClient("localhost" , 27017);
		this.db = mongoclient.getDB( "mydb" );
		this.wordCollection=db.getCollection("SE_Words");
		this.docCollection=db.getCollection("SE_Documents");
		
		
	}
	
	public String getWordsStartWith(String text){
		
		int index = text.lastIndexOf(" ");
		String prefix = index==-1?"":text.substring(0, index)+" ";
		
		Pattern regex = Pattern.compile("^"+text.substring(index+1,text.length()));		
		DBObject query = new BasicDBObject("text", regex);
		
		DBCursor cursor = wordCollection.find(query).limit(10);			
		StringBuilder jsonStr = new StringBuilder();
		jsonStr.append("[");
		int i=0;
		
		try {
		    while (cursor.hasNext()) {
		    	
		    	if(i>0)
		    		jsonStr.append(",");
		    	
		    	jsonStr.append("\""+(prefix+(String)cursor.next().get("text")).replaceAll("[\\n]", "")+"\"");
		    	i++;
		    	
		    }
		} finally {
		    cursor.close();
		}
		jsonStr.append("]");
		
		return jsonStr.toString();
	}
	
	public String search(String text,boolean isCSEnabled,double[] percentages) //pTfidf,pRanking,pTitle
			throws UnknownHostException, JSONException{
		TreeSet<String> words = new TreeSet<String>();
		
		this.pTfidf = percentages[0];
		this.pRanking = percentages[1];
		this.pTitle = percentages[2];
		
		boolean isAnd = false;
		if(text.contains(" and ")){
			isAnd=true;
			text = text.replace(" and ", " ");
		}
		
		TreeMap<String,SearchDetail> Links = new TreeMap<String,SearchDetail>();// count,rank,title
		
		StringTokenizer tokens = new StringTokenizer(text, " ");
		
		BasicDBList or = new BasicDBList();
		
		while(tokens.hasMoreTokens()) {
			String word = tokens.nextToken().toLowerCase();
			word.trim();		
			
			if(words.contains(word))
				continue;
			else
				words.add(word);
			
			DBObject clause = new BasicDBObject("text", word);
			or.add(clause);
		}
		
		DBObject query = new BasicDBObject("$or", or);
		
		DBCursor cursor = wordCollection.find(query);	
		SearchDetail refLink = new SearchDetail(); // url,count,rank,title
		
		try {
		    while (cursor.hasNext()) {
		    	BasicDBList docs = (BasicDBList)cursor.next().get("docs");
		    	
		    	for (int i = 0; i < docs.size(); i++) {	
		    		String url = (String)((BasicDBObject)docs.get(i)).get("url");
		    		String title = (String)((BasicDBObject)docs.get(i)).get("title");
		    		Double rank = (Double)((BasicDBObject)docs.get(i)).get("rank");
		    		
		    		if(Links.containsKey(url)){
		    			Links.get(url).searchedWordsFound ++;
		    			//Links.get(url).customRanking+=rank;
		    		}
		    		else
		    			Links.put(url,new SearchDetail(url,title,0.0,1,0.0));
		    		
		    		getpageRanking(Links.get(url),words);
		    		
			    }
		    }
		} finally {
		    cursor.close();
		}
		
		Normalize(Links);
		
		refLink = CalculateCustomRanking(Links,words);
			
		List<SearchDetail> list = new ArrayList<SearchDetail>();
		
		for(String url : Links.keySet())
		{
			SearchDetail sd = Links.get(url);
			
			Double cs = cosSimilarity(refLink.url, url);
			
			sd.cosSimilarity = cs==1.0000000000000002?1.0000000000000000:cs;
			
			list.add(sd);
			
			if(max_cos<cs)
				max_cos = cs;
		}
		
/*		for(SearchDetail sd : Links.values())
		{
			sd.cosSimilarity = sd.cosSimilarity/max_cos;
			list.add(sd);
		}*/
		
		mongoclient.close();
		
		if(isCSEnabled)
			Collections.sort(list,SearchDetail.getCosSimilarityComparator());
		else
			Collections.sort(list);
		
		StringBuilder jsonStr = new StringBuilder();
		jsonStr.append("[");
		int i=0;
		
		for(SearchDetail cs : list)
		{
			
			if(isAnd && cs.searchedWordsFound < words.size())
				continue;
			
			
			if(i>0)
				jsonStr.append(",");
			
			jsonStr.append("{\"url\":\""+cs.url.replace("\\", "\\\\")+"\",\"cosSimilarity\":"+
							cs.cosSimilarity+",\"count\":"+cs.searchedWordsFound+",\"rank\":"+cs.customRanking+
							",\"title\":\""+cs.title.replace("\"", "")+"\"}");
			i++;
		}
		jsonStr.append("]");
		return jsonStr.toString();
	}
	
	
	private SearchDetail CalculateCustomRanking(TreeMap<String, SearchDetail> links,TreeSet<String> words) {
		
		SearchDetail refLink = new SearchDetail();
		
		for(SearchDetail sd : links.values()){
			
			Double hasTitle=0.0;
			
			for(String word:words)
			{
				if(sd.title.contains(word)){
					hasTitle = 1.0;
					break;
				}
			}
			
			sd.customRanking = 
					sd.pageRanking*this.pRanking
					+sd.tfidf*this.pTfidf
					+hasTitle*this.pTitle;
			
			if(sd.searchedWordsFound>refLink.searchedWordsFound)
    			refLink = sd;
    		else if(sd.searchedWordsFound==refLink.searchedWordsFound)
    			if (sd.customRanking>refLink.customRanking)
    				refLink = sd;
		}
		return refLink;
	}

	private void Normalize(TreeMap<String, SearchDetail> links) {

		for(SearchDetail sd : links.values()){
			sd.pageRanking = sd.pageRanking/max_pageRanking;
			sd.tfidf = sd.tfidf/max_tfidf;
		}
		
	}

	private void getpageRanking(SearchDetail searchDetail,TreeSet<String> words) {
		
		DBObject query = new BasicDBObject("url", searchDetail.url);
		
		DBCursor cursor = docCollection.find(query);
		
		if(cursor.hasNext())
		{
			DBObject doc = (DBObject)cursor.next();
			searchDetail.pageRanking = (Double)doc.get("ranking");
			
			max_pageRanking = max_pageRanking<searchDetail.pageRanking?searchDetail.pageRanking:max_pageRanking;
			
			for(int i=0;i< ((BasicDBList)doc.get("words")).size();i++)
			{
				DBObject word = (DBObject)((BasicDBList)doc.get("words")).get(i);
				if(words.contains((String)word.get("text")))
				{
					searchDetail.tfidf += (Double)word.get("tfidf");
					max_tfidf = max_tfidf<searchDetail.tfidf?searchDetail.tfidf:max_tfidf;
				}
			}
		}
		
	}

	private <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V[]>> entriesSortedByValues(Map<K,V[]> map) {
        SortedSet<Map.Entry<K,V[]>> sortedEntries = new TreeSet<Map.Entry<K,V[]>>(
            new Comparator<Map.Entry<K,V[]>>() {
                
            	@Override 
                public int compare(Map.Entry<K,V[]> e1, Map.Entry<K,V[]> e2) {
                    int res = e2.getValue()[0].compareTo(e1.getValue()[0]);
                    if(res==0)
                    	res = e2.getValue()[1].compareTo(e1.getValue()[1]);
                    return res != 0 ? res : 1;
                }
            }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
	
	private double cosSimilarity(String url, String url2) {
		BasicDBList or = new BasicDBList();
		or.add(new BasicDBObject("url", url));
		or.add(new BasicDBObject("url", url2));
		DBObject query = new BasicDBObject("$or", or);
		
		DBCursor cursor = docCollection.find(query);
		double similarity = 0;
		try {		    
		    	DBObject doc1 = (DBObject)cursor.next();
		    	DBObject doc2 = null;
		    	
		    	if(cursor.hasNext())
		    		doc2 = (DBObject)cursor.next();
		    	else
		    		doc2 = doc1;
		    	
		    	double dot=0.0;
		    	for (int i = 0; i < ((BasicDBList)doc1.get("words")).size(); i++) {	
		    		
		    		BasicDBObject word1 = (BasicDBObject)((BasicDBList)doc1.get("words")).get(i);
		    		
		    		for(int j = 0; j < ((BasicDBList)doc2.get("words")).size(); j++) {
		    			
		    			BasicDBObject word2 = (BasicDBObject)((BasicDBList)doc2.get("words")).get(j);
		    		
		    			if(((String)word1.get("text")).compareTo((String)word2.get("text"))==0){
		    				similarity += (Double)word1.get("tfidf") * (Double)word2.get("tfidf");
		    				//dot += (Double)word1.get("tfidf")/2.5259884922267717 * (Double)word2.get("tfidf")/2.5259884922267717;
		    				//denom +=Math.sqrt((Math.pow((Double)word1.get("tfidf"),2)*(Math.pow((Double)word2.get("tfidf"),2))));
		    			}
		    		}
			    }
		    	similarity = similarity / ((Double)(doc1.get("vectorlength")) * (Double)(doc2.get("vectorlength")));
		    	//similarity = (dot/(Math.sqrt(Math.pow((Double)(doc1.get("vectorlength")),2) * Math.pow((Double)(doc2.get("vectorlength")),2))))*(3.14/180);

		} finally {
		    cursor.close();
		}
		return similarity;
	}
}
