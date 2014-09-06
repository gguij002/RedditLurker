package com.gery.redditlurker;

import java.util.concurrent.ExecutionException;

import com.gery.database.LoadIMagesTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

public class ActivityStoryContent extends Activity {
	private WebView webView;
	private String url;
	private Bitmap imageBitmap = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.handleIntent(getIntent());

		if (isImage()) {
			setContentView(R.layout.activity_image_display);
			AsyncTask<String, Void, Bitmap> loadImage = new LoadIMagesTask(this).execute(url);
			try {
				imageBitmap = loadImage.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			ImageView imageView = (ImageView) findViewById(R.id.image_viewer);
			imageView.setImageBitmap(imageBitmap);
		} else {
			setContentView(R.layout.activity_story_content);
			webView = (WebView) findViewById(R.id.story_content_webview_view);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.setWebViewClient(new WebViewClient());
			webView.loadUrl(url);
		}
	}

	@Override
	public void onDestroy() {
		if (imageBitmap != null)
			imageBitmap.recycle();
		super.onDestroy();
	}

	private void handleIntent(Intent intent) {
		url = intent.getStringExtra("url");
	}

	private boolean isImage() {
		return (url.endsWith(".jpg") || url.endsWith(".gif") || url.endsWith("png"));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
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
