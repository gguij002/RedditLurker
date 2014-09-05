package com.gery.redditlurker;

import org.json.simple.JSONObject;

import android.graphics.Bitmap;

public class SubRedditInfo implements Comparable<SubRedditInfo> {
	String header_title;
	String id;
	String display_name;//
	String header_img;
	String url;
	String public_description;
	String name;
	Bitmap imageBitMap = null;
	boolean favorite = false;
	JSONObject jsonObject;

	public SubRedditInfo(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	public SubRedditInfo execute() {
		this.header_title = (String) jsonObject.get("header_title");
		this.public_description = (String) jsonObject.get("public_description");
		this.url = (String) jsonObject.get("url");
		this.display_name = capitalize((String) jsonObject.get("display_name"));
		this.id = ((String) jsonObject.get("id"));
		this.header_img = (String) jsonObject.get("header_img");
		this.name = (String) jsonObject.get("name");
		return this;
	}

	private String capitalize(String line) {
		return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}
	@Override
	public int compareTo(SubRedditInfo subredditInfo) {
		if (this.id.equalsIgnoreCase(subredditInfo.id))
			return 0;
		else
			return 1;

	}

	@Override
	public String toString() {
		String subRedString;

		subRedString = "DisplayName: " + this.display_name + " Name: " + this.name + " Favorite: " + this.favorite;

		return subRedString;
	}

	@Override
	public boolean equals(Object object) {
		System.out.println("EQUALS");
		boolean sameSame = false;

		if (object != null && object instanceof SubRedditInfo) {
			sameSame = this.name.equalsIgnoreCase(((SubRedditInfo) object).name);
		}

		return sameSame;
	}

	public Bitmap getImageBitMap() {
		return imageBitMap;
	}

	public void setImageBitMap(Bitmap bitmap) {
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

	public void setId(String Id) {
		this.id = Id;
	}

	public String getUrl() {
		return url;
	}

	public int getFavoriteAsInt() {
		int flag = (favorite) ? 1 : 0;
		return flag;
	}

	public void setFavoriteFromInt(int intFav) {
		this.favorite = (intFav == 1) ? true : false;
	}

	public String getName() {
		return this.name;
	}
}
