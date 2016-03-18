package Homework;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlThread extends Thread
{
	String filePath,url;

	public CrawlThread(String url, String filePath)
	   {
	      this.url = url;
	      this.filePath = filePath;
	   }

	   @Override
	   public void run()
	   {
		   try{
				Connection connection = Jsoup.connect(url);
				Document htmlDocument = connection
						.ignoreContentType(true)
						.get();
				htmlDocument.select("head").first().children().first()
						.before("<url>"+url.toString()+"</url>");
				
				File filename = new File(filePath);

				BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
				bw.write(htmlDocument.html());
				bw.close(); 				
			}
			catch(Exception e)
			{
				/*System.out.println("url: "+url);
				System.out.println("exception: "+e.getMessage());*/
			} 
	   }

}
