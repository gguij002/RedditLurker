package com.gery.redditlurker;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

public class SubRedditChannelActivity extends Activity  
{
		// List Items
		int currentFirstVisibleItem = 0;
		int currentVisibleItemCount = 0;
		int totalItemCount = 0;
		int currentScrollState = 0;
		boolean loadingMore = false;
		Long offset = 4L;
		// List Items
		
		private ProgressDialog pDialog;
		List<StoryInfo> storieList;
	
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_subreddit);
	 
	        // get the action bar
	        ActionBar actionBar = getActionBar();
	        storieList = new ArrayList<StoryInfo>();
	        
	        // Enabling Back navigation on Action Bar icon
	        actionBar.setDisplayHomeAsUpEnabled(true);
	 
	        handleIntent(getIntent());
	    }
	 
	    @Override
	    protected void onNewIntent(Intent intent) {
	        setIntent(intent);
	        handleIntent(intent);
	    }
	 
	    /**
	     * Handling intent data
	     */
	    private void handleIntent(Intent intent) {
	    	String query = null;
	    	
	        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	             query = intent.getStringExtra(SearchManager.QUERY);
	             query = "/r/"+query+"/"; 
	            
	            /**
	             * Use this query to display search results like 
	             * 1. Getting the data from SQLite and showing in listview 
	             * 2. Making webrequest and displaying the data 
	             * For now we just display the query only
	             */
	        }
	        else //comes from the lists and not the search bar
	        {
	        	query = intent.getStringExtra("subReddit");
	        }
	        System.out.println("Passed from Views Query: "+ query);
	        new LoadStories(query).execute();
	    }
	    
		 /**
			 * Background Async Task to Load Stories by making HTTP Request
			 * */
			class LoadStories extends AsyncTask<String, String, List<StoryInfo>> {
				private String subRedditChannel;
				
				public LoadStories(String subReddit) {
					subRedditChannel = subReddit;
					loadingMore = true;
				}

				/**
				 * Before starting background thread Show Progress Dialog
				 * */
				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					pDialog = new ProgressDialog(SubRedditChannelActivity.this);
					pDialog.setMessage("Loading Stories ...");
					pDialog.setIndeterminate(false);
					pDialog.setCancelable(false);
					pDialog.show();
				}

				/**
				 * getting SubReddits
				 * */
				protected List<StoryInfo> doInBackground(String... args) {
					// "http://reddit.com/r/reddits.rss?limit=[limit]&after=[after]";
					final String REDDIT_SUBREDDITS_URL = URLCreate(subRedditChannel,offset.intValue());
					System.out.println("String Pulling Data URL COMPLETED: "+ REDDIT_SUBREDDITS_URL);
					List<StoryInfo> listOfStories = new ArrayList<StoryInfo>();

					// Create List Of Stories
					JSONObject subRedditsJSON = new RedditRSSReader(
							REDDIT_SUBREDDITS_URL).execute();
					JSONObject data = (JSONObject) subRedditsJSON.get("data");
					JSONArray listOfSubredditsRaw = (JSONArray) data.get("children");

					int length = listOfSubredditsRaw.size();
					for (int i = 0; i < length; i++) {
						JSONObject var = (JSONObject) ((JSONObject) listOfSubredditsRaw.get(i)).get("data");
						StoryInfo item = new StoryInfo(var).execute();//(var).execute();
						String thumb_image_url = item.thumbnail;
						if (thumb_image_url != null && !thumb_image_url.isEmpty()) {
							item.imageBitMap = getImage(thumb_image_url);
						}
						listOfStories.add(item);
					}
					return listOfStories;
				}

				private Bitmap getImage(String url) {
					Bitmap mIcon11 = null;
					try {
						InputStream in = new java.net.URL(url).openStream();
						mIcon11 = BitmapFactory.decodeStream(in);
					} catch (Exception e) {
						Log.e("Error", e.getMessage());
						e.printStackTrace();
						return null;
					}
					int width = mIcon11.getWidth();
					int height = mIcon11.getHeight();

					if (width > 341 || height > 201) {
						// calculate the scale
						float scaleWidth = ((float) 100) / width;
						float scaleHeight = ((float) 100) / height;
						// create a matrix for the manipulation
						Matrix matrix = new Matrix();
						matrix.postScale(scaleWidth, scaleHeight);
						mIcon11 = Bitmap.createBitmap(mIcon11, 0, 0, width, height,
								matrix, true);
					}

					return mIcon11;
				}

				private String URLCreate(String subReddit, int offset) {
					String after = "";
					if (storieList.size() > 0)
						after = storieList.get(storieList.size() - 1).name;//Get subreddits after This "Name"
					return "http://www.reddit.com"+subReddit+".json" + "?limit=" + offset+ "&after=" + after;
				}

				/**
				 * After completing background task Dismiss the progress dialog
				 * **/
				protected void onPostExecute(final List<StoryInfo> storiesInfoList) {
					// dismiss the dialog after getting all products
					pDialog.dismiss();
					loadingMore = false;
					storieList.addAll(storieList.size(), storiesInfoList);
					final ListView storiesListView = (ListView) findViewById(R.id.subreddit_channel_list);
					final int positionToSave = storiesListView
							.getFirstVisiblePosition();
					// updating UI from Background Thread
					runOnUiThread(new Runnable() {
						public void run() {
							ChannelBaseAdapter var = new ChannelBaseAdapter(getApplicationContext(), storieList);
							storiesListView.setAdapter(var);
							storiesListView.setSelection(positionToSave);
						}
					});
					super.onPostExecute(storiesInfoList);
				}
			}

}
