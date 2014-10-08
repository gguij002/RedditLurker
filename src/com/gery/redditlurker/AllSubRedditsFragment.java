package com.gery.redditlurker;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.gery.database.Connection;
import com.gery.database.RedditRSSReader;
import com.gery.database.SubRedditsDataSource;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class AllSubRedditsFragment extends Fragment implements OnScrollListener {
	// List Items
	int currentFirstVisibleItem = 0;
	int currentVisibleItemCount = 0;
	int totalItemCount = 0;
	int currentScrollState = 0;
	boolean loadingMore = false;
	Long offset = 20L;
	// List Items

	public AllSubRedditCustomBaseAdapter adapter;
	public List<SubRedditInfo> subRedditsList;
	private View rootView;
	Context context;
	private View footer;
	private ListView storiesListView;

	@Override
	public void onCreate(Bundle bundle) {
		subRedditsList = new ArrayList<SubRedditInfo>();
		super.onCreate(bundle);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		context = inflater.getContext();
		rootView = inflater.inflate(R.layout.fragment_all_subreddit, container, false);
		storiesListView = (ListView) rootView.findViewById(R.id.all_subreddit_list);

		footer = LayoutInflater.from(context).inflate(R.layout.footer_loader, null);
		storiesListView.addFooterView(footer, null, false);

		if (Connection.isNetworkConnected(context)) {
			new LoadSubReddits(context).execute();
		} else {
			Toast.makeText(context, "No Internet Connection Found", Toast.LENGTH_LONG).show();
		}

		setOnItemClickListener(inflater.getContext());
		return rootView;
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
			/*** do the work for load more date! ***/
			if (!loadingMore) {
				loadingMore = true;
				new LoadSubReddits(context).execute();
			}
		}
		// storiesListView.removeFooterView(footer);
	}

	private void setOnItemClickListener(final Context context) {
		final ListView storiesListView = (ListView) rootView.findViewById(R.id.all_subreddit_list);
		storiesListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				if (Connection.isNetworkConnected(context)) {
					SubRedditInfo subReddit = (SubRedditInfo) subRedditsList.get(position);
					Intent nextActivity = new Intent(context, ActivitySubRedditChannel.class);
					nextActivity.putExtra("subRedditJSON", subReddit.getJsonObjectAsString());
					nextActivity.putExtra("favorite", subReddit.favorite);

					byte[] byteArray = null;
					if (subReddit.imageBitMap != null) {
						ByteArrayOutputStream bStream = new ByteArrayOutputStream();
						subReddit.imageBitMap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
						byteArray = bStream.toByteArray();
					}
					nextActivity.putExtra("imageBitMap", byteArray);

					startActivity(nextActivity);
				} else
					Toast.makeText(context, "No Internet Connection Found", Toast.LENGTH_LONG).show();
			}
		});
		storiesListView.setOnScrollListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		SubRedditsDataSource srDataSource = new SubRedditsDataSource(context);
		srDataSource.open();
		UpdateFavs(srDataSource.getAllSubRedditsID());
		srDataSource.close();
	}

	/**
	 * Background Async Task to Load subreddits by making HTTP Request
	 * */
	class LoadSubReddits extends AsyncTask<String, String, List<SubRedditInfo>> {
		private Context fragmentContext;
		private ProgressBar progressBar;

		public LoadSubReddits(Context context) {
			this.fragmentContext = context;
			loadingMore = true;
			progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar_load_subs_all);
		}

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			if (!subRedditsList.isEmpty())
				storiesListView.addFooterView(footer, null, false);
		}

		/**
		 * getting SubReddits
		 * */
		protected List<SubRedditInfo> doInBackground(String... args) {
			// "http://reddit.com/r/reddits.rss?limit=[limit]&after=[after]";
			final String REDDIT_SUBREDDITS_URL = URLCreate(offset.intValue());
			System.out.println("REDDIT_SUBREDDITS_URL: " + REDDIT_SUBREDDITS_URL);
			List<SubRedditInfo> listOfSubReddits = new ArrayList<SubRedditInfo>();

			// Create List Of Subreddits
			JSONObject subRedditsJSON = new RedditRSSReader(REDDIT_SUBREDDITS_URL).execute();
			JSONObject data = (JSONObject) subRedditsJSON.get("data");
			JSONArray listOfSubredditsRaw = (JSONArray) data.get("children");

			SubRedditsDataSource dataSource = new SubRedditsDataSource(fragmentContext);
			dataSource.open();
			List<String> subRedditsIdsFromDb = dataSource.getAllSubRedditsID();
			dataSource.close();

			int length = listOfSubredditsRaw.size();
			for (int i = 0; i < length; i++) {
				JSONObject var = (JSONObject) ((JSONObject) listOfSubredditsRaw.get(i)).get("data");
				SubRedditInfo item = new SubRedditInfo(var).execute();

				if (subRedditsIdsFromDb.contains(item.name)) {
					System.out.println("subRedditsIdsFromDb.contains(item.id)" + item.display_name);
					item.favorite = true;
				}
				listOfSubReddits.add(item);
			}
			return listOfSubReddits;
		}

		private String URLCreate(int offset) {
			String after = "";
			if (subRedditsList.size() > 0)
				after = "&after=" + subRedditsList.get(subRedditsList.size() - 1).name;

			return "http://www.reddit.com/reddits/.json" + "?limit=" + offset + after;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(final List<SubRedditInfo> listOfSubReddits) {
			loadingMore = false;

			if (!subRedditsList.isEmpty()) {
				storiesListView.removeFooterView(footer);
			}
			progressBar.setVisibility(View.GONE);

			if (listOfSubReddits == null)
				return;
			subRedditsList.addAll(subRedditsList.size(), listOfSubReddits);

			final int index = storiesListView.getFirstVisiblePosition();
			View v = storiesListView.getChildAt(0);
			final int top = (v == null) ? 0 : v.getTop();

			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					adapter = new AllSubRedditCustomBaseAdapter(fragmentContext, R.layout.fragment_all_subreddit, subRedditsList);
					storiesListView.setAdapter(adapter);
					storiesListView.setSelectionFromTop(index, top);
				}
			});
			super.onPostExecute(subRedditsList);
		}
	}

	public void UpdateFavs(List<String> storiesDB) {
		List<SubRedditInfo> newTempList = new ArrayList<SubRedditInfo>();

		if (this.adapter != null && subRedditsList != null && this.subRedditsList.size() != storiesDB.size()) {
			for (SubRedditInfo subReddits : this.subRedditsList) {
				if (!storiesDB.contains(subReddits.name)) {
					subReddits.favorite = false;
				} else {
					subReddits.favorite = true;
				}
				newTempList.add(subReddits);
			}
			this.adapter.clear();
			this.adapter.addAll(newTempList);
			this.adapter.notifyDataSetChanged();
		}
	}
}
