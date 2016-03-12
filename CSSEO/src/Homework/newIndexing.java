package Homework;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;



public class newIndexing {
	static HashMap<String, String> mainList = new HashMap<String, String>();
	static HashMap<Integer, String> stringList = new HashMap<Integer, String>();

	@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
	public static void main(String[] args) throws JsonParseException,
			JsonProcessingException, IOException, SAXException, TikaException {
		// read the json

		final InputStream in = new FileInputStream("C:/Users/bps21/Desktop/extract.json");
		int varI = 0;
			MongoClient mongoclient= new MongoClient("localhost" , 27017);
			for (Iterator it = new ObjectMapper().readValues(
					new JsonFactory().createJsonParser(in), Map.class); it
					.hasNext();) {
				varI++;
				LinkedHashMap<String, Object> keyValue = (LinkedHashMap<String, Object>) it.next();

				String filePath = (String) keyValue.get("File Name");

				File file = new File(filePath);
				InputStream input = new FileInputStream(file);
				//System.out.println(file.getPath());

				Metadata metadata = new Metadata();

				BodyContentHandler handler = new BodyContentHandler(10 * 1024 * 1024);
				AutoDetectParser parser = new AutoDetectParser();

				parser.parse(input, handler, metadata);
				String dataContent = handler.toString();
				HashMap<String, Integer> hashmap = new HashMap<String, Integer>();
				dataContent = dataContent.replaceAll("[^a-zA-Z]+", " ");
				dataContent = dataContent.toLowerCase();

				String[] wordArray = dataContent.split(" ");
				String newString = "";
				for (String str : wordArray) {
					if (str.equals("the") || str.equals("a") || str.equals("is")
							|| str.equals("") || str.equals(" ")) {
						// No Action Please.
					} else {
						newString = newString + " " + str;

						if (hashmap.containsKey(str)) {
							int var = hashmap.get(str);
							hashmap.put(str, var + 1);
						} else {
							hashmap.put(str, 1);
						}
					}
				}
				newIndexing.stringList.put(varI, newString);

				Set set = hashmap.entrySet();
				Iterator iter = set.iterator();
				// Display Each elements
				DB db = mongoclient.getDB( "mydb" );
				
				DBCollection collection1=db.getCollection("newIndex");
				while (iter.hasNext()) {
					Map.Entry me = (Map.Entry) iter.next();
					String key = (String) me.getKey();
					if (newIndexing.mainList.containsKey(key)) {
						String value = newIndexing.mainList.get(key) + " [" + (varI) + ":"
								+ me.getValue() + "]";
						newIndexing.mainList.put(key, value);

					} else {
						String value = "[" + (varI) + ":" + me.getValue() + "]";
						newIndexing.mainList.put(key, value);

					}
					BasicDBObject document = new BasicDBObject();
					
					document.put("word", me.getKey());
					document.put("count", me.getValue());
					//document.put("doc_word_count", (tfCalculator(temps, temp,pair,words,uuid)).doc_count);
					document.put("UUID", varI);
					   collection1.insert(document);
				        
				//	document.put("tf_score", (tfCalculator(temps, temp,pair,words,uuid)).tf_score);
					
				}

				//System.out.println("Total extract thread :" + indexThreads.size());
				
				
				
			
		

		// access file with tika

		// read content

		// seperate all words

		// store as index file

		//
	

	}
			Set s = newIndexing.mainList.entrySet();	
			// Get an iterator
			Iterator i = s.iterator();
			// Display elements
			while (i.hasNext()) {
				Map.Entry me = (Map.Entry) i.next();
				System.out.print(me.getKey() + ": ");
				System.out.println(me.getValue());
			}
			System.out.println("--x--");
			in.close();

		}
		}

	
