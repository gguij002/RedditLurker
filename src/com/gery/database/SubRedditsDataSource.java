package com.gery.database;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.gery.redditlurker.SubRedditInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class SubRedditsDataSource {

	  // Database fields
	  private SQLiteDatabase database;
	  private MySQLiteHelper dbHelper;
	  private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
	      MySQLiteHelper.COLUMN_SUBREDDIT };
	  private Context context;

	  public SubRedditsDataSource(Context context) {
		this.context = context;
		dbHelper = new MySQLiteHelper(context);
	  }

	  public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	  }

	  public void close() {
	    dbHelper.close();
	  }

	  public SubRedditInfo createSubReddit(SubRedditInfo subReddit) {
		  SubRedditInfo temp = subRedditExists(subReddit.getUrl());
		if(temp != null)
		{
			System.out.println("TRIED!! to add SubReddit with URL and id: " + temp.getUrl()+" "+temp.getId());
			return temp;
		}
	    ContentValues values = new ContentValues();
	    values.put(MySQLiteHelper.COLUMN_SUBREDDIT, subReddit.getJsonObject().toJSONString());
	    long insertId = database.insert(MySQLiteHelper.TABLE_SUBREDDITS, null, values);
	    Cursor cursor = database.query(MySQLiteHelper.TABLE_SUBREDDITS,
	        allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
	        null, null, null);
	    cursor.moveToFirst();
	    SubRedditInfo newSubReddit = cursorToSubReddit(cursor);
	    cursor.close();
	    System.out.println("SubReddit added with URL and id: " + newSubReddit.getUrl()+" "+newSubReddit.getId());
	    return newSubReddit;
	  }

	  public void deleteSubReddit(SubRedditInfo subReddit) {
	    String id = subReddit.getId();
	    System.out.println("SubReddit deleted with id: " + id);
	    database.delete(MySQLiteHelper.TABLE_SUBREDDITS, MySQLiteHelper.COLUMN_ID
	        + " = " + id, null);
	  }

	  public List<SubRedditInfo> getAllSubReddit() {
	    List<SubRedditInfo> subReddits = new ArrayList<SubRedditInfo>();

	    Cursor cursor = database.query(MySQLiteHelper.TABLE_SUBREDDITS,
	        allColumns, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      SubRedditInfo subRedditFromDB = cursorToSubReddit(cursor);
	      subReddits.add(subRedditFromDB);
	      cursor.moveToNext();
	    }
	    // make sure to close the cursor
	    cursor.close();
	    return subReddits;
	  }
	  
	  public SubRedditInfo subRedditExists(String subRedditID)
	  {
		  List<SubRedditInfo> subReddits = this.getAllSubReddit();
		  for(SubRedditInfo sr : subReddits)
		  {
			  if(sr.getUrl().equalsIgnoreCase(subRedditID))
				  return sr.execute();
		  }
		  return null;
	  }

	  private JSONObject getJsonFromString(String jsonString)
	  {
		  JSONParser parser = new JSONParser();
		    JSONObject jsonObject = null;
	    	try {
	    		jsonObject = (JSONObject)parser.parse(jsonString);
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    	return jsonObject;
	  }
	  
	  private SubRedditInfo cursorToSubReddit(Cursor cursor) {
		//  String subRedditId = cursor.getString(0);
		  String jsonString = cursor.getString(1);
		  JSONObject jObject = getJsonFromString(jsonString);
		  
		  SubRedditInfo subReddit = new SubRedditInfo(jObject).execute();
	//	  subReddit.setId(subRedditId);
//	    subReddit.setId(cursor.getLong(0));
//	    subReddit.setSubReddit(cursor.getString(1));
	    return subReddit;
	  }
	} 
