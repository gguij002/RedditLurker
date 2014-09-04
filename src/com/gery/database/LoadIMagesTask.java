package com.gery.database;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.view.Display;
import android.view.WindowManager;

public class LoadIMagesTask extends AsyncTask<String, Void, Bitmap> {

	private Context context;

	public LoadIMagesTask(Context context) {
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Bitmap doInBackground(String... urls) {
		Bitmap map = null;
		for (String url : urls) {
			map = downloadImage(url);
		}
		return map;
	}

	// Sets the Bitmap returned by doInBackground
	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
	}

	// Creates Bitmap from InputStream and returns it
	private Bitmap downloadImage(String url) {
		Bitmap bitmap = null;
		InputStream stream = null;
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;

		try {
			stream = getHttpConnection(url);

			// bitmap = BitmapFactory.decodeStream(stream); //TODO: FUCKING BUG
			// ON
			// BitmapFactory.decodeStream(stream, null, options); Cant reset.
			bitmap = decodeSampledBitmapFromResource(stream, width, height, url);

			stream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return bitmap;
	}

	private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	private Bitmap decodeSampledBitmapFromResource(InputStream stream, int reqWidth, int reqHeight, String url) throws IOException {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(stream, null, options);
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		stream = getHttpConnection(url);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeStream(stream, null, options);
	}

	// Makes HttpURLConnection and returns InputStream
	private InputStream getHttpConnection(String urlString) throws IOException {
		InputStream stream = null;
		URL url = new URL(urlString);
		URLConnection connection = url.openConnection();

		try {
			HttpURLConnection httpConnection = (HttpURLConnection) connection;
			httpConnection.setRequestMethod("GET");
			httpConnection.connect();

			if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				for (int i = 0;; i++) {
					String headerName = httpConnection.getHeaderFieldKey(i);
					String headerValue = httpConnection.getHeaderField(i);
					System.out.println("CONNECTION H: " + headerName);
					System.out.println("CONNECTION V: " + headerValue);

					if (headerName == null && headerValue == null) {
						System.out.println("No more headers");
						break;
					}
				}
				stream = httpConnection.getInputStream();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return stream;
	}
}
