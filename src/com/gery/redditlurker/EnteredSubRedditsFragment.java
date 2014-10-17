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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.gery.database.Utils;
import com.gery.database.SubRedditsDataSource;

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
		new LoadSubRedditsFromDB(ActivityContext).execute();

		rootView = inflater.inflate(R.layout.fragment_entered_subreddit, container, false);
		setOnItemClickListener(ActivityContext);
		return rootView;
	}

	private void setOnItemClickListener(final Context context) {
		final ListView storiesListView = (ListView) rootView.findViewById(R.id.entered_subreddit_list);
		storiesListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				if (Utils.isNetworkConnected(context)) {
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

			List<SubRedditInfo> values = srDataSource.getAllSubReddit();

			srDataSource.close();
			return values;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected void onPostExecute(final List<SubRedditInfo> listOfSubReddits) {
			subRedditsList.clear();
			subRedditsList.addAll(listOfSubReddits);

			final ListView storiesListView = (ListView) rootView.findViewById(R.id.entered_subreddit_list);
			TextView emptyText = (TextView) rootView.findViewById(android.R.id.empty);
			storiesListView.setEmptyView(emptyText);
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
