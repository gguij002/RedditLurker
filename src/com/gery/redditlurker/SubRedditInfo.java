package com.gery.redditlurker;

import org.json.simple.JSONObject;
import android.widget.ImageView;

public class SubRedditInfo 
{
	String header_title;
	String id;
	String display_name;
	String header_img;
	String url;
	String public_description;
	String name;
	ImageView image;
	JSONObject jsonObject;
	
	public SubRedditInfo(JSONObject jsonObject)
	{
		this.jsonObject = jsonObject;
	}
	
	public SubRedditInfo execute()
	{
		this.header_title = (String) jsonObject.get("header_title");
		this.public_description = (String) jsonObject.get("public_description");
		this.url = (String) jsonObject.get("url");
		this.display_name = ((String) jsonObject.get("display_name"));
		this.id = ((String) jsonObject.get("id"));
		this.header_img = (String) jsonObject.get("header_img");
		this.name = (String) jsonObject.get("name");
		
		return this;
	}
}
