package com.gery.redditlurker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RedditRSSReader 
{
	private String URL;
	
	public RedditRSSReader(String URL)
	{
		this.URL = URL;
	}
	
	public JSONObject execute()
	{
		BufferedReader streamReader = null;
		try {
			URL url = new URL(URL);
			URLConnection conn = url.openConnection();
			InputStream input = conn.getInputStream();
			streamReader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
		} catch (Exception e1) {
			System.out.println("Error at URL");
			e1.printStackTrace();
		}
		
		StringBuilder responseStrBuilder = new StringBuilder();
		String inputStr = null;
		try {
			while ((inputStr = streamReader.readLine()) != null)
			    responseStrBuilder.append(inputStr);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    
	    JSONParser parser = new JSONParser();
	    JSONObject jsonObject = null;
    	try {
    		jsonObject = (JSONObject)parser.parse(inputStr.toString());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return jsonObject;
 }
}
