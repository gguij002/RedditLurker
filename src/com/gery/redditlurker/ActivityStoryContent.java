package com.gery.redditlurker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.gery.database.Connection;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;

public class ActivityStoryContent extends Activity {
	private WebView webView;
	private String url;
	private Bitmap imageBitmap = null;
	private Bitmap storyImageBitmap = null;

	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.handleIntent(getIntent());

		if (isImage()) {
			setContentView(R.layout.activity_image_display);
			
			
			
			final ImageView imageView = (ImageView) findViewById(R.id.image_viewer);
			final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

			// This line shows progressBar again for recycled view
			progressBar.setVisibility(View.VISIBLE);
			
			Picasso.with(this).load(url).into(imageView, new Callback() {
				@Override
				public void onSuccess() {
					progressBar.setVisibility(View.GONE);
					storyImageBitmap = getImageBitmap(imageView);
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
	
	private Bitmap getImageBitmap(ImageView v) {
		BitmapDrawable drawable = (BitmapDrawable) v.getDrawable();
		Bitmap bitmap = drawable.getBitmap();

		return bitmap;
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

	
	private void saveImageToGallery()
	{
		OutputStream output;
		
		// Find the SD Card path
        File filepath = Environment.getExternalStorageDirectory();

        // Create a new folder in SD Card
        File dir = new File(filepath.getAbsolutePath() + "/RedditLurkerIMG/");
        dir.mkdirs(); 
        
        File file = new File(dir, "Wallpaper.jpg" );
        
        try {
			output = new FileOutputStream(file);
			 // Compress into png format image from 0% - 100%
			if(storyImageBitmap != null){
				storyImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
				output.flush();
				output.close();
			}
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        ContentValues values = new ContentValues();

	    values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
	    values.put(Images.Media.MIME_TYPE, "image/jpeg");
	    values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());

	    this.getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_actions, menu);

		MenuItem itemSearch = menu.findItem(R.id.action_search_widget);
		itemSearch.setVisible(false);
		
		MenuItem itemFav = menu.findItem(R.id.action_fav);
		itemFav.setVisible(false);
		
		MenuItem itemSaveImage = menu.findItem(R.id.action_save_image);
		if(isImage())
			itemSaveImage.setVisible(true);//Change to true after testings
		else
			itemSaveImage.setVisible(false);
		
		return super.onCreateOptionsMenu(menu);
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
		switch (item.getItemId()) {
		case android.R.id.home: {
			super.onBackPressed();
			return true;
		}
		case R.id.action_save_image: {
			this.saveImageToGallery();
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
