package com.gery.redditlurker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ActivityStoryContent extends Activity
{
	private WebView webView;
	private String url;
	 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_story_content);
		
		this.handleIntent(getIntent());
 
		webView = (WebView) findViewById(R.id.story_content_webview_view);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient());
		webView.loadUrl(url);
	}
	
	private void handleIntent(Intent intent)
	{
		url = intent.getStringExtra("url");
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
		switch (item.getItemId()) {
		case android.R.id.home:{
             super.onBackPressed();
             return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
