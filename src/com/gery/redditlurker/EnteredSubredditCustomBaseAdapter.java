package com.gery.redditlurker;

import java.util.List;

import com.gery.database.SubRedditsDataSource;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class EnteredSubredditCustomBaseAdapter extends BaseAdapter {
	private static List<SubRedditInfo> list;

	private LayoutInflater mInflater;

	public EnteredSubredditCustomBaseAdapter(Context context,
			List<SubRedditInfo> listItems) {
		list = listItems;
		mInflater = LayoutInflater.from(context);
	}

	public int getCount() {
		return list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, final ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.entered_subreddit_list_item,
					null);
			holder = new ViewHolder();
			holder.displayName = (TextView) convertView
					.findViewById(R.id.entered_sub_reddit_list_item_displayName_text);
			holder.txtLink = (TextView) convertView
					.findViewById(R.id.entered_sub_reddit_list_item_link_text);
			holder.goButton = (ImageButton) convertView
					.findViewById(R.id.entered_imagebutton_go);
			holder.goButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent i = new Intent(mInflater.getContext(),
							SubRedditChannelActivity.class);
					mInflater.getContext().startActivity(i);
				}
			});
			
			holder.deleteButton = (ImageButton) convertView
					.findViewById(R.id.entered_imagebutton_delete);
			holder.deleteButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					SubRedditInfo subReddit = list.get(position);
					SubRedditsDataSource srDataSource = new SubRedditsDataSource(parent.getContext());
					srDataSource.open();
					
					srDataSource.deleteSubReddit(subReddit);
					list.remove(position);
					srDataSource.close();
					notifyDataSetChanged();
					//Refresh Page
				}
			});


			holder.thumbView = (ImageView) convertView
					.findViewById(R.id.entered_subreddit_thumb_view);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// Second TextView in the list item
		String header_title_text = list.get(position).header_title;
		if (header_title_text != null && !header_title_text.isEmpty())
			holder.displayName.setText(header_title_text);
		else
			holder.displayName.setText(list.get(position).display_name);

		String link = list.get(position).url;
		holder.txtLink.setText(link.substring(0, link.length() - 1));

//		holder.goButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View arg0) {
//				Intent i = new Intent(mInflater.getContext(),
//						SubRedditActivity.class);
//				mInflater.getContext().startActivity(i);
//			}
//		});
		  
		Bitmap image_bits = list.get(position).imageBitMap;
		if (image_bits != null)
			holder.thumbView.setImageBitmap(image_bits);

		return convertView;
	}

	static class ViewHolder {
		TextView displayName;
		TextView txtLink;
		ImageView thumbView;
		ImageButton goButton;
		ImageButton deleteButton;
	}
}
