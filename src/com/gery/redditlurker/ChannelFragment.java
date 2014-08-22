package com.gery.redditlurker;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.gery.database.SubRedditsDataSource;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ChannelFragment extends Fragment
{
	// List Items
	int currentFirstVisibleItem = 0;
	int currentVisibleItemCount = 0;
	int totalItemCount = 0;
	int currentScrollState = 0;
	boolean loadingMore = false;
	Long offset = 1L;
	// List Items
	
	View rootView; 
	SubRedditsDataSource srDataSource;
	Context contex;
	private ProgressDialog pDialog;
	
	List<StoryInfo> storieList;

	
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
	
		srDataSource = new SubRedditsDataSource(inflater.getContext());
		srDataSource.open();
		contex = inflater.getContext();
		new LoadStories(inflater.getContext()).execute();
	    rootView = inflater.inflate(R.layout.activity_subreddit, container, false);
	     
	    return rootView;
	 }
	 
	 /**
		 * Background Async Task to Load Stories by making HTTP Request
		 * */
		class LoadStories extends AsyncTask<String, String, List<StoryInfo>> {
			private Context fragmentContext;

			public LoadStories(Context context) {
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
				final String REDDIT_SUBREDDITS_URL = URLCreate("REPLACXE",offset.intValue());

				List<StoryInfo> listOfStories = new ArrayList<StoryInfo>();

				// Create List Of Stories
				JSONObject subRedditsJSON = new RedditRSSReader(
						REDDIT_SUBREDDITS_URL).execute();
				JSONObject data = (JSONObject) subRedditsJSON.get("data");
				JSONArray listOfSubredditsRaw = (JSONArray) data.get("children");

				int length = listOfSubredditsRaw.size();
				for (int i = 0; i < length; i++) {
//					JSONObject var = (JSONObject) ((JSONObject) listOfSubredditsRaw
//							.get(i)).get("data");
					//StoryInfo item = new StoryInfo();//(var).execute();
				//	String header_image_url = item.header_img;
//					if (header_image_url != null && !header_image_url.isEmpty()) {
//						item.imageBitMap = getImage(header_image_url);
//					}
					//listOfStories.add(item);
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
				final ListView storiesListView = (ListView) rootView
						.findViewById(R.id.subreddit_channel_list);
				final int positionToSave = storiesListView
						.getFirstVisiblePosition();
				// updating UI from Background Thread
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						ChannelBaseAdapter var = new ChannelBaseAdapter(fragmentContext, storieList);
						storiesListView.setAdapter(var);
						storiesListView.setSelection(positionToSave);
					}
				});
				super.onPostExecute(storiesInfoList);
			}
		}
}
