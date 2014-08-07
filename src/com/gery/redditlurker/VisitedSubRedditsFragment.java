package com.gery.redditlurker;

import java.util.ArrayList;
import java.util.List;

import com.gery.redditlurker.AllSubRedditsFragment.LoadSubReddits;
import com.gery.userpreferences.UserPrefs;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class VisitedSubRedditsFragment extends Fragment {
	List<SubRedditInfo> subRedditsList;
	private ProgressDialog pDialog;
	View rootView; 
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	subRedditsList = new ArrayList<SubRedditInfo>();
    	UserPrefs prefs = new UserPrefs( inflater.getContext());
    	
    	//new LoadSubRedditsFromDB(inflater.getContext()).execute();
        View rootView = inflater.inflate(R.layout.fragment_entered_subreddit, container, false);
         
        return rootView;
    }
}
