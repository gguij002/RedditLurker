package com.gery.redditlurker;

import java.util.Date;
import android.text.format.DateUtils;

import org.json.simple.JSONObject;

import android.graphics.Bitmap;

public class StoryInfo {

	public String title;
	public String domain;
	public boolean clicked;
	public String author;
	public boolean over_18;
	public String thumbnail;
	public String subreddit_id;
	public String subreddit;
	public long downs;
	public long ups;
	public Double created;
	public String url;
	public String author_flair_text;// Can be null
	public String name;
	public long num_comments;
	public long score;
	public Bitmap imageBitMap = null;
	public String permalink;

	JSONObject jsonObject;

	public StoryInfo(JSONObject jObject) {
		this.jsonObject = jObject;
	}

	public StoryInfo execute() {
		this.title = (String) jsonObject.get("title");
		this.domain = (String) jsonObject.get("domain");
		this.clicked = (Boolean) jsonObject.get("clicked");
		this.author = (String) jsonObject.get("author");
		this.over_18 = (Boolean) jsonObject.get("over_18");
		this.thumbnail = (String) jsonObject.get("thumbnail");
		this.subreddit_id = (String) jsonObject.get("subreddit_id");
		this.subreddit = capitalize((String) jsonObject.get("subreddit"));
		this.downs = (Long) jsonObject.get("downs");
		this.ups = (Long) jsonObject.get("ups");
		this.created = (Double) jsonObject.get("created");
		this.url = (String) jsonObject.get("url");
		this.permalink = (String) jsonObject.get("permalink");
		this.author_flair_text = (String) jsonObject.get("author_flair_text");
		this.name = (String) jsonObject.get("name");
		this.num_comments = (Long) jsonObject.get("num_comments");
		this.score = (Long) jsonObject.get("score");

		return this;
	}

	private String capitalize(String line) {
		return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}
	
	public Date getCreated_UTC_formatted()
	{
		Date d = new Date(created.longValue() * 1000);
		System.out.println("Current UTC: " +System.currentTimeMillis() +" UTC from Reddit: " + created);
		System.out.println("DATE FORMATTED: " + d.toString());
		return d;
	}

}
