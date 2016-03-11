package Homework;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * Class that represents a text document
 * Keeps track of the number of times a word appears in the document, it's term frequency, and 
 * eventually, its inverse document frequency (if used with TfIdf) for finding
 * important keywords in the document
 * 
 * 
 * @author Barkin Aygun
 *
 */

public class SE_Document {
	public boolean isComplete=false;
	public String title = null;
	public String Page_Location = null;
	public HashMap<String,String> Meta_data = new HashMap<String,String>();
	public Set<String> Media = new HashSet<String>();
	public TreeMap<String, Double[]> words; // n_ij, tf_ij, tf_idf
	public Set<String> incomingLinks = new HashSet<String>();
	public Set<String> outgoingLinks = new HashSet<String>();
	double PageRanking = 0.0;
	public int sumof_n_kj;
	double vectorlength;
	
	/**
	 * Constructor for document class that is used by the parent TfIdf class
	 * @param br Reader that loaded the text file already, used to read lines from
	 * large documents
	 * @param parent the TfIdf class calling this ctor
	 * @throws IOException 
	 */
	public SE_Document(){
		
	}
	public SE_Document(String filename, SE_Index parent) throws IOException {
		process(filename,parent);
	}
	public void process(String filename, SE_Index parent) throws IOException {
		Page_Location = filename;
		BufferedReader br;				
		String line;
		String word;
		StringTokenizer tokens;
		sumof_n_kj = 0;
		vectorlength = 0;
		Double[] tempdata;
		words = new TreeMap<String, Double[]>();
		try {
			br = new BufferedReader(new FileReader(filename));
			line = br.readLine();
			while (line != null) {
				tokens = new StringTokenizer(line, ":; \"\',.[]{}()¡!?-/");
				while(tokens.hasMoreTokens()) {
					word = tokens.nextToken().toLowerCase();
					word.trim();
					if (word.length() < 3) continue;
					if (words.get(word) == null) {
						tempdata = new Double[]{1.0,0.0,0.0};
						words.put(word, tempdata);
					}
					else {
						tempdata = words.get(word);
						tempdata[0]++;
						words.put(word,tempdata);
					}
					sumof_n_kj++;
				}
				line = br.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Iterate through the words to fill their tf's
		for (Iterator<String> it = words.keySet().iterator(); it.hasNext(); ) {
			word = it.next();
			tempdata = words.get(word);
			tempdata[1] = tempdata[0] / (float) sumof_n_kj;
			words.put(word,tempdata);
			parent.addWordOccurence(word,this.Page_Location);
		}	
		
		org.jsoup.nodes.Document doc = Jsoup.parse(new File(filename), "UTF-8");
		title = doc.title().toString();
		Elements linksOnPage = doc.select("a[href]");
		Elements metas = doc.select("META");
		if(metas!=null&& metas.size()>0)
			for(Element u :metas)
				Meta_data.put(metas.first().attr("http-equiv"),metas.first().attr("content").toString());
		for(Element u : linksOnPage )
		{
			String url = null;
			if(u.absUrl("href")!=""){
				url = u.absUrl("href").toString();
			}
			else{
				
				url = parent.getFileUrl(u.attr("href"),filename);
				outgoingLinks.add(url);
			}
			
			//outgoingLinks.add(url);
			if(parent.documents.containsKey(url))
				parent.documents.get(url).incomingLinks.add(filename);
			else if((new File(url)).exists()){
				SE_Document newDoc = new SE_Document();
				newDoc.incomingLinks.add(url);
				parent.documents.put(url, newDoc);
			}
		}
		
		Elements media = doc.select("img[src]");
		for(Element u : media )
		{
			String url = null;
			if(u.absUrl("src")!=""){
				url = u.absUrl("src").toString();
			}
			else{				
				url = parent.getFileUrl(u.attr("src"),filename);
				Media.add(url);
			}
			//Media.add(url);
		}
		this.isComplete=true;
	}
	
	/**
	 * Calculates the tfidf of the words after called by the parent TfIdf class
	 * @param parent the TfIdf class
	 */
	public void calculateTfIdf(SE_Index parent) {
		String word;
		SE_WordData corpusdata;
		Double[] worddata;
		double tfidf;
		for (Iterator<String> it = words.keySet().iterator(); it.hasNext(); ) {
			word = it.next();
			corpusdata= parent.allwords.get(word);
			worddata = words.get(word);
			tfidf = worddata[1] * corpusdata.idf;
			worddata[2] = tfidf;
			vectorlength += tfidf * tfidf;
			words.put(word, worddata);
		}
		vectorlength = Math.sqrt(vectorlength);
	}
	
	/**
	 * Prints every word in the document with their information:
	 * <ul> 
	 * <li>number of times word appears
	 * <li>Word's term frequency (number of times it appears / total word count)
	 * <li>Word's inverse document frequency (specifically how important it is in this document)
	 * </ul> 
	 */
	public void printData() {
		String word;
		Double[] td;
		for (Iterator<String> it = words.keySet().iterator(); it.hasNext(); ) {
			word = it.next();
			td = words.get(word);
			System.out.println(word + "\t" + td[0] + "\t" + td[1] + "\t" + td[2]);
		}
	}
	
	/**
	 * Gives the most important numWords words
	 * @param numWords Number of words to return
	 * @return String array of words
	 */
	public String[] bestWordList(int numWords) {
		SortedMap<String, Double[]> sortedWords = new TreeMap<String, Double[]>(new SE_Document.ValueComparer(words));
		sortedWords.putAll(words);
		int counter = 0;
		String[] bestwords = new String[numWords];
		for (Iterator<String> it = sortedWords.keySet().iterator(); it.hasNext() && (counter < numWords); counter++) {
			bestwords[counter] = it.next();
		}
		return bestwords;
	}
	
	/**
	 * Override for bestWordList with default number of words of 10
	 * @return String array of best words
	 */
	public String[] bestWordList() {
		return bestWordList(10);
	}
	
	/** inner class to do sorting of the map **/
	private static class ValueComparer implements Comparator<String> {
		private TreeMap<String, Double[]>  _data = null;
		public ValueComparer (TreeMap<String, Double[]> data){
			super();
			_data = data;
		}

         public int compare(String o1, String o2) {
        	 double e1 = ((Double[]) _data.get(o1))[2];
             double e2 = ((Double[]) _data.get(o2))[2];
             if (e1 > e2) return -1;
             if (e1 == e2) return 0;
             if (e1 < e2) return 1;
             return 0;
         }
	}
}