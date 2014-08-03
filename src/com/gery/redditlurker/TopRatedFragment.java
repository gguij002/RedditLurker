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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class TopRatedFragment extends Fragment implements OnScrollListener
{
	//List Items 
	int currentFirstVisibleItem = 0;
	int currentVisibleItemCount = 0;
	int totalItemCount = 0;
	int currentScrollState = 0;
	boolean loadingMore = false;
	Long startIndex = 0L;
	Long offset = 25L;
	View footerView;
	//List Items
	
	List<SubReddit> subRedditsList;
    private ProgressDialog pDialog;
    View rootView;
	
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
        subRedditsList = new ArrayList<SubReddit>();
        new LoadInbox(inflater.getContext()).execute();
        rootView = inflater.inflate(R.layout.fragment_top_rated, container, false);
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
        if (this.currentVisibleItemCount > 0 && this.currentScrollState == SCROLL_STATE_IDLE && this.totalItemCount == (currentFirstVisibleItem + currentVisibleItemCount)) {
            /*** In this way I detect if there's been a scroll which has completed ***/
            /*** do the work for load more date! ***/
            if (!loadingMore) {
                loadingMore = true;
                new LoadInbox(context).execute();
            }
        }
    }

    
    private void setOnItemClickListener(final Context context)
    {
    	final ListView storiesListView = (ListView) rootView.findViewById(R.id.all_subreddit_list);
    	storiesListView.setOnItemClickListener(new OnItemClickListener() {
 	         @Override
 	         public void onItemClick(AdapterView<?> a, View v, int position, long id) { 
 	        	  SubReddit subReddit = (SubReddit) subRedditsList.get(position);
 	        	  Intent nextActivity = new Intent(context, SubRedditItemActivity.class);
             	  nextActivity.putExtra("subReddit", subReddit.link);
             	  startActivity(nextActivity); 
 		     }  
 	    });
    	storiesListView.setOnScrollListener(this);
    }
    
	/**
	 * Background Async Task to Load all INBOX messages by making HTTP Request
	 * */
	class LoadInbox extends AsyncTask<String, String, List<SubReddit>> 
	{
		private Context fragmentContext;
		
		public LoadInbox(Context context)
		{
			this.fragmentContext = context;
			loadingMore = true;
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
        protected List<SubReddit> doInBackground(String... args) 
        {
        	//"http://reddit.com/r/reddits.rss?limit=[limit]&after=[after]";
        	final String REDDIT_SUBREDDITS_URL = URLCreate(startIndex.intValue(), offset.intValue());

        	List<SubReddit> listOfSubReddits = new ArrayList<SubReddit>();
             
            //Create List Of Subreddits
            Document subRedditsDocument = new RedditRSSReader(REDDIT_SUBREDDITS_URL).execute();
            NodeList listOfSubredditsRaw = subRedditsDocument.getElementsByTagName("item");
            int length = listOfSubredditsRaw.getLength();
            for (int i = 0; i < length; i++) {
            	SubReddit item = new SubReddit((Element)listOfSubredditsRaw.item(i)).execute();	
            	listOfSubReddits.add(item);
   	   		}
            subRedditsList.addAll(startIndex.intValue(),listOfSubReddits);
            return subRedditsList;
        }
        
        private String URLCreate(int startIndex, int offset)
        {
        	return "http://www.reddit.com/reddits/.rss" +  "?limit=" + offset + "&after="+ startIndex;
        }
        
        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(final List<SubReddit> listOfSubReddits) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            loadingMore = false;
            if (listOfSubReddits.size() > 0) {
                startIndex = startIndex + listOfSubReddits.size();
            }
           
            
            final ListView storiesListView = (ListView) rootView.findViewById(R.id.all_subreddit_list);
	        // updating UI from Background Thread
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                	SubRedditCustomBaseAdapter var = new SubRedditCustomBaseAdapter(fragmentContext, listOfSubReddits);
                	storiesListView.setAdapter(var);
            	}
            });
            super.onPostExecute(listOfSubReddits);
        }
	}
}

