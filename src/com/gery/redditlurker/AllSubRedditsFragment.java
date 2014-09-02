package com.gery.redditlurker;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.gery.database.ImageReader;
import com.gery.database.SubRedditsDataSource;

import android.app.ProgressDialog;
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
import android.widget.AdapterView.OnItemClickListener;

public class AllSubRedditsFragment extends Fragment implements OnScrollListener {
	// List Items
	int currentFirstVisibleItem = 0;
	int currentVisibleItemCount = 0;
	int totalItemCount = 0;
	int currentScrollState = 0;
	boolean loadingMore = false;
	Long offset = 4L;
	// List Items

	public AllSubRedditCustomBaseAdapter adapter;
	public List<SubRedditInfo> subRedditsList;
	private ProgressDialog pDialog;
	private View rootView;
	Context context;

	@Override
	public void onCreate(Bundle bundle)
	{
		subRedditsList = new ArrayList<SubRedditInfo>();
		super.onCreate(bundle);
	}
	
	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		context = inflater.getContext();
		new LoadSubReddits(context).execute();
		rootView = inflater.inflate(R.layout.fragment_all_subreddit, container, false);
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
	}

	private void setOnItemClickListener(final Context context) {
		final ListView storiesListView = (ListView) rootView.findViewById(R.id.all_subreddit_list);
		storiesListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				SubRedditInfo subReddit = (SubRedditInfo) subRedditsList.get(position);
				Intent nextActivity = new Intent(context, SubRedditChannelActivity.class);
				nextActivity.putExtra("subReddit", subReddit.url);
				nextActivity.putExtra("favorite", subReddit.favorite);
				nextActivity.putExtra("subName", subReddit.name);
				startActivity(nextActivity);
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
		if (SubRedditsDataSource.AddedItem()) {
			SubRedditsDataSource srDataSource = new SubRedditsDataSource(context);
			srDataSource.open();
			UpdateFavs(srDataSource.getAllSubRedditsID());
			srDataSource.close();
			// SubRedditsDataSource.AddedItemFalse();
		}
	}

	/**
	 * Background Async Task to Load subreddits by making HTTP Request
	 * */
	class LoadSubReddits extends AsyncTask<String, String, List<SubRedditInfo>> {
		private Context fragmentContext;

		public LoadSubReddits(Context context) {
			this.fragmentContext = context;
			loadingMore = true;
		}

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(fragmentContext);
			pDialog.setMessage("Loading SubReddits ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
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
				String header_image_url = item.header_img;

				if (header_image_url != null && !header_image_url.isEmpty()) {
					item.imageBitMap = new ImageReader(header_image_url).exceute().imageBitmap;
				}

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
				after = "&after=" + subRedditsList.get(subRedditsList.size() - 1).name;// Get
																						// subreddits
																						// after
																						// This
																						// "Name"
			return "http://www.reddit.com/reddits/.json" + "?limit=" + offset + after;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(final List<SubRedditInfo> listOfSubReddits) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			loadingMore = false;
			subRedditsList.addAll(subRedditsList.size(), listOfSubReddits);
			final ListView storiesListView = (ListView) rootView.findViewById(R.id.all_subreddit_list);
			final int positionToSave = storiesListView.getFirstVisiblePosition();
			// updating UI from Background Thread
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					adapter = new AllSubRedditCustomBaseAdapter(fragmentContext, R.layout.fragment_all_subreddit, subRedditsList);
					storiesListView.setAdapter(adapter);
					storiesListView.setSelection(positionToSave);
				}
			});
			super.onPostExecute(listOfSubReddits);
		}
	}

	public void UpdateFavs(List<String> storiesDB) {
		List<SubRedditInfo> newTempList = new ArrayList<SubRedditInfo>();
		
		if(subRedditsList != null && this.subRedditsList.size() != storiesDB.size()){
			for (SubRedditInfo subReddits : this.subRedditsList) {
				System.out.println("BEFORE IF: " + subReddits.display_name + "-" + subReddits.name);
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
