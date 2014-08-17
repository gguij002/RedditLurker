package com.gery.redditlurker;

import java.util.List;

import com.gery.database.SubRedditsDataSource;
import com.gery.redditlurker.AllSubRedditCustomBaseAdapter.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ChannelBaseAdapter extends BaseAdapter {
	
	private static List<StoryInfo> list;
	private LayoutInflater mInflater;
	
	public ChannelBaseAdapter(Context fragmentContext, List<StoryInfo> storieList) {
		list = storieList;
		mInflater = LayoutInflater.from(fragmentContext);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.channel_list_items,
					null);
			holder = new ViewHolder();
			holder.displayName = (TextView) convertView
					.findViewById(R.id.story_title1);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		
		return convertView;
	}
	
	static class ViewHolder {
		TextView displayName;
	}

}
