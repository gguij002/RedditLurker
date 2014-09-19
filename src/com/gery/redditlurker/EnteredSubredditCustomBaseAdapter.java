package com.gery.redditlurker;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.gery.database.SubRedditsDataSource;

public class EnteredSubredditCustomBaseAdapter extends BaseAdapter {
	private List<SubRedditInfo> list;

	private LayoutInflater mInflater;

	public EnteredSubredditCustomBaseAdapter(Context context, List<SubRedditInfo> listItems) {
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
			convertView = mInflater.inflate(R.layout.entered_subreddit_list_item, null);
			holder = new ViewHolder();
			holder.linkText = (TextView) convertView.findViewById(R.id.entered_sub_reddit_list_item_link_text);
			holder.displayName = (TextView) convertView.findViewById(R.id.entered_sub_reddit_list_item_displayName_text);

			holder.deleteButton = (ImageButton) convertView.findViewById(R.id.entered_imagebutton_delete);

			holder.thumbView = (ImageView) convertView.findViewById(R.id.entered_subreddit_thumb_view);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SubRedditInfo subReddit = list.get(position);
				list.remove(position);

				SubRedditsDataSource srDataSource = new SubRedditsDataSource(parent.getContext());
				srDataSource.open();
				srDataSource.deleteSubReddit(subReddit.getName());
				srDataSource.close();

				notifyDataSetChanged();
			}
		});

		// Second TextView in the list item
		String header_title_text = list.get(position).header_title;
		if (header_title_text != null && !header_title_text.isEmpty())
			holder.linkText.setText(header_title_text);
		else
			holder.linkText.setText(list.get(position).display_name);

		String link = list.get(position).url;
		holder.displayName.setText(link.substring(0, link.length() - 1));

		Bitmap image_bits = list.get(position).imageBitMap;
		if (image_bits != null)
			holder.thumbView.setImageBitmap(image_bits);

		return convertView;
	}

	static class ViewHolder {
		TextView linkText;
		TextView displayName;
		ImageView thumbView;
		ImageButton deleteButton;
	}
}
