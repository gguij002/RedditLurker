package com.gery.redditlurker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

import org.json.simple.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.gery.database.RedditRSSReader;
import com.gery.database.SubRedditsDataSource;
import com.gery.database.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class ActivityStoryContent extends Activity {
	private WebView webView;
	private String url;
	private Bitmap imageBitmap = null;
	private Bitmap storyImageBitmap = null;
	private String permalink;
	private String displayName;
	private String subRedditId;
	private SubRedditInfo sub;
	// make sure to set Target as strong reference
		private Target loadtarget;

	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState) {
		Utils.setPrefTheme(this);
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		this.handleIntent(getIntent());
		this.webView = new WebView(this);

		setContentView(R.layout.activity_story_content);
		setProgressBarIndeterminateVisibility(true);
		webView = (WebView) findViewById(R.id.story_content_webview_view);
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);

		// new
		webSettings.setLoadsImagesAutomatically(true);
		webView.setBackgroundColor(Color.TRANSPARENT);
		webView.getSettings().setUseWideViewPort(true);
		webView.setScrollbarFadingEnabled(true);
		webView.setInitialScale(1);

		webSettings.setLoadWithOverviewMode(true);
		webSettings.setUseWideViewPort(true);

		webSettings.setBuiltInZoomControls(true);
		webSettings.setDisplayZoomControls(false);
		webSettings.setSupportZoom(true);

		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.setWebChromeClient(new WebChromeClient() {
		});

		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				setProgressBarIndeterminateVisibility(false);
			}
		});
		webView.loadUrl(formatURL(url));
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
		subRedditId = intent.getStringExtra("subRedditId");
		setHeaderBarThumb(intent.getByteArrayExtra("imageBitMap"));
		setTitle(displayName);
	}

	private boolean isImage() {
		return (url.endsWith(".jpg") || url.endsWith("png"));
	}
	
	

	public void loadBitmap(String url) {

	    if (loadtarget == null) loadtarget = new Target() {
	        @Override
	        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
	            // do something with the Bitmap
	            handleLoadedBitmap(bitmap);
	        }

			@Override
			public void onBitmapFailed(Drawable arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPrepareLoad(Drawable arg0) {
				// TODO Auto-generated method stub
				
			}
	    };

	    Picasso.with(this).load(url).into(loadtarget);
	}

	public void handleLoadedBitmap(Bitmap b) {
	    saveImageToGallery(b);
	}

	private void saveImageToGallery(Bitmap bitmap) {
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
			if (bitmap != null) {
				
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
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

		MenuItem itemMakeSubRedditFav = menu.findItem(R.id.action_make_subreddit_fav);
		itemMakeSubRedditFav.setVisible(false);

		itemMakeSubRedditFav.setTitle("Favorite: " + displayName);

		MenuItem itemSaveImage = menu.findItem(R.id.action_save_image);

		if (isImage()) {
			itemSaveImage.setVisible(true);
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
			this.loadBitmap(url);
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
		case R.id.action_make_subreddit_fav: {
			SubRedditsDataSource srDataSource = new SubRedditsDataSource(this);
			srDataSource.open();
			if (!srDataSource.isRawSubRedditExist(subRedditId)) {
				try {
					sub = new LoadSubReddit(this, displayName).execute().get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				final ImageView holder = new ImageView(this);

				Picasso.with(this).load(sub.header_img).into(holder, new Callback() {
					@Override
					public void onSuccess() {
						sub.imageBitMap = getImageBitmap(holder);
					}

					@Override
					public void onError() {
						// Set Error Image
					}
				});
				srDataSource.addSubRedditToDB(sub);
				Toast.makeText(getApplicationContext(), displayName + " is now Favorite", Toast.LENGTH_SHORT).show();
			}
			srDataSource.close();
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

	// Load Channel To Store on DB
	class LoadSubReddit extends AsyncTask<String, String, SubRedditInfo> {
		private String subRedditName;
		private Context context;
		private ProgressDialog pDialogChannel;

		public LoadSubReddit(Context context, String subRedditName) {
			this.subRedditName = subRedditName;
			this.context = context;
		}

		protected void onPreExecute() {
			super.onPreExecute();
			pDialogChannel = new ProgressDialog(context);
			pDialogChannel.setMessage("Saving Favorite SubReddit ...");
			pDialogChannel.setIndeterminate(false);
			pDialogChannel.setCancelable(false);
			pDialogChannel.show();
		}

		protected SubRedditInfo doInBackground(String... args) {
			// "http://reddit.com/r/reddits.rss?limit=[limit]&after=[after]";
			final String REDDIT_SUBREDDITS_URL = URLCreate();
			System.out.println("REDDIT_SUBREDDITS_URL: " + REDDIT_SUBREDDITS_URL);

			// Create List Of Subreddit
			JSONObject subRedditsJSON = new RedditRSSReader(REDDIT_SUBREDDITS_URL).execute();
			JSONObject data = (JSONObject) subRedditsJSON.get("data");

			SubRedditInfo item = new SubRedditInfo(data).execute();
			item.favorite = true;
			return item;
		}

		private String URLCreate() {
			return "http://www.reddit.com/r/" + subRedditName + "/about.json";
		}

		protected void onPostExecute(final SubRedditInfo subRedditInfo) {
			pDialogChannel.dismiss();
			super.onPostExecute(subRedditInfo);
		}
	}

}
