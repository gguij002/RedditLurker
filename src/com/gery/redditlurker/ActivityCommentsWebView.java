package com.gery.redditlurker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ActivityCommentsWebView extends Activity {
	private WebView webView;
	private String permalink;

	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setCancelable(true);
		dialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				onBackPressed();
				// ****cleanup code****
			}
		});
		dialog.setMessage("Loading Comments...");
		dialog.show();

		setContentView(R.layout.activity_comments_webview);
		this.handleIntent(getIntent());

		webView = (WebView) findViewById(R.id.comments_webview_view);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				dialog.dismiss();
			}
		});
		webView.loadUrl(createURL(this.permalink));
	}
	
	private String createURL(String permalink){
		return "http://www.reddit.com" + permalink + "/.compact";
	}

	private void handleIntent(Intent intent) {
		permalink = intent.getStringExtra("permalink");
		String displayName = intent.getStringExtra("name");
		setHeaderBarThumb(intent.getByteArrayExtra("imageBitMap"));
		setTitle("LurkR: " + displayName);
	}

	private void setHeaderBarThumb(byte[] thumbBitmapArr) {
		if (thumbBitmapArr != null) {
			Bitmap thumbBitmap = BitmapFactory.decodeByteArray(thumbBitmapArr, 0, thumbBitmapArr.length);
			Resources res = getResources();
			BitmapDrawable icon = null;

			icon = new BitmapDrawable(res, thumbBitmap);
			getActionBar().setIcon(icon);
		} else {
			getActionBar().setIcon(R.drawable.ic_launcher);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_actions, menu);

		MenuItem itemSearch = menu.findItem(R.id.action_search_widget);
		itemSearch.setVisible(false);
		
		MenuItem itemFav = menu.findItem(R.id.action_fav);
		itemFav.setVisible(false);
		
		MenuItem copyUrl = menu.findItem(R.id.action_copy_url);
		copyUrl.setVisible(false);
		
		MenuItem itemSaveImage = menu.findItem(R.id.action_save_image);
		itemSaveImage.setVisible(false);
		
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			super.onBackPressed();
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
