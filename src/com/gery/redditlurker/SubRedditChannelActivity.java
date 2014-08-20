package com.gery.redditlurker;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.gery.database.SubRedditsDataSource;

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
		Long offset = 4L;
		// List Items
		
		public boolean isFromSearch = false;
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
	 
	        try {
				handleIntent(getIntent());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
	    }
	 
	    @Override
	    protected void onNewIntent(Intent intent) {
	        setIntent(intent);
	        try {
				handleIntent(intent);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
	    }
	 
	    /**
	     * Handling intent data
	     * @throws ExecutionException 
	     * @throws InterruptedException 
	     */
	    private void handleIntent(Intent intent) throws InterruptedException, ExecutionException {
	    	String query = null;
	    	
	        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	             query = intent.getStringExtra(SearchManager.QUERY);
	             query = "/r/"+query+"/"; 
	             isFromSearch = true;
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
	        AsyncTask<String, String, List<StoryInfo>> var = new LoadStories(query).execute();
	        if(var.get() == null || var.get().isEmpty()) {
	        	System.out.println("INVALID SUBREDDIT: " + query);
	        }
	        isFromSearch= false;
	    }
	    
		 /**
			 * Background Async Task to Load Stories by making HTTP Request
			 * */
			class LoadStories extends AsyncTask<String, String, List<StoryInfo>> {
				private String subRedditChannel;
				
				public LoadStories(String subReddit) {
					subRedditChannel = subReddit;
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
					final String STORIES_URL = URLCreate(subRedditChannel,offset.intValue());
					System.out.println("String Pulling Data URL COMPLETED: "+ STORIES_URL);
					List<StoryInfo> listOfStories = new ArrayList<StoryInfo>();

					// Create List Of Stories
					JSONObject subRedditsJSON = new RedditRSSReader(
							STORIES_URL).execute();
					if(subRedditsJSON == null || subRedditsJSON.isEmpty())
						return null;
					
					JSONObject data = (JSONObject) subRedditsJSON.get("data");
					JSONArray listOfSubredditsRaw = (JSONArray) data.get("children");

					int length = listOfSubredditsRaw.size();
					for (int i = 0; i < length; i++) {
						JSONObject var = (JSONObject) ((JSONObject) listOfSubredditsRaw.get(i)).get("data");
						StoryInfo item = new StoryInfo(var).execute();
						String thumb_image_url = item.thumbnail;
						if (thumb_image_url != null && !thumb_image_url.isEmpty()) {
							item.imageBitMap = getImage(thumb_image_url);
						}
						listOfStories.add(item);
					}
					
					if(isFromSearch & length > 0)
					{
						addNewSUbredditFromSearch(listOfStories.get(0).subreddit_id);
					}
					
					return listOfStories;
				}
				
				private void addNewSUbredditFromSearch(String subId)
				{
					String beforeURL = fetchBeforeURL(subId);
					System.out.println("BEFORE URL IS: "+ beforeURL);
					
				   	SubRedditsDataSource srDataSource = new SubRedditsDataSource(getApplicationContext());
		    		srDataSource.open();
		        	SubRedditInfo subReddit = createSubReddit(beforeURL);
					srDataSource.addSubRedditToDB(subReddit);
					AllSubRedditsFragment.addedItem = true;
					srDataSource.close();
					isFromSearch = false;
				}
				
				private String fetchBeforeURL(String subId)
				{
					String afterURL = URLCreateAfter(subId);
					JSONObject subRedditsJSONAfter = new RedditRSSReader(
							afterURL).execute();
					JSONObject dataAFter = (JSONObject) subRedditsJSONAfter.get("data");
					JSONArray dataAfterRaw = (JSONArray) dataAFter.get("children");
					JSONObject varA = (JSONObject) ((JSONObject) dataAfterRaw.get(0)).get("data");
					String name = (String) varA.get("name");
					String beforeURL = URLBeforeAfter(name);
					return beforeURL;
				}
				
				private SubRedditInfo createSubReddit(String beforeURL)
				{
					JSONObject subRedditsJSONB = new RedditRSSReader(
							beforeURL).execute();
					JSONObject dataB = (JSONObject) subRedditsJSONB.get("data");
					JSONArray dataBRaw = (JSONArray) dataB.get("children");
					JSONObject varB = (JSONObject) ((JSONObject) dataBRaw.get(0)).get("data");
					SubRedditInfo item = new SubRedditInfo(varB).execute();
					String header_image_url = item.header_img;
					if (header_image_url != null && !header_image_url.isEmpty()) {
						item.imageBitMap = getImage(header_image_url);
					}
					return item;
				}
				
				private String URLCreateAfter(String after) {
					return "http://www.reddit.com/reddits/.json" + "?limit=1&after=" + after;
				}
				
				private String URLBeforeAfter(String before) {
					return "http://www.reddit.com/reddits/.json" + "?limit=1&before=" + before;
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
					if(storiesInfoList == null)
						return;
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
