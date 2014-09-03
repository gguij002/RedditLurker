package com.gery.redditlurker;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ChannelBaseAdapter extends BaseAdapter {

	private static List<StoryInfo> list;
	private LayoutInflater mInflater;
	private Context fragmentContext;

	public ChannelBaseAdapter(Context fragmentContext, List<StoryInfo> storieList) {
		list = storieList;
		mInflater = LayoutInflater.from(fragmentContext);
		this.fragmentContext = fragmentContext;
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
			convertView = mInflater.inflate(R.layout.channel_list_items, null);
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

		final StoryInfo storyInfo = list.get(position);
		holder.title.setText(storyInfo.title);
		holder.author.setText(storyInfo.author);
		holder.subreddit.setText(storyInfo.subreddit);
		holder.comments.setText(constructCommentsUpsTime(position));
		holder.comments.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent nextActivity = new Intent(fragmentContext, ActivityCommentsWebView.class);
				nextActivity.putExtra("permalink", storyInfo.permalink);
				nextActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				fragmentContext.startActivity(nextActivity);
			}
		});

		Bitmap image_bits = storyInfo.imageBitMap;
		if (image_bits != null)
			holder.thumbView.setImageBitmap(image_bits);

		return convertView;
	}

	private String constructCommentsUpsTime(int listItem) {
		StoryInfo story = list.get(listItem);
		long comments = story.num_comments;
		long score = story.score;
		double time = story.created;

		String commentsUpsTime = "" + comments + "\n" + score;

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
