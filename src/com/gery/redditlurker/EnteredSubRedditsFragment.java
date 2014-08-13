package com.gery.redditlurker;

import java.util.ArrayList;
import java.util.List;

import com.gery.database.SubRedditsDataSource;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class EnteredSubRedditsFragment extends Fragment {
	List<SubRedditInfo> subRedditsList;
	private ProgressDialog pDialog;
	View rootView; 
	SubRedditsDataSource srDataSource;
	Context contex;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	subRedditsList = new ArrayList<SubRedditInfo>();
    	srDataSource = new SubRedditsDataSource(inflater.getContext());
		srDataSource.open();
    	contex = inflater.getContext();
    	new LoadSubRedditsFromDB(inflater.getContext()).execute();
        rootView = inflater.inflate(R.layout.fragment_entered_subreddit, container, false);
         
        return rootView;
    }
    
    @Override
    public void onResume()
    {
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
			List<SubRedditInfo> values = srDataSource.getAllSubReddit();
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
		}

    	
    }
}
