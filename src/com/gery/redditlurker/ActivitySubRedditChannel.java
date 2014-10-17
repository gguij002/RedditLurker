package com.gery.redditlurker;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ProgressBar;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.gery.database.RedditRSSReader;
import com.gery.database.SubRedditsDataSource;
import com.gery.database.Utils;

public class ActivitySubRedditChannel extends Activity implements OnScrollListener, OnMenuItemClickListener {
	// List Items
	int currentFirstVisibleItem = 0;
	int currentVisibleItemCount = 0;
	int totalItemCount = 0;
	int currentScrollState = 0;
	Long offset = 20L;
	// List Items

	public List<StoryInfo> storieList;
	private boolean loadingMore;

	SubRedditInfo subReddit = null;
	private byte[] headerBarThumb;
	private ListView storiesListView;
	private View footer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Utils.setPrefTheme(this);
		super.onCreate(savedInstanceState);
		storieList = new ArrayList<StoryInfo>();
		setContentView(R.layout.activity_subreddit);
		storiesListView = (ListView) findViewById(R.id.subreddit_channel_list);
		footer = LayoutInflater.from(this).inflate(R.layout.footer_loader, null);

		handleIntent(getIntent());

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

		this.ReCreateSubReddit(intent);
		setHeaderBarThumb(subReddit.imageBitMap);

		setTitle(subReddit.display_name);

		new LoadStories(this, subReddit.url).execute();
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
				if (!srDataSource.isRawSubRedditExist(subReddit.name)) {
					new LoadSubReddit(this, this.storieList.get(0).subreddit).execute();
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
			
		case R.id.action_theme_question:
		{
			final View anchor = findViewById(R.id.action_fav);
			PopupMenu popupMenu = new PopupMenu(this, anchor);
			popupMenu.setOnMenuItemClickListener(this);
			popupMenu.getMenuInflater().inflate(R.menu.themes_menu, popupMenu.getMenu());
			popupMenu.show();
			return true;
		}
		case R.id.action_sort_menu:
		{
//			//Context wrapper = new ContextThemeWrapper(this, R.style.AppBaseTheme);
//			final View sortView = findViewById(item.getItemId());
//			PopupMenu popupMenu = new PopupMenu(this, sortView);
//			popupMenu.setOnMenuItemClickListener(this);
//			popupMenu.inflate(R.menu.sort_menu);
//			popupMenu.show();
//			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public boolean onMenuItemClick(final MenuItem item) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		        	 switch (item.getItemId())
		             {
		     	        case R.id.action_theme_dark:
		     	        	Utils.savePrefTheme(ActivitySubRedditChannel.this, Utils.THEME_DARK);
		     	        break;
		     	        case R.id.action_theme_light:
		     	        	Utils.savePrefTheme(ActivitySubRedditChannel.this, Utils.THEME_LIGHT);
		     	        break;
		     	        case R.id.action_theme_mixed:
		     	        	Utils.savePrefTheme(ActivitySubRedditChannel.this, Utils.THEME_BASE);
		     	        break;
		             }
		        	dialog.dismiss();
		         	Utils.restartSelf(ActivitySubRedditChannel.this);
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		        	 dialog.dismiss();
		            break;
		        }
		    }
		};
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Restart Alert");
		builder.setMessage("Need to restart application to change theme\nContinue?").setPositiveButton("Yes Restart", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
		
		return false;
//
//		switch (item.getItemId()) {
//
//		case R.id.item_hot:
//			Toast.makeText(this, "Comedy Clicked", Toast.LENGTH_SHORT).show();
//			return true;
//		case R.id.item_new:
//			Toast.makeText(this, "Movies Clicked", Toast.LENGTH_SHORT).show();
//			return true;
//		case R.id.item_top:
//			Toast.makeText(this, "Music Clicked", Toast.LENGTH_SHORT).show();
//			return true;
//		default:
//			return false;
//		}
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_actions, menu);
		MenuItem itemFav = menu.findItem(R.id.action_fav);
		itemFav.setVisible(true);
		
		MenuItem changeTheme = menu.findItem(R.id.action_theme_question);
		changeTheme.setVisible(true);

		setFavoriteButton(itemFav);
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
				if (isYoutubeVid(subReddit.url)) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(subReddit.url)));
				} else {
					Intent nextActivity = new Intent(context, ActivityStoryContent.class);
					nextActivity.putExtra("url", subReddit.url);
					nextActivity.putExtra("imageBitMap", headerBarThumb);
					nextActivity.putExtra("permalink", subReddit.permalink);
					nextActivity.putExtra("name", ActivitySubRedditChannel.this.subReddit.display_name);
					nextActivity.putExtra("subRedditId", subReddit.subreddit_id);
					startActivity(nextActivity);
				}
			}
		});
		storiesListView.setOnScrollListener(this);
	}

	private boolean isYoutubeVid(String url) {
		return url.contains("youtube.com") || url.contains("youtu.be");
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
			pDialogChannel = new ProgressDialog(context);
			pDialogChannel.setMessage("Saving Favorite SubReddit ...");
			pDialogChannel.setIndeterminate(false);
			pDialogChannel.setCancelable(false);
			pDialogChannel.show();
		}

		protected SubRedditInfo doInBackground(String... args) {
			// "http://reddit.com/r/reddits.rss?limit=[limit]&after=[after]";
			final String REDDIT_SUBREDDITS_URL = URLCreate();
			System.out.println("REDDIT_SUBREDDITS_URL: " + REDDIT_SUBREDDITS_URL);

			// Create List Of Subreddit
			JSONObject subRedditsJSON = new RedditRSSReader(REDDIT_SUBREDDITS_URL).execute();
			JSONObject data = (JSONObject) subRedditsJSON.get("data");

			SubRedditInfo item = new SubRedditInfo(data).execute();
			item.favorite = true;
			subReddit = item;
			return item;
		}

		private String URLCreate() {
			return "http://www.reddit.com/r/" + subRedditName + "/about.json";
		}

		protected void onPostExecute(final SubRedditInfo subRedditInfo) {
			pDialogChannel.dismiss();
			super.onPostExecute(subRedditInfo);
		}
	}

	/**
	 * Background Async Task to Load Stories by making HTTP Request
	 * */
	class LoadStories extends AsyncTask<String, String, List<StoryInfo>> {
		private String subRedditChannel;
		private ProgressBar progressBar;

		public LoadStories(Context context, String subReddit) {
			subRedditChannel = subReddit;
			loadingMore = true;
			progressBar = (ProgressBar) findViewById(R.id.progressBar_load_subs);
		}

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!storieList.isEmpty())
				storiesListView.addFooterView(footer, null, false);
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

			return listOfStories;
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
			loadingMore = false;

			if (!storieList.isEmpty()) {
				storiesListView.removeFooterView(footer);
			}
			progressBar.setVisibility(View.GONE);

			if (storiesInfoList == null)
				return;

			storieList.addAll(storieList.size(), storiesInfoList);

			storiesListView = (ListView) findViewById(R.id.subreddit_channel_list);

			final int index = storiesListView.getFirstVisiblePosition();
			View v = storiesListView.getChildAt(0);
			final int top = (v == null) ? 0 : v.getTop();

			runOnUiThread(new Runnable() {
				public void run() {
					ChannelBaseAdapter var = new ChannelBaseAdapter(ActivitySubRedditChannel.this, storieList);
					storiesListView.setAdapter(var);
					storiesListView.setSelectionFromTop(index, top);
				}
			});
			super.onPostExecute(storiesInfoList);
		}
	}

}
