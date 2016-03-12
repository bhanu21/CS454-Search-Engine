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
	
	public SearchEngine() throws UnknownHostException{
		this.mongoclient= new MongoClient();
		this.mongoclient=new MongoClient("localhost" , 27017);
		this.db = mongoclient.getDB( "mydb" );
		this.docCollection=db.getCollection("SE_Words");
	}
	
	public String search(String text) throws UnknownHostException, JSONException{
		TreeSet<String> words = new TreeSet<String>();
		
		boolean isAnd = false;
		if(text.contains(" and ")){
			isAnd=true;
			text = text.replace(" and ", " ");
		}
		
		TreeMap<String,Double[]> Links = new TreeMap<String,Double[]>();
		
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
		
		DBCursor cursor = docCollection.find(query);			
		
		try {
		    while (cursor.hasNext()) {
		    	BasicDBList docs = (BasicDBList)cursor.next().get("docs");
		    	
		    	for (int i = 0; i < docs.size(); i++) {	
		    		String url = (String)((BasicDBObject)docs.get(i)).get("url");
		    		Double rank = (Double)((BasicDBObject)docs.get(i)).get("rank");
		    		
		    		if(Links.containsKey(url)){
		    			Links.get(url)[0]++;
		    			Links.get(url)[1]+=rank;
		    		}
		    		else
		    			Links.put(url,new Double[]{1.0,rank});
			    }
		    }
		} finally {
		    cursor.close();
		}
		
		//if(isAnd)
			
		
		StringBuilder jsonStr = new StringBuilder();
		jsonStr.append("[");
		int i=0;
		for(Iterator<Entry<String,Double[]>> it = entriesSortedByValues(Links).iterator();it.hasNext();)
		{
			Entry<String,Double[]> link = it.next();
			
			if(isAnd && link.getValue()[0] < words.size())
				continue;
			
			
			if(i>0)
				jsonStr.append(",");
			
			jsonStr.append("{\"url\":\""+link.getKey().replace("\\", "\\\\")+"\",\"count\":"+link.getValue()[0]+",\"rank\":"+link.getValue()[1]+"}");
			i++;
		}
		jsonStr.append("]");
		return jsonStr.toString();
	}
	
	public String getWordsStartWith(String text){
		
		Pattern regex = Pattern.compile("^"+text);		
		DBObject query = new BasicDBObject("text", regex);
		
		DBCursor cursor = docCollection.find(query).limit(10);			
		StringBuilder jsonStr = new StringBuilder();
		jsonStr.append("[");
		int i=0;
		
		try {
		    while (cursor.hasNext()) {
		    	
		    	if(i>0)
		    		jsonStr.append(",");
		    	
		    	jsonStr.append("\""+(String)cursor.next().get("text")+"\"");
		    	i++;
		    	
		    }
		} finally {
		    cursor.close();
		}
		jsonStr.append("]");
		
		return jsonStr.toString();
	}
	public <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V[]>> entriesSortedByValues(Map<K,V[]> map) {
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
	
	public JSONArray sort(TreeMap<String,Double[]> Links) throws JSONException{
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

}
