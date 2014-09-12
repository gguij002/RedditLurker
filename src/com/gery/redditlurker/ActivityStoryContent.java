package com.gery.redditlurker;

import java.util.concurrent.ExecutionException;

import com.gery.database.LoadIMagesTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ActivityStoryContent extends Activity {
	private WebView webView;
	private String url;
	private Bitmap imageBitmap = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.handleIntent(getIntent());

		if (isImage()) {
			setContentView(R.layout.activity_image_display);
			
			ImageView imageView = (ImageView) findViewById(R.id.image_viewer);
			final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

			//This line shows progressBar again for recycled view
			progressBar.setVisibility(View.VISIBLE);
			
			Picasso.with(this).load(url).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError() {
                	//Set Error Image
                }
            });
			
			
			//Picasso.with(this).load(url).placeholder(R.id.progressBar).into(imageView);
			

			// AsyncTask<String, Void, Bitmap> loadImage = new
			// LoadIMagesTask(this).execute(url);
			// try {
			// imageBitmap = loadImage.get();
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// } catch (ExecutionException e) {
			// e.printStackTrace();
			// }
			// ImageView imageView = (ImageView)
			// findViewById(R.id.image_viewer);
			// imageView.setImageBitmap(imageBitmap);
		} else {
			setContentView(R.layout.activity_story_content);
			webView = (WebView) findViewById(R.id.story_content_webview_view);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.setWebViewClient(new WebViewClient());
			webView.loadUrl(formatURL(url));
		}
	}

	private String formatURL(String url) {
		if (url.contains("www.reddit.com"))
			return url + ".compact";
		else
			return url;
	}

	@Override
	public void onDestroy() {
		if (imageBitmap != null)
			imageBitmap.recycle();
		super.onDestroy();
	}

	private void setHeaderBarThumb(byte[] thumbBitmapArr) {
		Bitmap thumbBitmap = BitmapFactory.decodeByteArray(thumbBitmapArr, 0, thumbBitmapArr.length);
		Resources res = getResources();
		BitmapDrawable icon = null;

		icon = new BitmapDrawable(res, thumbBitmap);
		getActionBar().setIcon(icon);
	}

	private void handleIntent(Intent intent) {
		url = intent.getStringExtra("url");
		String displayName = intent.getStringExtra("name");
		setHeaderBarThumb(intent.getByteArrayExtra("imageBitMap"));
		setTitle("LurkR: " + displayName);
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
