package com.gery.redditlurker;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.gery.database.Connection;
import com.gery.database.SubRedditsDataSource;
import com.gery.database.Timer;

public class EnteredSubRedditsFragment extends Fragment {
	public List<SubRedditInfo> subRedditsList;
	View rootView;
	private Context ActivityContext;
	EnteredSubredditCustomBaseAdapter adapter;

	@Override
	public void onCreate(Bundle bundle) {
		subRedditsList = new ArrayList<SubRedditInfo>();
		super.onCreate(bundle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ActivityContext = inflater.getContext();
		Timer.StartTimer("EnteredSubRedditsFragment.OnCreate()-LoadSubRedditsFromDB");
		new LoadSubRedditsFromDB(ActivityContext).execute();
		Timer.EndTimer("EnteredSubRedditsFragment.OnCreate()-LoadSubRedditsFromDB");

		rootView = inflater.inflate(R.layout.fragment_entered_subreddit, container, false);
		setOnItemClickListener(ActivityContext);
		return rootView;
	}

	private void setOnItemClickListener(final Context context) {
		final ListView storiesListView = (ListView) rootView.findViewById(R.id.entered_subreddit_list);
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
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		SubRedditsDataSource srDataSource = new SubRedditsDataSource(ActivityContext);
		srDataSource.open();
		UpdateSubRedditList(srDataSource.getAllSubReddit());
		srDataSource.close();
	}

	class LoadSubRedditsFromDB extends AsyncTask<String, String, List<SubRedditInfo>> {

		private Context context;

		public LoadSubRedditsFromDB(Context context) {
			this.context = context;
		}

		@Override
		protected List<SubRedditInfo> doInBackground(String... params) {
			SubRedditsDataSource srDataSource;

			srDataSource = new SubRedditsDataSource(ActivityContext);
			srDataSource.open();

			Timer.StartTimer("doInBackground.srDataSource.getAllSubReddit()");
			List<SubRedditInfo> values = srDataSource.getAllSubReddit();
			Timer.EndTimer("doInBackground.srDataSource.getAllSubReddit()");
			srDataSource.close();
			return values;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected void onPostExecute(final List<SubRedditInfo> listOfSubReddits) {
			Timer.StartTimer("onPostExecute");

			subRedditsList.clear();
			subRedditsList.addAll(listOfSubReddits);

			final ListView storiesListView = (ListView) rootView.findViewById(R.id.entered_subreddit_list);
			// final int positionToSave =
			// storiesListView.getFirstVisiblePosition();
			// updating UI from Background Thread
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					adapter = new EnteredSubredditCustomBaseAdapter(context, subRedditsList);
					storiesListView.setAdapter(adapter);

					// storiesListView.setSelection(positionToSave);
				}
			});
			super.onPostExecute(listOfSubReddits);
			Timer.EndTimer("onPostExecute");
		}
	}

	public void UpdateSubRedditList(List<SubRedditInfo> list) {
		if (this.adapter != null && this.subRedditsList != null && this.subRedditsList.size() != list.size()) {
			this.subRedditsList.clear();
			this.subRedditsList.addAll(list);
			adapter.notifyDataSetChanged();
		}
	}
}
