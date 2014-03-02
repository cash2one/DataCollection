package com.datacollection.twitter;

public class User
{
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
	
	private String oauthToken;
	
	private String tokenSecret;
	
	/**
	 * The user's name, it can only be filled in following a Facebook graph request.
	 */
	private String name = "";

	/**
	 * This creates a User object, which is used to hold Facebook User ID's
	 * @param id The unique Facebook ID
	 */
	public User(String fid,String tid, String pnum)
	{
		this.facebookID = fid;
		this.tweetID=tid;
		this.phoneNum=pnum;
	}
	
	
	public User(String id,int type)
	{
		if(type==0){
			this.phoneNum=id;
		}
		else if(type==1){
			this.facebookID=id;
		}
		else this.tweetID=id;
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
	
	public String getTweetId(){
		return tweetID;
	}
	
	/**
	 * get the user's phone #
	 */
	
	public String getPhoneNum(){
		return phoneNum;
	}
	/**
	 * Overrides Object's equals method, uses the Facebook ID for comparison.
	 */
	public boolean equals(Object other)
	{
		if (other instanceof User)
		{
			return (((User)other).getTweetId().equals(tweetID));
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
			return tweetID;
		else
			return name;
	}
	
	/**
	 * The simplist hashCode method, use the string representation of the ID
	 * and use Java's build in string.hashCode method 
	 */
	public int hashCode()
	{
		return tweetID.hashCode();
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
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		if (name == null)
			this.name = "";
		else
			this.name = name;
	}


	public String getOauthToken() {
		return oauthToken;
	}


	public void setOauthToken(String oauthToken) {
		this.oauthToken = oauthToken;
	}


	public String getTokenSecret() {
		return tokenSecret;
	}


	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}
}
