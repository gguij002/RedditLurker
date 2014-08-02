package com.gery.redditlurker;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import android.widget.ImageView;

public class SubReddit 
{
	String title;
	String link;
	String description;
	ImageView image;
	Element element;
	
	public SubReddit(Element element)
	{
		this.element = element;
	}
	
	public SubReddit execute()
	{
		this.title = createTitle(element);
		this.description = createDescription(element);
		this.link = createLink(element);
		
		return this;
	}
	
	private String createTitle(Element element)
	{
		return getCategoryFromXML("title");
	}
	
	private String getCategoryFromXML(String category)
	{
		Element singleItem = (Element) element.getElementsByTagName(category).item(0);
		Node textText = (Node) singleItem.getChildNodes().item(0);
		return textText.getNodeValue().trim();
	}
	
	private String createLink(Element element)
	{
		return getCategoryFromXML("link");
	}
	
	private String createDescription(Element element)
	{
		return getCategoryFromXML("description");
	}
}
