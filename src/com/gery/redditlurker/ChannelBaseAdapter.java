package com.gery.redditlurker;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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
			holder.title = (TextView) convertView.findViewById(R.id.story_title1);
			holder.author = (TextView) convertView.findViewById(R.id.author_textview);
			holder.subreddit = (TextView) convertView.findViewById(R.id.subreddit_textview_list_item);
			holder.comments = (Button) convertView.findViewById(R.id.comments_button);
			holder.thumbView = (ImageView) convertView.findViewById(R.id.story_thumb_view1);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.title.setText(list.get(position).title);
		holder.author.setText(list.get(position).author);
		holder.subreddit.setText(list.get(position).subreddit);
		holder.comments.setText(constructCommentsUpsTime(position));
		
		Bitmap image_bits = list.get(position).imageBitMap;
		if (image_bits != null)
			holder.thumbView.setImageBitmap(image_bits);

		return convertView;
	}
	
	private String constructCommentsUpsTime(int listItem)
	{
		StoryInfo story = list.get(listItem);
		long comments = story.num_comments;
		long score = story.score;
		double time = story.created;
		
		String commentsUpsTime = ""+comments + "\n" + score; 
		
		return commentsUpsTime;
	}
	
	static class ViewHolder {
		public TextView author;
		public TextView title;
		public Button comments; 
		public ImageView thumbView;
		public TextView subreddit;
	}

}
