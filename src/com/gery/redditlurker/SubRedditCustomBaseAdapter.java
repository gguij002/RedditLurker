package com.gery.redditlurker;
import java.util.List;

import android.R.mipmap;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SubRedditCustomBaseAdapter extends BaseAdapter {
	 private static List<SubReddit> list;
	 
	 private LayoutInflater mInflater;

	 public SubRedditCustomBaseAdapter(Context context, List<SubReddit> listItems) {
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

	 public View getView(int position, View convertView, ViewGroup parent) {
	  ViewHolder holder;
	  if (convertView == null) {
	   convertView = mInflater.inflate(R.layout.all_aubreddit_list_item, null);
	   holder = new ViewHolder();
	   holder.txtTitle = (TextView) convertView.findViewById(R.id.sub_reddit_list_item_title_text);
	   holder.txtLink = (TextView) convertView.findViewById(R.id.sub_reddit_list_item_link_text);
	//   holder.thumbView = (ImageView) convertView.findViewById(R.id.date);

	   convertView.setTag(holder);
	  } else {
	   holder = (ViewHolder) convertView.getTag();
	  }
	  
	  //COME BACK EHREE
	  ImageButton button = (ImageButton)convertView.findViewById(R.id.imagebutton_go);
	  button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				 Intent i = new Intent(mInflater.getContext(), SubRedditActivity.class);
				 mInflater.getContext().startActivity(i);
			}
	  });
	  
	  holder.txtTitle.setText(list.get(position).title);
	  String link = list.get(position).link;
	  holder.txtLink.setText(link.substring(0, link.length()-1));
	  
//	  if(list.get(position).thumbUrl != null)
//		  new DownloadImageTask(holder.thumbView)
//	      .execute(list.get(position).thumbUrl);
//	  holder.comment.setText(list.get(position).comments);
	  
	  return convertView;
	 }

	 static class ViewHolder {
	  TextView txtTitle;
	  TextView txtLink;
	  ImageView thumbView;
	  TextView comment;
	 }
	}