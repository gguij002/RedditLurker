package com.gery.redditlurker;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import android.widget.ImageView;


import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;
import android.widget.ImageView;

public class SubRedditChannel 
{
	private Document doc;
	public SubRedditInfo subRedditInfo;
	public List<SubRedditListItem> redditListItems;
	public String channelDescription;
	public String channelTitle;
	public ImageView channelImage;
	public String imageStringUrl;
	
	public SubRedditChannel(Document doc)
	{
		this.doc = doc;
	}
	
	public void execute()
	{
		redditListItems = new ArrayList<SubRedditListItem>();
		Element channelElement = (Element) doc.getElementsByTagName("channel").item(0);
		//this.subRedditInfo = new SubRedditInfo(channelElement);
		redditListItems = createListItems(channelElement);
	}
	
	private void initChannelInformation(Element channelElement)
	{
		channelTitle = getChannelTitle(channelElement);
		channelDescription = getChannelDesc(channelElement);
		imageStringUrl = getImageURL(channelElement);
	
	}
	
	private String getChannelTitle(Element channelElement)
	{
		Element channelTitleElement =  (Element)channelElement.getElementsByTagName("title").item(0);
		Node textChannelTitle = channelTitleElement.getChildNodes().item(0);
		return textChannelTitle.getNodeValue().trim();
	}
	
	private String getChannelDesc(Element channelElement)
	{
		String desc = null;
		NodeList channelDescElements = channelElement.getElementsByTagName("description");
		try{
			
			Element channelDescElement = (Element)channelDescElements.item(0);
		    Node textChannelTitle = channelDescElement.getChildNodes().item(0);
		    desc = (String) (textChannelTitle == null ? textChannelTitle : textChannelTitle.getNodeValue().trim());
		}
		catch(Exception e)
		{
			Log.e("Error", e.getMessage());
	          e.printStackTrace();
	    }
		
		return desc;
	}
	
	private String getImageURL(Element channelElement)
	{
		Element imageElement = (Element) channelElement.getElementsByTagName("image").item(0);
		Element urlElement = (Element) imageElement.getElementsByTagName("url").item(0); 
		
		Node url = urlElement.getChildNodes().item(0);
		return url.getNodeValue().trim();
	}


	private List<SubRedditListItem> createListItems(Element channelElement)
	{
		 NodeList listOfStories = channelElement.getElementsByTagName("item");
		 int length = listOfStories.getLength();
		 
		 for (int i = 0; i < length; i++) {
			 SubRedditListItem item = new SubRedditListItem((Element)listOfStories.item(i));	
			 redditListItems.add(item);
	   	}
		 return redditListItems;
	}
}


