package com.gery.redditlurker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
		String jsonString = null;
		try {
			URL url = new URL(URL);
			URLConnection conn = url.openConnection();
			InputStream input = conn.getInputStream();
			jsonString = getStringfromInputReader(input);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	    JSONParser parser = new JSONParser();
	    JSONObject jsonObject = null;
    	try {
    		jsonObject = (JSONObject)parser.parse(jsonString);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return jsonObject;
 }
	
	private String getStringfromInputReader(InputStream in) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        reader.close();
        return out.toString();
	}
}
