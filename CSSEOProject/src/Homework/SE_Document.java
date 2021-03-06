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
	
	
	public SE_Document(){}
	
	public SE_Document(String filename, SE_Index parent,org.jsoup.nodes.Document doc) throws IOException {
		
		process(filename,parent,doc);
	}
	public void process(String filename, SE_Index parent,org.jsoup.nodes.Document doc) throws IOException {
		
		Elements eUrl = doc.select("url");
		Page_Location = eUrl.size()==0?filename:eUrl.get(0).html();

		String word;
		StringTokenizer tokens;
		sumof_n_kj = 0;
		vectorlength = 0;
		Double[] tempdata;
		words = new TreeMap<String, Double[]>();
		
		tokens = new StringTokenizer(doc.body().text(), ":; \"\',.[]{}()�!?-/%^<>");
		
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
		
		// Iterate through the words to fill their tf's
		for (Iterator<String> it = words.keySet().iterator(); it.hasNext(); ) {
			word = it.next();
			tempdata = words.get(word);
			tempdata[1] = tempdata[0] / (float) sumof_n_kj;
			words.put(word,tempdata);
			parent.addWordOccurence(word,this.Page_Location);
		}	
		
		
		title = doc.title().toString();
		Elements linksOnPage = doc.select("a[href]");
		Elements metas = doc.select("META");
		if(metas!=null&& metas.size()>0)
			for(Element u :metas)
				Meta_data.put(metas.first().attr("http-equiv"),metas.first().attr("content").toString());
		for(Element u : linksOnPage )
		{
			String url = null;
			if(eUrl.size()>0){
				if(u.absUrl("href")==""){
					if(u.attr("href")=="#")
						continue;
					url = parent.getHttpUrl(u.attr("href"),Page_Location);
				}
				else
					url = u.absUrl("href").toString();
				outgoingLinks.add(url);
			}
			else{
				if(!(new File(u.attr("href"))).exists())
					url = parent.getFileUrl(u.attr("href"),filename);
				outgoingLinks.add(url);
			}
			
			//outgoingLinks.add(url);
			if(parent.documents.containsKey(url))
				parent.documents.get(url).incomingLinks.add(Page_Location);
			else {
				SE_Document newDoc = new SE_Document();
				newDoc.Page_Location = url;
				newDoc.incomingLinks.add(Page_Location);
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
			if(parent.max_tfid<tfidf)
			{
				parent.max_tfid=tfidf;
			}
		}
		vectorlength = Math.sqrt(vectorlength);
	}
	
}