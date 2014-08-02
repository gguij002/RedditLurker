package com.gery.redditlurker;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class RedditRSSReader 
{
	private String URL;
	
	public RedditRSSReader(String URL)
	{
		this.URL = URL;
	}
	
	public Document execute()
	{
		try 
		{
        	URL url = new URL(URL);
        	URLConnection conn = url.openConnection();
        	InputStream input = conn.getInputStream();
        	
        	DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        	Document doc = builder.parse(input);
        	
        	return doc;
        }
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
        }
	}
}
