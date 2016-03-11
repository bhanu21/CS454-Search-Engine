package Homework;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RankThread extends Thread {
	String filename;
	File file;
	HashMap<String,HashSet> IncomingLinks =new HashMap<String,HashSet>();
	
	 public RankThread(String filename, File file) {
		super();
		this.filename = filename;
		this.file = file;
		//this.IncomingLinks=IncomingLinks;
	}

	public void run()
	   {
		try {
			IncomingLinks.put(file.getPath(),IncominLinks(file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
						
						System.out.println(b);
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

}
