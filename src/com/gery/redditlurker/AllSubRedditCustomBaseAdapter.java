package com.gery.redditlurker;

import java.util.List;

import com.gery.database.SubRedditsDataSource;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.Settings.System;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AllSubRedditCustomBaseAdapter extends BaseAdapter {
	private static List<SubRedditInfo> list;

	private LayoutInflater mInflater;

	public AllSubRedditCustomBaseAdapter(Context context,
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
		final ViewHolder holder;
		final SubRedditInfo subReddit = list.get(position);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.all_aubreddit_list_item,
					null);
			holder = new ViewHolder();
			holder.displayName = (TextView) convertView
					.findViewById(R.id.sub_reddit_list_item_displayName_text);
			holder.txtLink = (TextView) convertView
					.findViewById(R.id.sub_reddit_list_item_link_text);
			holder.goButton = (ImageButton) convertView
					.findViewById(R.id.imagebutton_go);
			holder.goButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent i = new Intent(mInflater.getContext(), SubRedditChannelActivity.class);
					i.putExtra("subReddit", list.get(position).url);
					mInflater.getContext().startActivity(i);
				}
			});
			
			holder.favoriteButton = (ImageButton) convertView.findViewById(R.id.all_sub_favorite_image_button);
			if(subReddit.favorite)
				holder.favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
			else
				holder.favoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
			
			holder.favoriteButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					SubRedditsDataSource srDataSource = new SubRedditsDataSource(parent.getContext());
					srDataSource.open();
					if(subReddit.favorite)//Its already fav. Delete
					{
						subReddit.favorite = false;
						srDataSource.deleteSubReddit(subReddit);
						EnteredSubRedditsFragment.subRedditsList.remove(subReddit);
						holder.favoriteButton.setImageResource(android.R.drawable.btn_star_big_off);
					}//not fav Add
					else{
						subReddit.favorite = true;
						srDataSource.addSubRedditToDB(subReddit);
						EnteredSubRedditsFragment.subRedditsList.add(subReddit);
						holder.favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
					}
					ListView lView = (ListView)parent.findViewById(R.id.entered_subreddit_list);
		        	((EnteredSubredditCustomBaseAdapter) lView.getAdapter()).notifyDataSetChanged();
					//AllSubRedditsFragment.addedItem = false;
					srDataSource.close();
				}
			});

			holder.thumbView = (ImageView) convertView.findViewById(R.id.subreddit_thumb_view);
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

		Bitmap image_bits = list.get(position).imageBitMap;
		if (image_bits != null)
			holder.thumbView.setImageBitmap(image_bits);

		return convertView;
	}

	static class ViewHolder {
		TextView displayName;
		TextView txtLink;
		ImageView thumbView;
		TextView comment;
		ImageButton goButton;
		ImageButton favoriteButton;
	}
}