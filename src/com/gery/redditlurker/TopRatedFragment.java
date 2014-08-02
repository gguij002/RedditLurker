package com.gery.redditlurker;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony.Sms.Conversations;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class TopRatedFragment extends Fragment {
	 
	List<SubReddit> subRedditsList;
    private ProgressDialog pDialog;
    View rootView;
	
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
        subRedditsList = new ArrayList<SubReddit>();
        new LoadInbox(inflater.getContext()).execute();
    	
	    rootView = inflater.inflate(R.layout.fragment_top_rated, container, false);
	    return rootView;
    }
    
	/**
	 * Background Async Task to Load all INBOX messages by making HTTP Request
	 * */
	class LoadInbox extends AsyncTask<String, String, String> 
	{
		private Context fragmentContext;
		
		public LoadInbox(Context context)
		{
			this.fragmentContext = context;
		}
		
	     /**
	     * Before starting background thread Show Progress Dialog
	     * */
	    @Override
	    protected void onPreExecute() 
	    {
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
        protected String doInBackground(String... args) 
        {
        	final String REDDIT_SUBREDDITS_URL = "http://www.reddit.com/reddits/.rss";
            List<SubReddit> listOfSubReddits = new ArrayList<SubReddit>();
             
            //Create List Of Subreddits
            Document subRedditsDocument = new RedditRSSReader(REDDIT_SUBREDDITS_URL).execute();
            NodeList listOfSubredditsRaw = subRedditsDocument.getElementsByTagName("item");
            int length = listOfSubredditsRaw.getLength();
            for (int i = 0; i < length; i++) {
            	SubReddit item = new SubReddit((Element)listOfSubredditsRaw.item(i)).execute();	
            	listOfSubReddits.add(item);
   	   		}
            subRedditsList = listOfSubReddits;
            return null;
        }
 
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            final ListView storiesListView = (ListView) rootView.findViewById(R.id.all_subreddit_list);
            storiesListView.setOnItemClickListener(new OnItemClickListener() {
      	         @Override
      	         public void onItemClick(AdapterView<?> a, View v, int position, long id) { 
      	        	  SubReddit subReddit = (SubReddit) subRedditsList.get(position);
      	        	  Intent nextActivity = new Intent(fragmentContext, SubRedditItemActivity.class);
                  	  nextActivity.putExtra("subReddit", subReddit.link);
                  	  startActivity(nextActivity); 
      		     }  
      	    });
            
	        // updating UI from Background Thread
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                	SubRedditCustomBaseAdapter var = new SubRedditCustomBaseAdapter(fragmentContext, subRedditsList);
                	storiesListView.setAdapter(var);
            	}
            });
        }
	}
}

