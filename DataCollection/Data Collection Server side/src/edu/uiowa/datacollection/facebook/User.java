package edu.uiowa.datacollection.facebook;

public class User
{
	public static final int PHONE_NUMBER_TYPE = 0;
	public static final int FACEBOOK_TYPE = 1;
	public static final int TWITTER_TYPE = 2;

	/**
	 * The User's unique Facebook ID
	 */
	private String facebookID;

	/**
	 * The User's Tweet ID
	 */
	private String tweetID;

	/**
	 * The User's phone #
	 */
	private String phoneNum;

	/**
	 * The user's name, it can only be filled in following a Facebook graph
	 * request.
	 */
	private String name = "";

	/**
	 * This creates a User object, which is used to hold Facebook User ID's
	 * 
	 * @param id
	 *            The unique Facebook ID
	 */
	public User(String fid, String tid, String pnum)
	{
		this.facebookID = fid;
		this.tweetID = tid;
		this.phoneNum = pnum;
	}

	public User(String id, int type)
	{
		if (type == PHONE_NUMBER_TYPE)
		{
			this.phoneNum = id;
		}
		else if (type == FACEBOOK_TYPE)
		{
			this.facebookID = id;
		}
		else if (type == TWITTER_TYPE)
		{
			this.tweetID = id;
		}
		else
		{
			throw new IllegalArgumentException("Invalid User data type");
		}
	}

	/**
	 * 
	 * @return The User's unique Facebook ID
	 */
	public String getFacebookId()
	{
		return facebookID;
	}

	/**
	 * get the user's Tweeter ID
	 */

	public String getTweetId()
	{
		return tweetID;
	}

	/**
	 * get the user's phone #
	 */

	public String getPhoneNum()
	{
		return phoneNum;
	}

	/**
	 * Overrides Object's equals method, uses the Facebook ID for comparison.
	 */
	public boolean equals(Object other)
	{
		if (other instanceof User)
		{
			return (((User) other).getFacebookId().equals(facebookID));
		}
		return false;
	}

	/**
	 * The string represntation of the object. If there is no name with the user
	 * it returns their ID, otherwise it prints their name.
	 */
	public String toString()
	{
		if (name.length() == 0)
			return facebookID;
		else
			return name;
	}

	/**
	 * The simplist hashCode method, use the string representation of the ID and
	 * use Java's build in string.hashCode method
	 */
	public int hashCode()
	{
		return facebookID.hashCode();
	}

	/**
	 * 
	 * @return The user's name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * This method sets the user's name.
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		if (name == null)
			this.name = "";
		else
			this.name = name;
	}
}
