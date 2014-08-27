package com.gery.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

public static int counter = 0;	
	
  public static final String TABLE_SUBREDDITS = "subreddits";
  public static final String COLUMN_ID = "_id";
  public static final String COLUMN_SUBREDDIT = "subreddit";
  public static final String COLUMN_SUBIMAGE = "subimage";
  public static final String COLUMN_FAVORITE = "favorite";

  private static final String DATABASE_NAME = "subreddit.db";
  private static final int DATABASE_VERSION = 1;

  // Database creation sql statement
  private static final String DATABASE_CREATE = "create table " + TABLE_SUBREDDITS 
	+ "(" + COLUMN_ID + " text not null, " 
		  + COLUMN_SUBREDDIT + " text not null, "
		  + COLUMN_SUBIMAGE + " image blob, " 
		  + COLUMN_FAVORITE + " BOOLEAN NOT NULL CHECK ("+COLUMN_FAVORITE+" IN (0,1)));";

  public MySQLiteHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    System.out.println("MySQLiteHelper exec with COntetx: " + context.getClass() +"Counter: " +counter++ );
   // context.deleteDatabase(getDatabaseName()); //USE TO WIPE DB
  }

  @Override
  public void onCreate(SQLiteDatabase database) {
	  database.execSQL(DATABASE_CREATE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(MySQLiteHelper.class.getName(),
        "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBREDDITS);
    onCreate(db);
  }
} 
