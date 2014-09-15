package com.gery.redditlurker;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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

			// This line shows progressBar again for recycled view
			progressBar.setVisibility(View.VISIBLE);
			
			Picasso.with(this).load(url).into(imageView, new Callback() {
				@Override
				public void onSuccess() {
					progressBar.setVisibility(View.GONE);
				}

				@Override
				public void onError() {
					// Set Error Image
				}
			});

		} else {
			
			final ProgressDialog dialog = new ProgressDialog(this);
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					onBackPressed();
				}
			});
			dialog.setMessage("Loading...");
			dialog.show();
			
			setContentView(R.layout.activity_story_content);
			
			webView = (WebView) findViewById(R.id.story_content_webview_view);
			webView.getSettings().setJavaScriptEnabled(true);
			webView.setWebViewClient(new WebViewClient() {
			    @Override
			    public void onPageFinished(WebView view, String url) {
			    	dialog.dismiss();
			    }
			});
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
		if(thumbBitmapArr != null)
		{
			Bitmap thumbBitmap = BitmapFactory.decodeByteArray(thumbBitmapArr, 0, thumbBitmapArr.length);
			Resources res = getResources();
			BitmapDrawable icon = null;
	
			icon = new BitmapDrawable(res, thumbBitmap);
			getActionBar().setIcon(icon);
		}
		else
		{
			getActionBar().setIcon(R.drawable.ic_launcher);
		}
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
