package com.gery.redditlurker;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.gery.database.Connection;
import com.gery.database.RedditRSSReader;

public class FrontPageFragment extends Fragment implements OnScrollListener 
{
	// List Items
		int currentFirstVisibleItem = 0;
		int currentVisibleItemCount = 0;
		int totalItemCount = 0;
		int currentScrollState = 0;
		Long offset = 20L;
		private boolean loadingMore;
		// List Items
		
		public List<StoryInfo> storieList;
		private Context context;
		private View rootView;
		
		@Override
		public void onCreate(Bundle bundle) {
			storieList = new ArrayList<StoryInfo>();
			super.onCreate(bundle);
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			context = inflater.getContext();

			if (Connection.isNetworkConnected(context)) {
				new LoadStories(context, "").execute();
			} else {
				Toast.makeText(context, "No Internet Connection Found", Toast.LENGTH_LONG).show();
			}
			
			rootView = inflater.inflate(R.layout.activity_subreddit, container, false);
			setOnItemClickListener(inflater.getContext());
			return rootView;
		}
		
		private void setOnItemClickListener(final Context context) {
			final ListView storiesListView = (ListView) rootView.findViewById(R.id.subreddit_channel_list);
			storiesListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> a, View v, int position, long id) {
					StoryInfo subReddit = (StoryInfo) storieList.get(position);
					Intent nextActivity = new Intent(context, ActivityStoryContent.class);
					nextActivity.putExtra("url", subReddit.url);
					nextActivity.putExtra("name", subReddit.subreddit);
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
					new LoadStories(context, "").execute();
				}
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
				//		onBackPressed();
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

			
				return listOfStories;
			}

			private String URLCreate(String subReddit, int offset) {
				String after = "";
				if (storieList.size() > 0)
					after = storieList.get(storieList.size() - 1).name;

				return "http://www.reddit.com/.json" + "?limit=" + offset + "&after=" + after;
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
				
				final ListView storiesListView = (ListView) rootView.findViewById(R.id.subreddit_channel_list);
				final int positionToSave = storiesListView.getFirstVisiblePosition();
				// updating UI from Background Thread
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						ChannelBaseAdapter var = new ChannelBaseAdapter(context, storieList);
						storiesListView.setAdapter(var);
						storiesListView.setSelection(positionToSave);
					}
				});
				super.onPostExecute(storiesInfoList);
			}
		}


}
