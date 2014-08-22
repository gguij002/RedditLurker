package com.gery.redditlurker;

import java.util.ArrayList;
import java.util.List;
import com.gery.database.*;

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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class EnteredSubRedditsFragment extends Fragment {
	static List<SubRedditInfo> subRedditsList;
	private ProgressDialog pDialog;
	View rootView; 
	private Context contex;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	contex = inflater.getContext();
    	subRedditsList = new ArrayList<SubRedditInfo>();
    	
    	Timer.StartTimer("EnteredSubRedditsFragment.OnCreate()-LoadSubRedditsFromDB");
    	new LoadSubRedditsFromDB(contex).execute();
    	Timer.EndTimer("EnteredSubRedditsFragment.OnCreate()-LoadSubRedditsFromDB");
    	
        rootView = inflater.inflate(R.layout.fragment_entered_subreddit, container, false);
        setOnItemClickListener(contex);
        return rootView;
    }
    
	private void setOnItemClickListener(final Context context) {
		final ListView storiesListView = (ListView) rootView.findViewById(R.id.entered_subreddit_list);
		storiesListView.setOnItemClickListener(new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				SubRedditInfo subReddit = (SubRedditInfo) subRedditsList.get(position);
				Intent nextActivity = new Intent(context, SubRedditChannelActivity.class);
				nextActivity.putExtra("subReddit", subReddit.url);
				startActivity(nextActivity);
			}
		});
	}
	
	@Override
    public void onPause()
    {
    	System.out.println("EneteredSubsFragment.onPause()" + AllSubRedditsFragment.addedItem);
    	super.onPause();
    }

    
    @Override
    public void onResume()
    {
    	System.out.println("EneteredSubsFragment.onResume()" + AllSubRedditsFragment.addedItem);
    	super.onResume();
    	if(AllSubRedditsFragment.addedItem)
    	{
    		AllSubRedditsFragment.addedItem = false;
    		new LoadSubRedditsFromDB(contex).execute();
    	}
    }
    
    class LoadSubRedditsFromDB extends AsyncTask<String, String, List<SubRedditInfo>>{

    	private Context context;
    	
		public LoadSubRedditsFromDB(Context context) {
			this.context = context;
		}

		@Override
		protected List<SubRedditInfo> doInBackground(String... params) {
			SubRedditsDataSource srDataSource;
			
			srDataSource = new SubRedditsDataSource(contex);
			srDataSource.open();
			
			Timer.StartTimer("doInBackground.srDataSource.getAllSubReddit()");
			List<SubRedditInfo> values = srDataSource.getAllSubReddit();
			Timer.EndTimer("doInBackground.srDataSource.getAllSubReddit()");
			return values;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(context);
			pDialog.setMessage("Loading SubReddits From DB...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
		
		protected void onPostExecute(final List<SubRedditInfo> listOfSubReddits) {
			Timer.StartTimer("onPostExecute");
			pDialog.dismiss();
			
			subRedditsList.clear();
			subRedditsList.addAll(listOfSubReddits);
			
			final ListView storiesListView = (ListView) rootView.findViewById(R.id.entered_subreddit_list);
			//final int positionToSave = storiesListView.getFirstVisiblePosition();
			// updating UI from Background Thread
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					EnteredSubredditCustomBaseAdapter var = new EnteredSubredditCustomBaseAdapter(context, subRedditsList);
					storiesListView.setAdapter(var);
					
					//storiesListView.setSelection(positionToSave);
				}
			});
			super.onPostExecute(listOfSubReddits);
			Timer.EndTimer("onPostExecute");
		}

    	
    }
}
