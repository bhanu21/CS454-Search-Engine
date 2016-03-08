package Homework;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class test {
	static List<File> files = new ArrayList();
	HashMap<String,HashSet> IncomingLinks = new HashMap<String , HashSet>();
	
	public String ChangeURL (Element e)
	{
		
		String baseurl="C:/Temp/en";
		String a=(baseurl+e.getElementsByTag("a").toString().replace("../","")
				.replace("<a href=","/").replace("</a>", " ").replace(">", " ").replace("\"", ""));
		String b= a.replace(a.substring(a.lastIndexOf(".html")+5),"");
		/*System.out.println((baseurl+e.getElementsByTag("a").toString().replace("../","")
				.replace("<a href=","/").replace("</a>", " ").replace(">", " ").replace("\"", ""))
				);*/
		//System.out.println(b);
		return b;
	}
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
	public HashSet IncominLinks(File filePath) throws IOException
	{
		HashSet<String> incomingLinks =  new HashSet<String>();
		
				 Document doc1 = Jsoup.parse(filePath, "UTF-8");
		//Document doc1 = Jsoup.connect("https://www.google.com/").get();
		Elements linksOnPage = doc1.select("a[href]");
		for(Element e: linksOnPage)
		{	
			try{
				if(e.absUrl("href")!="")
				{
				}
				else{
					String b= new test().ChangeURL(e);
					File f = new File (b);
				//	System.out.println(f);
				 Document doc = Jsoup.parse(f, "UTF-8");
				 
				 Elements linksOnThisPage = doc.select("a[href]");
				 for(Element e1 : linksOnThisPage )
					
					{	if(e1.absUrl("href")!="")
					{
					}
					else  {
						
						String c= new test().ChangeURL(e1);
						if(b.contentEquals(c)){}
						else{
						if(c.compareToIgnoreCase(filePath.getPath())==0)
							{
							incomingLinks.add(b);
						//	System.out.println(f);
							}
						}
						}
						
					
						
					}
				 
				 
				
				}
			}
			catch(Exception ex)
			{}
			/*try{
			Document doc2 = Jsoup.connect(e.absUrl("href")).get();
			
			Elements linksOnThisPage = doc2.select("a[href]");
			for(Element e1 : linksOnThisPage )
			{	
				if(e1.absUrl("href").contentEquals("C:\\Temp\\en\\articles\\(\\1\\5\\(15810)_1994_JR1_9064.html"))
			{

				incomingLinks.add(e1.absUrl("href"));
				System.out.println(e1.absUrl("href"));
			}
				
			}
			}
			catch(Exception ex)
			{}
			*/
			
		}
		//System.out.println(incomingLinks);
		return incomingLinks;

	}

	public static void main(String[] args) throws IOException {
				test t=new test();
				
				for(File file: list(new File("C:/Temp/en/")))
						{
							t.IncomingLinks.put(file.getPath(),t.IncominLinks(file));
							
						}
				for(String link: t.IncomingLinks.keySet())
				{
					System.out.println(link );
					System.out.println(t.IncomingLinks.get(link));
					
				}
					
		}
}