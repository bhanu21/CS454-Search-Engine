package Homework;



import java.awt.List;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mozilla.universalchardet.Constants;

public class PageRank {
	
	public static ArrayList<String> allurl = new ArrayList<String>();
	public static HashMap<String, String> urldoc = new HashMap<String, String>();
	public static HashMap<String, Integer> urlnum = new HashMap<String, Integer>();
	public static HashMap<String, ArrayList<String>> urllinks = new HashMap<String, ArrayList<String>>();

	public static ArrayList<String> getConnectedLinks(String url)
			throws MalformedURLException, IOException {
		// TODO Auto-generated method stub
try{
		Document doc = Jsoup.parse(new URL(url), 2000);
		Elements resultLinks = doc.select("a");

		if (!urlnum.containsKey(url)) {
			urlnum.put(url, resultLinks.size());
		}

		ArrayList<String> links = new ArrayList<String>();
		// System.out.println("number of links: " + resultLinks.size());
		for (Element link : resultLinks) {

			String href = link.attr("href");
			String test = link.absUrl("href");

			if (!urldoc.containsKey(url)) {
				urldoc.put(url, href);
			}
			links.add(test);

		}

		if (!urllinks.containsKey(url)) {
			urllinks.put(url, links);
		}

			for (int i = 0; i < resultLinks.size(); i++) {

				if (!urlnum.containsKey(links.get(i))) {
					allurl.add(links.get(i));
					getConnectedLinks(links.get(i));
				}

			}}
			catch(Exception e)
{}
			return allurl;

		}
	
	public static void main(String[] args) throws MalformedURLException, IOException {
		
		for(String s:getConnectedLinks("http://www.york.ac.uk/teaching/cws/wws/webpage1.html" ))
				{
					System.out.println(s);
				}
	}

	public static Integer getnum(String url) throws MalformedURLException, IOException {
		// TODO Auto-generated method stub
		
		Document doc = Jsoup.parse(new URL(url), 2000);
		Elements resultLinks = doc.select("a");

		
		return resultLinks.size();
	}


	public static String getdoc(String string) throws IOException {
		// TODO Auto-generated method stub
		Document doc=Jsoup.connect(string).get();
		String title = doc.attr(string);

	  System.out.println(title);
		return null;
	}


	public static ArrayList<String> getlist(String url) throws MalformedURLException, IOException {
		
		// TODO Auto-generated method stub
		
		Document doc = Jsoup.parse(new URL(url), 2000);
		Elements resultLinks = doc.select("a");

		
		ArrayList<String> links = new ArrayList<String>();
		// System.out.println("number of links: " + resultLinks.size());
		for (Element link : resultLinks) {

			String test = link.absUrl("href");

			links.add(test);

		}

		return links;
	}

}