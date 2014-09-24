package com.gery.redditlurker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.gery.database.LoadThumbsTask;
import com.gery.database.RedditRSSReader;
import com.gery.database.SubRedditsDataSource;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class ActivitySubRedditChannel extends Activity implements OnScrollListener {
	// List Items
	int currentFirstVisibleItem = 0;
	int currentVisibleItemCount = 0;
	int totalItemCount = 0;
	int currentScrollState = 0;
	Long offset = 20L;
	// List Items

	public boolean isFromSearch = false;
	public List<StoryInfo> storieList;
	private boolean loadingMore;

	SubRedditInfo subReddit = null;
	private byte[] headerBarThumb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		storieList = new ArrayList<StoryInfo>();
		handleIntent(getIntent());

		setContentView(R.layout.activity_subreddit);

		// get the action bar
		ActionBar actionBar = getActionBar();

		// Enabling Back navigation on Action Bar icon
		actionBar.setDisplayHomeAsUpEnabled(true);

		setOnItemClickListener(this);
	}

	private void setHeaderBarThumb(Bitmap thumbBitmap) {
		if (thumbBitmap != null) {
			Resources res = getResources();
			BitmapDrawable icon = null;

			icon = new BitmapDrawable(res, thumbBitmap);
			getActionBar().setIcon(icon);
		} else
			getActionBar().setIcon(R.drawable.ic_launcher);
	}

	private void handleIntent(Intent intent) {

		subReddit = new SubRedditInfo(new JSONObject());
		subReddit.favorite = false;

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			subReddit.display_name = intent.getStringExtra(SearchManager.QUERY);
			subReddit.url = "/r/" + subReddit.display_name + "/";
			isFromSearch = true;
		} else {// comes from the lists and not the search bar
			this.ReCreateSubReddit(intent);
			setHeaderBarThumb(subReddit.imageBitMap);
		}

		setTitle("LurkR: " + subReddit.display_name);

		new LoadStories(this, subReddit.url).execute();

		isFromSearch = false;
	}

	private void ReCreateSubReddit(Intent intent) {
		String jsonObjectAsString = intent.getStringExtra("subRedditJSON");
		JSONObject jsonObject = getJsonFromString(jsonObjectAsString);
		subReddit = new SubRedditInfo(jsonObject).execute();

		byte[] imageBitmapArr = this.headerBarThumb = intent.getByteArrayExtra("imageBitMap");
		if (imageBitmapArr != null) {
			Bitmap thumbBitmap = BitmapFactory.decodeByteArray(imageBitmapArr, 0, imageBitmapArr.length);
			subReddit.setImageBitMap(thumbBitmap);
		}
		boolean fav = intent.getBooleanExtra("favorite", false);
		subReddit.favorite = fav;

	}

	private JSONObject getJsonFromString(String jsonString) {
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) parser.parse(jsonString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObject;
	}

	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		SubRedditsDataSource srDataSource = new SubRedditsDataSource(this);
		srDataSource.open();
		if (subReddit.favorite) { // Its fav, Add to DB
			if (this.storieList != null)// && !this.storieList.isEmpty()
			{
				if (!srDataSource.isRawSubRedditExist(subReddit.name)) 
				{
					try {
						System.out.println("Loading subreddit to add to DB: Highly expensive and must be avouded");
						subReddit = new LoadSubReddit(this, this.storieList.get(0).subreddit_id).execute().get();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					subReddit.favorite = true;
					srDataSource.addSubRedditToDB(subReddit);
				}
			}
		} else { // Not fav Delete from DB
			srDataSource.deleteSubReddit(subReddit.name);
		}
		srDataSource.close();
		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
		switch (item.getItemId()) {
		case android.R.id.home: {
			super.onBackPressed();
			return true;
		}

		case R.id.action_fav:
			if (subReddit.favorite) {
				subReddit.favorite = false;
				item.setIcon(android.R.drawable.btn_star_big_off);
			} else {
				subReddit.favorite = true;
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
		
		MenuItem itemSaveImage = menu.findItem(R.id.action_save_image);
		itemSaveImage.setVisible(false);
		
		MenuItem copyUrl = menu.findItem(R.id.action_copy_url);
		copyUrl.setVisible(false);
		
		return super.onCreateOptionsMenu(menu);
	}

	private void setFavoriteButton(MenuItem item) {
		if (subReddit.favorite) {
			item.setIcon(android.R.drawable.btn_star_big_on);
		} else {
			item.setIcon(android.R.drawable.btn_star_big_off);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void setOnItemClickListener(final Context context) {
		final ListView storiesListView = (ListView) findViewById(R.id.subreddit_channel_list);
		storiesListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				StoryInfo subReddit = (StoryInfo) storieList.get(position);
				Intent nextActivity = new Intent(context, ActivityStoryContent.class);
				nextActivity.putExtra("url", subReddit.url);
				nextActivity.putExtra("imageBitMap", headerBarThumb);
				nextActivity.putExtra("name", ActivitySubRedditChannel.this.subReddit.display_name);
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
				new LoadStories(this, subReddit.url).execute();
			}
		}
	}

	// Load Channel To Store on DB
	class LoadSubReddit extends AsyncTask<String, String, SubRedditInfo> {
		private String subRedditName;
		private Context context;
		private ProgressDialog pDialogChannel;

		public LoadSubReddit(Context context, String subRedditName) {
			this.subRedditName = subRedditName;
			this.context = context;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			pDialogChannel = new ProgressDialog(context);// CHANGE TO
															// GETAPPLICATION
			// CONTEXT
			pDialogChannel.setMessage("Saving Favorite SubReddit ...");
			pDialogChannel.setIndeterminate(false);
			pDialogChannel.setCancelable(false);
			pDialogChannel.show();
		}

		protected SubRedditInfo doInBackground(String... args) {
			SubRedditInfo sub = addNewSUbredditFromSearch(this.subRedditName);
			return sub;
		}

		protected void onPostExecute(final SubRedditInfo subRedditInfo) {
			pDialogChannel.dismiss();
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
				System.out.println("ITS GOING IN HERE NO BUENO");
				item.imageBitMap = new LoadThumbsTask(header_image_url).exceute().imageBitmap;// Picasso.with(context).load(header_image_url).get();
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
		ProgressDialog dialog;

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
			dialog = new ProgressDialog(context);
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					onBackPressed();
					// ****cleanup code****
				}
			});
			dialog.setMessage("Loading Stories...");
			dialog.show();

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
				listOfStories.add(item);
			}

			if (isFromSearch && length > 0) {
				subReddit.favorite = isFavoriteFromSearch(listOfStories.get(0).subreddit_id);
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

		private String URLCreate(String subReddit, int offset) {
			String after = "";
			if (storieList.size() > 0)
				after = storieList.get(storieList.size() - 1).name;

			return "http://www.reddit.com" + subReddit + ".json" + "?limit=" + offset + "&after=" + after;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(final List<StoryInfo> storiesInfoList) 
		{
			loadingMore = false;
			if (storiesInfoList == null)
				return;
			storieList.addAll(storieList.size(), storiesInfoList);
			
			dialog.dismiss();
			
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
