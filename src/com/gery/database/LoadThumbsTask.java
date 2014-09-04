package com.gery.database;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

public class LoadThumbsTask {

	private String imageURL;
	public Bitmap imageBitmap = null;

	public LoadThumbsTask(String imageURL) {
		this.imageURL = imageURL;
	}

	public LoadThumbsTask exceute() {
		this.imageBitmap = getImage(imageURL);
		return this;
	}

	private Bitmap getImage(String url) {
		Bitmap mIcon11 = null;
		try {
			InputStream in = new java.net.URL(url).openStream();
			mIcon11 = BitmapFactory.decodeStream(in);
		} catch (Exception e) {
			Log.e("Error", e.getMessage());
			e.printStackTrace();
		}
		int width = mIcon11.getWidth();
		int height = mIcon11.getHeight();

		if (width > 341 || height > 201) {
			// calculate the scale
			float scaleWidth = ((float) 100) / width;
			float scaleHeight = ((float) 100) / height;
			// create a matrix for the manipulation
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			mIcon11 = Bitmap.createBitmap(mIcon11, 0, 0, width, height, matrix, true);
		}

		return mIcon11;
	}

}
