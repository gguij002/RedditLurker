package com.gery.database;

import java.io.ByteArrayOutputStream;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class SubRedditsDataSource {
	
	  private static boolean addedItem = false;

	  // Database fields
	  private SQLiteDatabase database;
	  private MySQLiteHelper dbHelper;
	  private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
	      MySQLiteHelper.COLUMN_SUBREDDIT, MySQLiteHelper.COLUMN_SUBIMAGE };

	  public SubRedditsDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	  }

	  public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	  }

	  public void close() {
	    dbHelper.close();
	  }

	  public void addSubRedditToDB(SubRedditInfo subReddit) {
		  	boolean exists = isRawSubRedditExist(subReddit.getId());
			if(exists) {
				System.out.println("TRIED!! to add SubReddit with URL and id: " + subReddit.getUrl()+" "+subReddit.getId());
				return;
			}
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COLUMN_SUBREDDIT, subReddit.getJsonObject().toJSONString());
			values.put(MySQLiteHelper.COLUMN_ID, subReddit.getId());
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			subReddit.getImageBitMap().compress(Bitmap.CompressFormat.PNG, 100, bos);
			values.put(MySQLiteHelper.COLUMN_SUBIMAGE, bos.toByteArray());
			
			database.insert(MySQLiteHelper.TABLE_SUBREDDITS, null, values);
			System.out.println("SubReddit added with URL and id: " + subReddit.getUrl()+" "+subReddit.getId());
		}
	  
	  public static boolean AddedItem()
	  {
		  return addedItem;
	  }
	  
	  public static void AddedItemTrue()
	  {
		  addedItem = true;
	  }
	  
	  public static void AddedItemFalse()
	  {
		  addedItem = false;
	  }

	  public void deleteSubReddit(SubRedditInfo subReddit) {
		    String id = subReddit.getId();
		    System.out.println("SubReddit deleted with id: " + id);
		    
			String whereClause = MySQLiteHelper.COLUMN_ID + " = " + "\""+id+"\"";
			database.delete(MySQLiteHelper.TABLE_SUBREDDITS, whereClause, null);
	  }
	  
	  private Cursor getAllSubRedditRaw()
	  {
		  return database.query(MySQLiteHelper.TABLE_SUBREDDITS,
			        allColumns, null, null, null, null, null);
	  }
	  
	  public List<String> getAllSubRedditsID()
	  {
		  List<String> ids = new ArrayList<String>();
		  Cursor cursor = getAllSubRedditRaw();
		  cursor.moveToFirst();
		  while (!cursor.isAfterLast()) {
		    	String idFromCursor = cursor.getString(0);
		    	ids.add(idFromCursor);
		    	cursor.moveToNext();
		  }
		  cursor.close();
		  return ids;
	  }
	  
	  private boolean isRawSubRedditExist(String id)
	  {
		  Cursor cursor = getAllSubRedditRaw();
		  cursor.moveToFirst();
		    while (!cursor.isAfterLast()) {
		    	String idFromCursor = cursor.getString(0);
		    	
		    	if(idFromCursor.equalsIgnoreCase(id))//matchFound
		    	{
		    		System.out.println("FOUND MATCH ID: " + idFromCursor);
		    		cursor.close();
		    		return true;
		    	}
		    	cursor.moveToNext();
		    }
		    // make sure to close the cursor
		    cursor.close();
		    return false;
	  }

	  public List<SubRedditInfo> getAllSubReddit() {
	    List<SubRedditInfo> subReddits = new ArrayList<SubRedditInfo>();
	    
	    Cursor cursor = getAllSubRedditRaw();

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	      JSONObject jObject = 	getJsonFromString(cursor.getString(1));
	      SubRedditInfo subRedditFromDB = new SubRedditInfo(jObject).execute();
	      
	      byte[] var = cursor.getBlob(2);
	      if(var != null)
	      {
	    	  Bitmap imageBitMap = BitmapFactory.decodeByteArray(var, 0, var.length);
	    	  subRedditFromDB.setImageBitMap(imageBitMap);
	      }
	      subReddits.add(subRedditFromDB);
	      cursor.moveToNext();
	    }
	    // make sure to close the cursor
	    cursor.close();
	    return subReddits;
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
	} 
