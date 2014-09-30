package com.gery.redditlurker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

public class ActivityStoryContent extends Activity {
	private WebView webView;
	private String url;
	private Bitmap imageBitmap = null;
	private Bitmap storyImageBitmap = null;
	private String permalink;
	private String displayName;

	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState) {
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		this.handleIntent(getIntent());
		this.webView = new WebView(this);

		if (isImage()) {
			setContentView(R.layout.activity_image_display);
			setProgressBarIndeterminateVisibility(true);
			final ImageView imageView = (ImageView) findViewById(R.id.image_viewer);

			// final ProgressBar progressBar = (ProgressBar)
			// findViewById(R.id.progressBar);

			// This line shows progressBar again for recycled view
			// progressBar.setVisibility(View.VISIBLE);

			Picasso.with(this).load(url).into(imageView, new Callback() {
				@Override
				public void onSuccess() {
					setProgressBarIndeterminateVisibility(false);
					// progressBar.setVisibility(View.GONE);
					storyImageBitmap = getImageBitmap(imageView);
				}

				@Override
				public void onError() {
					// Set Error Image
				}
			});

		} else {

			setContentView(R.layout.activity_story_content);
			setProgressBarIndeterminateVisibility(true);
			webView = (WebView) findViewById(R.id.story_content_webview_view);
			WebSettings webSettings = webView.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webView.setWebChromeClient(new WebChromeClient() {
			});

			// webSettings.setBuiltInZoomControls(true);
			// webSettings.setAllowContentAccess(true);
			// webSettings.setLoadsImagesAutomatically(true);
			// webSettings.setLoadWithOverviewMode(true);
			// webSettings.setSupportZoom(true);
			// webSettings.setUseWideViewPort(true);
			webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageFinished(WebView view, String url) {
					setProgressBarIndeterminateVisibility(false);
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

	private void handleIntent(Intent intent) {
		url = intent.getStringExtra("url");
		permalink = intent.getStringExtra("permalink");
		displayName = intent.getStringExtra("name");
		setHeaderBarThumb(intent.getByteArrayExtra("imageBitMap"));
		setTitle(displayName);
	}

	private boolean isImage() {
		return (url.endsWith(".jpg") || url.endsWith("png"));
	}

	private void saveImageToGallery() {
		OutputStream output;

		// Find the SD Card path
		File filepath = Environment.getExternalStorageDirectory();

		// Create a new folder in SD Card
		File dir = new File(filepath.getAbsolutePath() + "/RedditLurkerIMG/");
		dir.mkdirs();

		File file = new File(dir, System.currentTimeMillis() + ".jpg");

		try {
			output = new FileOutputStream(file);
			// Compress into png format image from 0% - 100%
			if (storyImageBitmap != null) {
				storyImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
				output.flush();
				output.close();
			}
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ContentValues values = new ContentValues();

		values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
		values.put(Images.Media.MIME_TYPE, "image/jpeg");
		values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());

		this.getContentResolver().insert(Images.Media.EXTERNAL_CONTENT_URI, values);
		Toast.makeText(getApplicationContext(), "Image Saved to Gallery", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onPause() {
		webView.onPause();
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_actions, menu);
		// inflater.inflate(R.menu.share, menu);

		MenuItem copyUrl = menu.findItem(R.id.action_copy_url);
		copyUrl.setVisible(true);

		MenuItem openInBrowser = menu.findItem(R.id.action_open_in_browser);
		openInBrowser.setVisible(true);

		MenuItem itemViewCommewnts = menu.findItem(R.id.action_view_comments);
		itemViewCommewnts.setVisible(true);

		MenuItem itemSaveImage = menu.findItem(R.id.action_save_image);

		if (isImage()) {
			itemSaveImage.setVisible(true);// Change to true after testings

		}

		MenuItem itemShare = menu.findItem(R.id.action_share_menu);
		itemShare.setVisible(true);
		ShareActionProvider myShareActionProvider = (ShareActionProvider) itemShare.getActionProvider();
		if (myShareActionProvider != null) {
			myShareActionProvider.setShareIntent(createShareIntent());
		}

		return super.onCreateOptionsMenu(menu);
	}

	private Intent createShareIntent() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, url);
		return shareIntent;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
		switch (item.getItemId()) {
		case android.R.id.home: {
			webView.onPause();
			super.onBackPressed();
			this.finish();
			return true;
		}
		case R.id.action_save_image: {
			this.saveImageToGallery();
			return true;
		}
		case R.id.action_view_comments: {
			Intent nextActivity = new Intent(this, ActivityCommentsWebView.class);
			nextActivity.putExtra("permalink", permalink);
			nextActivity.putExtra("name", displayName);
			byte[] byteArray = null;
			if (imageBitmap != null) {
				ByteArrayOutputStream bStream = new ByteArrayOutputStream();
				imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
				byteArray = bStream.toByteArray();
			}
			nextActivity.putExtra("imageBitMap", byteArray);
			nextActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(nextActivity);
			return true;
		}
		case R.id.action_open_in_browser: {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(browserIntent);
			return true;
		}
		case R.id.action_copy_url: {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("simple text", url);
			clipboard.setPrimaryClip(clip);
			Toast.makeText(getApplicationContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
