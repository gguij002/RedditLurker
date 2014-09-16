package com.gery.redditlurker;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.ocpsoft.prettytime.PrettyTime;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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
import android.widget.ProgressBar;
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
		final ViewHolder holder;
		PrettyTime p = new PrettyTime();

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.channel_list_items, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.story_title1);
			holder.author = (TextView) convertView.findViewById(R.id.author_textview);
			holder.ups = (TextView) convertView.findViewById(R.id.ups_text_view);
			holder.comments = (Button) convertView.findViewById(R.id.comments_button);
			holder.thumbView = (ImageView) convertView.findViewById(R.id.story_thumb_view1);
			holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar_channel_image);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final StoryInfo storyInfo = list.get(position);
		holder.title.setText(storyInfo.title);
		holder.author.setText(p.format(storyInfo.getCreated_UTC_formatted()) + " by " + storyInfo.author);
		holder.ups.setText("Up: " + storyInfo.ups);
		holder.comments.setText(storyInfo.num_comments + "");
		holder.comments.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent nextActivity = new Intent(fragmentContext, ActivityCommentsWebView.class);
				nextActivity.putExtra("permalink", storyInfo.permalink);
				nextActivity.putExtra("name", storyInfo.subreddit);
				byte[] byteArray = null;
				if (storyInfo.imageBitMap != null) {
					ByteArrayOutputStream bStream = new ByteArrayOutputStream();
					storyInfo.imageBitMap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
					byteArray = bStream.toByteArray();
				}
				nextActivity.putExtra("imageBitMap", byteArray);
				nextActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				fragmentContext.startActivity(nextActivity);
			}
		});

		holder.progressBar.setVisibility(View.GONE);
		if (storyInfo.isValidThumbNail()) {
			if (storyInfo.thumbnail.equalsIgnoreCase("nsfw")) {
				holder.thumbView.setImageResource(R.drawable.ic_nsfw_image);
			} else if (storyInfo.thumbnail.equalsIgnoreCase("self")) {
				holder.thumbView.setImageResource(R.drawable.ic_launcher);
			} else {
				holder.progressBar.setVisibility(View.VISIBLE);
				Picasso.with(fragmentContext).load(storyInfo.thumbnail).into(holder.thumbView, new Callback() {
					@Override
					public void onSuccess() {
						holder.progressBar.setVisibility(View.GONE);
					}

					@Override
					public void onError() {
						// error
					}
				});
			}
		}

		return convertView;
	}

	static class ViewHolder {
		public TextView author;
		public TextView title;
		public Button comments;
		public ImageView thumbView;
		public TextView ups;
		public ProgressBar progressBar;
	}

}
