package com.gery.userpreferences;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.gery.redditlurker.SubRedditInfo;

import android.content.Context;
import android.content.SharedPreferences;

/** stores the user object in SharedPreferences */
public class UserPrefs{
 
    /** This application's preferences label */
    private static final String PREFS_NAME = "com.gery.userpreferences.UserPrefs";
 
    /** This application's preferences */
    private static SharedPreferences settings;
    
   /** This application's settings editor*/
   private static SharedPreferences.Editor editor;
 
   /** Constructor takes an android.content.Context argument*/
   public UserPrefs(Context ctx){
        if(settings == null){
           settings = ctx.getSharedPreferences(PREFS_NAME,
                                               Context.MODE_PRIVATE );
        }
       /*
        * Get a SharedPreferences editor instance.
        * SharedPreferences ensures that updates are atomic
        * and non-concurrent
        */
        editor = settings.edit();    
   }

   /** The prefix for flattened user keys */
   public static final String KEY_PREFIX =
               "com.gery.userpreferences.KEY";
    
   /** Method to return a unique key for any field belonging to a given object
   * @param id of the object
   * @param fieldKey of a particular field belonging to that object
   * @return key String uniquely identifying the object's field
   */
   private String getFieldKey(int id, String fieldKey) {
          return  KEY_PREFIX + id + "_" + fieldKey;
   }
   
   /** generic field keys */
   private static final String KEY_JSON = "com.gery.userpreferences.KEY_USERNAME";
   
  /** Store or Update */
  public void setUser(SubRedditInfo subReddit){
      if(subReddit == null)
        return; // don't bother
       
      int id = subReddit.getId().hashCode();
      editor.putString(
                 getFieldKey(id, KEY_JSON),
                 subReddit.getJsonObject().toJSONString() );
      editor.commit();
  }
   
  /** Retrieve */
  public SubRedditInfo getUser(int id){
      String name = settings.getString(getFieldKey(id, KEY_JSON), "" ); // default value
      JSONParser parser = new JSONParser();
      JSONObject jsonObject = null;
      try {
    	  jsonObject =  (JSONObject)parser.parse(name);
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}	
      return new SubRedditInfo(jsonObject);
  }
   
  /** Delete */
  public void deleteUser(SubRedditInfo user){
     if(user == null)
        return; // don't bother
   
     int id = user.getId().hashCode();
     editor.remove( getFieldKey(id, KEY_JSON) );
           
     editor.commit();
  }
}
