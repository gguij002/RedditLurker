package com.gery.redditlurker;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.gery.database.LoadThumbsTask;
import com.gery.database.RedditRSSReader;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;


public class SubRedditChannelActivity extends Activity implements OnScrollListener {
	// List Items
	int currentFirstVisibleItem = 0;
	int currentVisibleItemCount = 0;
	int totalItemCount = 0;
	int currentScrollState = 0;
	Long offset = 4L;
	// List Items

	public boolean isFromSearch = false;
	private ProgressDialog pDialog;
	public List<StoryInfo> storieList;
	private boolean loadingMore;
	private String query;
	private boolean favorite = false;
	SubRedditInfo subReddit = null;
	private String subNname;
	private String displayName;

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
		setOnItemClickListener(this);
	}
	
	public void onResume()
	{
		super.onResume();
	}
	
	@Override
	public void onPause()
	{
		SubRedditsDataSource srDataSource = new SubRedditsDataSource(this);
		srDataSource.open();
		if (favorite) { //Its fav, Add to DB
			if (this.storieList != null && !this.storieList.isEmpty()) {
				if(subReddit == null){
					try {
						subReddit = new LoadSubReddit(this.storieList.get(0).subreddit_id).execute().get();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
				subReddit.favorite = true;
				srDataSource.addSubRedditToDB(subReddit);
				subNname = subReddit.name;
			}
		}
		else{ //Not fav Delete from DB
			srDataSource.deleteSubReddit(subNname);
		}
		srDataSource.close();
		super.onPause();
	}

	/**
	 * Handling intent data
	 * 
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	private void handleIntent(Intent intent) throws InterruptedException, ExecutionException {
		query = null;

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			displayName = query = intent.getStringExtra(SearchManager.QUERY);
			query = "/r/" + query + "/";
			isFromSearch = true;
		} else {// comes from the lists and not the search bar
			query = intent.getStringExtra("subReddit");
			favorite = intent.getBooleanExtra("favorite", false);
			subNname = intent.getStringExtra("subName");
			displayName = intent.getStringExtra("displayName");
		}
		setTitle(displayName);
		AsyncTask<String, String, List<StoryInfo>> var = new LoadStories(this, query).execute();
		if (var.get() == null || var.get().isEmpty()) {
			System.out.println("INVALID SUBREDDIT: " + query);
		}
		isFromSearch = false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
		switch (item.getItemId()) {
		case android.R.id.home:{
             super.onBackPressed();
             return true;
		}

		case R.id.action_fav:
			if (favorite) {
				favorite = false;
				item.setIcon(R.drawable.ic_favorite_off_new);
			} else {
				favorite = true;
				item.setIcon(android.R.drawable.btn_star_big_on);
			}

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_actions, menu);
		MenuItem item = menu.findItem(R.id.action_fav);
		setFavoriteButton(item);
		MenuItem itemSearch = menu.findItem(R.id.action_search_widget);
		itemSearch.setVisible(false);
		return super.onCreateOptionsMenu(menu);
	}

	private void setFavoriteButton(MenuItem item) {
		if (favorite) {
			favorite = true;
			item.setIcon(android.R.drawable.btn_star_big_on);
		} else {
			favorite = false;
			item.setIcon(R.drawable.ic_favorite_off_new);
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
	
	private void setOnItemClickListener(final Context context) {
		final ListView storiesListView = (ListView) findViewById(R.id.subreddit_channel_list);
		storiesListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				StoryInfo subReddit = (StoryInfo) storieList.get(position);
				Intent nextActivity = new Intent(context, ActivityStoryContent.class);
				nextActivity.putExtra("url", subReddit.url);
				startActivity(nextActivity);
			}
		});
		storiesListView.setOnScrollListener(this);
	}

	@Override
	public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		this.currentFirstVisibleItem = firstVisibleItem;
		this.currentVisibleItemCount = visibleItemCount;
		this.totalItemCount = totalItemCount;
	}

	@Override
	public void onScrollStateChanged(AbsListView absListView, int scrollState) {
		this.currentScrollState = scrollState;
		this.isScrollCompleted(absListView.getContext());
	}

	private void isScrollCompleted(Context context) {
		if (this.currentVisibleItemCount > 0 && this.currentScrollState == SCROLL_STATE_IDLE
				&& this.totalItemCount == (currentFirstVisibleItem + currentVisibleItemCount)) {
			/***
			 * In this way I detect if there's been a scroll which has completed
			 ***/
			/*** do the work for load more Stories! ***/
			if (!loadingMore) {
				loadingMore = true;
				new LoadStories(this, query).execute();
			}
		}
	}

	// Load Channel To Store on DB
	class LoadSubReddit extends AsyncTask<String, String, SubRedditInfo> {
		private String subRedditName;

		public LoadSubReddit(String subRedditName) {
			this.subRedditName = subRedditName;
		}

		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected SubRedditInfo doInBackground(String... args) {
			SubRedditInfo sub = addNewSUbredditFromSearch(this.subRedditName);
			return sub;
		}

		protected void onPostExecute(final SubRedditInfo subRedditInfo) {
			super.onPostExecute(subRedditInfo);
		}

		private SubRedditInfo addNewSUbredditFromSearch(String subId) {
			String beforeURL = fetchBeforeURL(subId);

			SubRedditInfo subReddit = createSubReddit(beforeURL);
			isFromSearch = false;
			return subReddit;
		}

		private String fetchBeforeURL(String subId) {
			String afterURL = URLCreateAfter(subId);
			JSONObject subRedditsJSONAfter = new RedditRSSReader(afterURL).execute();
			JSONObject dataAFter = (JSONObject) subRedditsJSONAfter.get("data");
			JSONArray dataAfterRaw = (JSONArray) dataAFter.get("children");
			JSONObject varA = (JSONObject) ((JSONObject) dataAfterRaw.get(0)).get("data");
			String name = (String) varA.get("name");
			String beforeURL = URLBeforeAfter(name);
			return beforeURL;
		}

		private SubRedditInfo createSubReddit(String beforeURL) {
			JSONObject subRedditsJSONB = new RedditRSSReader(beforeURL).execute();
			JSONObject dataB = (JSONObject) subRedditsJSONB.get("data");
			JSONArray dataBRaw = (JSONArray) dataB.get("children");
			JSONObject varB = (JSONObject) ((JSONObject) dataBRaw.get(0)).get("data");
			SubRedditInfo item = new SubRedditInfo(varB).execute();
			String header_image_url = item.header_img;
			if (header_image_url != null && !header_image_url.isEmpty()) {
				item.imageBitMap = new LoadThumbsTask(header_image_url).exceute().imageBitmap;
			}
			return item;
		}

		private String URLCreateAfter(String after) {
			return "http://www.reddit.com/reddits/.json" + "?limit=1&after=" + after;
		}

		private String URLBeforeAfter(String before) {
			return "http://www.reddit.com/reddits/.json" + "?limit=1&before=" + before;
		}
	}

	/**
	 * Background Async Task to Load Stories by making HTTP Request
	 * */
	class LoadStories extends AsyncTask<String, String, List<StoryInfo>> {
		private String subRedditChannel;
		private Context context;

		public LoadStories(Context context, String subReddit) {
			this.context = context;
			subRedditChannel = subReddit;
			loadingMore = true;
		}

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(context);// CHANGE TO GETAPPLICATION
													// CONTEXT
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
			final String STORIES_URL = URLCreate(subRedditChannel, offset.intValue());

			List<StoryInfo> listOfStories = new ArrayList<StoryInfo>();

			// Create List Of Stories
			JSONObject subRedditsJSON = new RedditRSSReader(STORIES_URL).execute();
			if (subRedditsJSON == null || subRedditsJSON.isEmpty())
				return null;

			JSONObject data = (JSONObject) subRedditsJSON.get("data");
			JSONArray listOfSubredditsRaw = (JSONArray) data.get("children");

			int length = listOfSubredditsRaw.size();
			for (int i = 0; i < length; i++) {
				JSONObject var = (JSONObject) ((JSONObject) listOfSubredditsRaw.get(i)).get("data");
				StoryInfo item = new StoryInfo(var).execute();
				String thumb_image_url = item.thumbnail;
				if (thumb_image_url != null && !thumb_image_url.isEmpty() && isURL(thumb_image_url)) {
					item.imageBitMap = getImage(thumb_image_url);
				}
				listOfStories.add(item);
			}

			if (isFromSearch & length > 0) {
				favorite = isFavoriteFromSearch(listOfStories.get(0).subreddit_id);

				// addNewSUbredditFromSearch(listOfStories.get(0).subreddit_id);
			}

			return listOfStories;
		}

		private boolean isFavoriteFromSearch(String subRedditId) {
			SubRedditsDataSource srDataSource = new SubRedditsDataSource(getApplicationContext());
			srDataSource.open();
			boolean fav = srDataSource.isRawSubRedditExist(subRedditId);
			srDataSource.close();
			return fav;
		}

		private boolean isURL(String URL) {
			return URL.contains("http");
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
				mIcon11 = Bitmap.createBitmap(mIcon11, 0, 0, width, height, matrix, true);
			}

			return mIcon11;
		}

		private String URLCreate(String subReddit, int offset) {
			String after = "";
			if (storieList.size() > 0)
				after = storieList.get(storieList.size() - 1).name;

			return "http://www.reddit.com" + subReddit + ".json" + "?limit=" + offset + "&after=" + after;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(final List<StoryInfo> storiesInfoList) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			loadingMore = false;
			if (storiesInfoList == null)
				return;
			storieList.addAll(storieList.size(), storiesInfoList);
			final ListView storiesListView = (ListView) findViewById(R.id.subreddit_channel_list);
			final int positionToSave = storiesListView.getFirstVisiblePosition();
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
