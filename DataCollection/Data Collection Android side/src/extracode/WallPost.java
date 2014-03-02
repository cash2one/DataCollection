package extracode;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WallPost
{
	// The person whose wall was posted on.
	private String wallOwnerID;
	private String wallOwnerName;
	
	//Post id
	private String postID;
	
	//The content of the post
	private String content;
	
	
	public WallPost(Element tbodyContent)
	{
		//Get the user id by looking for the data hovercard attribute
		for (Element e : tbodyContent.getElementsByAttribute("data-hovercard"))
		{
			String dataHoverString = e.attr("data-hovercard");
			if (dataHoverString.contains("/ajax/hovercard/user.php?id="))
			{
				wallOwnerID = dataHoverString.substring(dataHoverString.indexOf("=") + 1);
				wallOwnerName = e.text();
			}
		}
		
		/*
		 * Get the post id by looking for the timestamp, as it has a link.
		 * The timestamp class has an id of _k3z, and so its searchable by that.
		 * We then extract the post id from the href attribute.
		 */
		Element timePostInfo = tbodyContent.select("a[class=_k3z]").get(0);
		String hrefString = timePostInfo.attr("href");
		postID = hrefString.substring(hrefString.lastIndexOf("/") + 1);
		
		
		/*
		 * The h5 element with a class name of uiStreamMessage contains
		 * the text displayed, what the user posted.
		 */
		Elements h5 = tbodyContent.select("h5[class=uiStreamMessage]");
		content = h5.get(0).text();
	}


	public String getWallOwnerID()
	{
		return wallOwnerID;
	}


	public String getWallOwnerName()
	{
		return wallOwnerName;
	}


	public String getPostID()
	{
		return postID;
	}


	public String getContent()
	{
		return content;
	}
}