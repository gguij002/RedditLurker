package com.gery.database;

import java.util.Calendar;

import com.gery.redditlurker.R;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils {

	private static int sTheme;

	public final static int THEME_LIGHT = 0;
	public final static int THEME_DARK = 1;
	public final static int THEME_BASE = 2;
	public final static String prefName= "theme";
	private static SharedPreferences prefs;

	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni == null) {
			// There are no active networks.
			return false;
		} else
			return true;
	}
	
	public static void savePrefTheme(Activity activity, int theme)
	{	
		sTheme = theme;
		prefs = activity.getSharedPreferences(prefName, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();

		editor.putInt("id", sTheme);
		editor.commit();
	}
	
	public static void setPrefTheme(Activity activity)
	{
		sTheme = selectPrefTheme(activity);
		onActivityCreateSetTheme(activity);
	}
	
	public static void restartSelf(Activity activity) {
	    AlarmManager am = (AlarmManager)activity.getSystemService(Context.ALARM_SERVICE);
	    am.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + 1000, // one second
	            PendingIntent.getActivity(activity, 0, activity.getIntent(), PendingIntent.FLAG_ONE_SHOT
	                    | PendingIntent.FLAG_CANCEL_CURRENT));
	    activity.finish();
	}
	
	public static int selectPrefTheme(Activity activity)
	{
		prefs = activity.getSharedPreferences(prefName, Context.MODE_PRIVATE);
		
    	return prefs.getInt("id", 100);
	}

	/**
	 * Set the theme of the Activity, and restart it by creating a new Activity
	 * of the same type.
	 */
	public static void changeToTheme(Activity activity, int theme) {
		sTheme = theme;
		activity.finish();
		activity.startActivity(new Intent(activity, activity.getClass()));
	}

	/** Set the theme of the activity, according to the configuration. */
	private static void onActivityCreateSetTheme(Activity activity) {
		switch (sTheme) {
		default:

		case THEME_LIGHT:
			activity.setTheme(R.style.LightTheme);
			break;
		case THEME_DARK:
			activity.setTheme(R.style.DarkTheme);
			break;
		case THEME_BASE:
			activity.setTheme(R.style.AppBaseTheme);
			break;
		}
	}
}
