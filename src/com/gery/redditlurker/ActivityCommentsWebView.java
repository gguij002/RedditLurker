package com.gery.redditlurker;

import com.gery.database.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ShareActionProvider;
import android.widget.Toast;

public class ActivityCommentsWebView extends Activity {
	private WebView webView;
	private String permalink;
	private String url;

	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState) {
		Utils.setPrefTheme(this);
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_comments_webview);
		setProgressBarIndeterminateVisibility(true);
		this.handleIntent(getIntent());

		webView = (WebView) findViewById(R.id.comments_webview_view);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				setProgressBarIndeterminateVisibility(false);
			}
		});
		url = createURL(this.permalink);
		webView.loadUrl(url);
	}

	private String createURL(String permalink) {
		return "http://www.reddit.com" + permalink + "/.compact";
	}

	private void handleIntent(Intent intent) {
		permalink = intent.getStringExtra("permalink");
		String displayName = intent.getStringExtra("name");
		setHeaderBarThumb(intent.getByteArrayExtra("imageBitMap"));
		setTitle(displayName);
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

		MenuItem copyUrl = menu.findItem(R.id.action_copy_url);
		copyUrl.setVisible(true);

		MenuItem openInBrowser = menu.findItem(R.id.action_open_in_browser);
		openInBrowser.setVisible(true);

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
		switch (item.getItemId()) {
		case android.R.id.home: {
			super.onBackPressed();
			return true;
		}
		case R.id.action_copy_url: {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("simple text", url);
			clipboard.setPrimaryClip(clip);
			Toast.makeText(getApplicationContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
			return true;
		}
		case R.id.action_open_in_browser: {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(browserIntent);
			return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
