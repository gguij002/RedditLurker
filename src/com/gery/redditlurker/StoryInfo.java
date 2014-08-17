package com.gery.redditlurker;

import org.json.simple.JSONObject;

public class StoryInfo {

	public String name;
	JSONObject jsonObject;
	
	public StoryInfo(JSONObject jObject)
	{
		this.jsonObject = jObject;
	}
	
	public StoryInfo execute()
	{
		return this;
	}
	
}
