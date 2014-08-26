package com.gery.redditlurker;

import org.json.simple.JSONObject;

import android.graphics.Bitmap;

public class SubRedditInfo implements Comparable<SubRedditInfo>
{
	String header_title;
	String id;
	String display_name;//
	String header_img;
	String url;
	String public_description;
	String name;
	Bitmap imageBitMap = null;
	boolean favorite= false;
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
		try
		{
			this.favorite = (Boolean) jsonObject.get("favorite");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		return this;
	}
	
	public void addFavToJson(boolean favorite)
	{
		try{
		this.getJsonObject().put("favorite", favorite);
		}
		catch(Exception e)
		{e.printStackTrace();}
	}
	
	public void addFavToJson()
	{
		try{
		this.getJsonObject().put("favorite", this.favorite);
		}
		catch(Exception e)
		{e.printStackTrace();}
	}
	
	@Override
	public int compareTo(SubRedditInfo subredditInfo)
	{
		if(this.id.equalsIgnoreCase(subredditInfo.id))
			return 0;
		else 
			return 1;
		
	}
	
	@Override
    public boolean equals(Object object)
    {
		System.out.println("EQUALS");
        boolean sameSame = false;

        if (object != null && object instanceof SubRedditInfo)
        {
            sameSame = this.name.equalsIgnoreCase(((SubRedditInfo) object).name);
        }

        return sameSame;
    }
	
	
	public Bitmap getImageBitMap()
	{
		return imageBitMap;
	}
	
	public void setImageBitMap(Bitmap bitmap)
	{
		this.imageBitMap = bitmap;
	}

	public JSONObject getJsonObject() {
		return jsonObject;
	}

	public void setJsonObject(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String Id)
	{
		this.id = Id;
	}
	
	public String getUrl() {
		return url;
	}
}
