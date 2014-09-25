package com.gery.redditlurker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.webkit.WebView;

public class GifWebView extends WebView{

	@SuppressLint("SetJavaScriptEnabled")
	public GifWebView(Context context, String path) {
		super(context);
		
		getSettings().setJavaScriptEnabled(true);
		loadUrl(path);
	}

}
