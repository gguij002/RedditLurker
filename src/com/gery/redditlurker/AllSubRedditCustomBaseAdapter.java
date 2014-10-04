package com.gery.redditlurker;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gery.database.SubRedditsDataSource;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class AllSubRedditCustomBaseAdapter extends ArrayAdapter<SubRedditInfo> {
	public List<SubRedditInfo> list;

	private LayoutInflater mInflater;

	public AllSubRedditCustomBaseAdapter(Context context, int res, List<SubRedditInfo> listItems) {
		super(context, res, listItems);
		list = listItems;
		mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return list.size();
	}

	public SubRedditInfo getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, final ViewGroup parent) {
		final ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.all_aubreddit_list_item, null);
			holder = new ViewHolder();
			holder.linkText = (TextView) convertView.findViewById(R.id.sub_reddit_list_item_link_text);
			holder.displayNameN = (TextView) convertView.findViewById(R.id.sub_reddit_list_item_displayName_text);
			holder.favoriteButton = (ImageButton) convertView.findViewById(R.id.all_sub_favorite_image_button);
			holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar_allsub_image);
			holder.thumbView = (ImageView) convertView.findViewById(R.id.subreddit_thumb_view);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final SubRedditInfo subRedditInfo = list.get(position);

		holder.favoriteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SubRedditsDataSource srDataSource = new SubRedditsDataSource(parent.getContext());
				srDataSource.open();
				if (subRedditInfo.favorite)// Its already fav. Delete
				{
					subRedditInfo.favorite = false;
					srDataSource.deleteSubReddit(subRedditInfo.getName());
					holder.favoriteButton.setImageResource(R.drawable.ic_favorite_off_new);
				}// not fav Add
				else {
					subRedditInfo.favorite = true;
					srDataSource.addSubRedditToDB(subRedditInfo);
					holder.favoriteButton.setImageResource(R.drawable.ic_favorite_yellow);
				}

				srDataSource.close();
			}
		});

		if (subRedditInfo.favorite)
			holder.favoriteButton.setImageResource(R.drawable.ic_favorite_yellow);
		else
			holder.favoriteButton.setImageResource(R.drawable.ic_favorite_off_new);

		// Second TextView in the list item
		String header_title_text = subRedditInfo.header_title;
		if (header_title_text != null && !header_title_text.isEmpty())
			holder.linkText.setText(header_title_text);
		else
			holder.linkText.setText(subRedditInfo.display_name);

		String link = subRedditInfo.url;
		holder.displayNameN.setText(link.substring(0, link.length() - 1));

		holder.progressBar.setVisibility(View.GONE);
		if (subRedditInfo.isValidThumbNail()) {
			if (subRedditInfo.header_img.equalsIgnoreCase("nsfw")) {
				holder.thumbView.setImageResource(R.drawable.ic_nsfw_image);
			} else if (subRedditInfo.header_img.equalsIgnoreCase("self")) {
				holder.thumbView.setImageResource(R.drawable.ic_launcher);
			} else {
				holder.progressBar.setVisibility(View.VISIBLE);
				Picasso.with(parent.getContext()).load(subRedditInfo.header_img).into(holder.thumbView, new Callback() {
					@Override
					public void onSuccess() {
						holder.progressBar.setVisibility(View.GONE);
						subRedditInfo.setImageBitMap(getImageBitmap(holder.thumbView));
					}

					@Override
					public void onError() {
						// error
					}
				});
			}
		} else
			holder.thumbView.setImageResource(R.drawable.ic_launcher);

		return convertView;
	}

	private Bitmap getImageBitmap(ImageView v) {
		BitmapDrawable drawable = (BitmapDrawable) v.getDrawable();
		Bitmap bitmap = drawable.getBitmap();

		return bitmap;
	}

	static class ViewHolder {
		public ProgressBar progressBar;
		TextView linkText;
		TextView displayNameN;
		ImageView thumbView;
		TextView comment;
		ImageButton favoriteButton;
	}
}