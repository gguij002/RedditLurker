package com.gery.redditlurker;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ActivityCommentsWebView extends Activity
{
	private WebView webView;
	 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comments_webview);
 
		webView = (WebView) findViewById(R.id.comments_webview_view);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient());
		webView.loadUrl("http://www.google.com");
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
