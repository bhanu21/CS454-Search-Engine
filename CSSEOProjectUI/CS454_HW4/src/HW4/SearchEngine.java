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

public class SearchEngine {
	MongoClient mongoclient=null;	
	DB db = null;
	DBCollection docCollection=null;
	DBCollection wordCollection=null;
	double max_cos=0.0;
	
	public SearchEngine() throws UnknownHostException{
		this.mongoclient= new MongoClient();
		this.mongoclient=new MongoClient("localhost" , 27017);
		this.db = mongoclient.getDB( "mydb" );
		this.wordCollection=db.getCollection("SE_Words");
		this.docCollection=db.getCollection("SE_Documents");
		this.max_cos=max_cos;
		
	}
	
	public String search(String text) throws UnknownHostException, JSONException{
		TreeSet<String> words = new TreeSet<String>();
		
		boolean isAnd = false;
		if(text.contains(" and ")){
			isAnd=true;
			text = text.replace(" and ", " ");
		}
		
		TreeMap<String,String[]> Links = new TreeMap<String,String[]>();// count,rank,title
		
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
		String[] refLink = new String[]{"","0.0","0.0"}; // url,count,rank,title
		
		try {
		    while (cursor.hasNext()) {
		    	BasicDBList docs = (BasicDBList)cursor.next().get("docs");
		    	
		    	for (int i = 0; i < docs.size(); i++) {	
		    		String url = (String)((BasicDBObject)docs.get(i)).get("url");
		    		String title = (String)((BasicDBObject)docs.get(i)).get("title");
		    		Double rank = (Double)((BasicDBObject)docs.get(i)).get("rank");
		    		
		    		if(Links.containsKey(url)){
		    			Links.get(url)[0] = (Double.parseDouble(Links.get(url)[0])+1)+""; // count
		    			Links.get(url)[1] = (Double.parseDouble(Links.get(url)[0])+rank)+""; // rank
		    		}
		    		else
		    			Links.put(url,new String[]{"1.0",rank.toString(),title});
		    		
		    		if(Double.parseDouble(Links.get(url)[0])>Double.parseDouble(refLink[1]))
		    			refLink = new String[]{url,Links.get(url)[0].toString(),Links.get(url)[1].toString(),title};
		    		else if(Double.parseDouble(Links.get(url)[0])==Double.parseDouble(refLink[1]))
		    			if (Double.parseDouble(Links.get(url)[1])>Double.parseDouble(refLink[2]))
		    				refLink = new String[]{url,Links.get(url)[0].toString(),Links.get(url)[1].toString(),title};
			    }
		    }
		} finally {
		    cursor.close();
		}
		
		TreeMap<Double,Entry<String,String[]>> neworderedByCosSimilarities = new TreeMap<Double,Entry<String,String[]>>();
		TreeMap<Double,Entry<String,String[]>> orderedByCosSimilarities = new TreeMap<Double,Entry<String,String[]>>();
		
		for(String url : Links.keySet())
		{
			Double cosSimilarity = refLink[0].compareToIgnoreCase(url)==0?1.0:cosSimilarity(refLink[0], url);			
			neworderedByCosSimilarities.put(cosSimilarity,Links.floorEntry(url));
		}

		for(Double cs1 : neworderedByCosSimilarities.descendingKeySet())
		{
			if(cs1.compareTo(1.0)==0){
			Entry<String,String[]> temp = neworderedByCosSimilarities.get(cs1);
			
			orderedByCosSimilarities.put(cs1,temp);
			}
			else{
				
					Entry<String,String[]> temp = neworderedByCosSimilarities.get(cs1);
					
					orderedByCosSimilarities.put((cs1/max_cos),temp);
					
			}
		}
		
		StringBuilder jsonStr = new StringBuilder();
		jsonStr.append("[");
		int i=0;
		
		for(Double cs : orderedByCosSimilarities.descendingKeySet())
		{
			
			Entry<String,String[]> temp = orderedByCosSimilarities.get(cs);
			
			if(isAnd && Double.parseDouble(temp.getValue()[0]) < words.size())
				continue;
			
			
			if(i>0)
				jsonStr.append(",");
			
			jsonStr.append("{\"url\":\""+temp.getKey().replace("\\", "\\\\")+"\",\"cosSimilarity\":"+
							cs+",\"count\":"+temp.getValue()[0]+",\"rank\":"+temp.getValue()[1]+
							",\"title\":\""+temp.getValue()[2].replace("\"", "")+"\"}");
			i++;
		}
		jsonStr.append("]");
		return jsonStr.toString();
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
	
	private JSONArray sort(TreeMap<String,Double[]> Links) throws JSONException{
		JSONArray sortedJsonArray = new JSONArray();
	    List<JSONObject> jsonValues = new ArrayList<JSONObject>();
	    
	    for (int i = 0; i < Links.size(); i++) {	    	
	    	jsonValues.add(new JSONObject(Links.get(i).toString()));
	    }
	    Collections.sort( jsonValues, new Comparator<JSONObject>() {
	        //You can change "Name" with "ID" if you want to sort by ID
	        private static final String KEY_NAME = "rank";

	        @Override
	        public int compare(JSONObject a, JSONObject b) {
	            Double valA = 0.0;
	            Double valB = 0.0;

	            try {
	                valA = (Double) a.get(KEY_NAME);
	                valB = (Double) b.get(KEY_NAME);
	            } 
	            catch (JSONException e) {
	                //do something
	            }

	            return valB.compareTo(valA);
	            //if you want to change the sort order, simply use the following:
	            //return -valA.compareTo(valB);
	        }
	    });
	    
	    for (int i = 0; i < jsonValues.size(); i++) {
	        sortedJsonArray.put(jsonValues.get(i));
	    }
	    
	    return sortedJsonArray;
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
		    	DBObject doc2 = (DBObject)cursor.next();
		    	double dot=0.0;
    			double denom=0.0;
		    	for (int i = 0; i < ((BasicDBList)doc1.get("words")).size(); i++) {	
		    		
		    		BasicDBObject word1 = (BasicDBObject)((BasicDBList)doc1.get("words")).get(i);
		    		
		    		for(int j = 0; j < ((BasicDBList)doc2.get("words")).size(); j++) {
		    			
		    			BasicDBObject word2 = (BasicDBObject)((BasicDBList)doc1.get("words")).get(i);
		    		
		    			if(((String)word1.get("text")).compareTo((String)word2.get("text"))==0){
		    				dot += (Double)word1.get("tfidf")/2.5259884922267717 * (Double)word2.get("tfidf")/2.5259884922267717;
		    				//denom +=Math.sqrt((Math.pow((Double)word1.get("tfidf"),2)*(Math.pow((Double)word2.get("tfidf"),2))));
		    			}
		    		}
			    }
		    	
		    	similarity = (dot/(Math.sqrt(Math.pow((Double)(doc1.get("vectorlength")),2) * Math.pow((Double)(doc2.get("vectorlength")),2))))*(3.14/180);
		    	if(max_cos<similarity)
		    	{max_cos=similarity;}
		} finally {
		    cursor.close();
		}
		return similarity;
	}
}
