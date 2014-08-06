package com.gery.redditlurker;

import org.json.simple.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import android.widget.ImageView;

public class SubRedditInfo 
{
	String header_title;
	String id;
	String display_name;
	String header_img;
	String url;
	String public_description;
	ImageView image;
	JSONObject jsonObject;
	
	public SubRedditInfo(JSONObject jsonObject)
	{
		this.jsonObject = jsonObject;
	}
	
	public SubRedditInfo execute()
	{
		this.header_title = ((String) jsonObject.get("header_title")).trim();
		this.public_description = ((String) jsonObject.get("public_description")).trim();
		this.url = ((String) jsonObject.get("url")).trim();
		this.display_name = ((String) jsonObject.get("display_name")).trim();
		this.id = ((String) jsonObject.get("id")).trim();
		this.header_img = ((String) jsonObject.get("header_img")).trim();
		
		return this;
	}
}
