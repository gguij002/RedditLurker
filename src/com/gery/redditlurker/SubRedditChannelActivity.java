package com.gery.redditlurker;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

public class SubRedditChannelActivity extends Activity  
{
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_subreddit);
	 
	        // get the action bar
	        ActionBar actionBar = getActionBar();
	 
	        // Enabling Back navigation on Action Bar icon
	        actionBar.setDisplayHomeAsUpEnabled(true);
	 
	        List<StoryInfo> values = new ArrayList<StoryInfo>();
	        values.add(new StoryInfo());
	        
	        final ListView subRedditChannelListView = (ListView) findViewById(R.id.subreddit_channel_list);
	        subRedditChannelListView.setAdapter(new ChannelBaseAdapter(this, values));
			
	        
	        handleIntent(getIntent());
	    }
	 
	    @Override
	    protected void onNewIntent(Intent intent) {
	        setIntent(intent);
	        handleIntent(intent);
	    }
	 
	    /**
	     * Handling intent data
	     */
	    private void handleIntent(Intent intent) {
	        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	            String query = intent.getStringExtra(SearchManager.QUERY);
	            
	            /**
	             * Use this query to display search results like 
	             * 1. Getting the data from SQLite and showing in listview 
	             * 2. Making webrequest and displaying the data 
	             * For now we just display the query only
	             */
	        }
	        else //comes from the lists and not the search bar
	        {

	        }
	    }
}
