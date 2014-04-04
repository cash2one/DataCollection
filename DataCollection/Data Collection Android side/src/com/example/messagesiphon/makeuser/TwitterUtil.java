package com.example.messagesiphon.makeuser;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterUtil
{

	private RequestToken requestToken = null;
	private AccessToken accessToken = null;
	private TwitterFactory twitterFactory = null;
	private Twitter twitter;

	public TwitterUtil()
	{
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder
				.setOAuthConsumerKey(ConstantValues.TWITTER_CONSUMER_KEY);
		configurationBuilder
				.setOAuthConsumerSecret(ConstantValues.TWITTER_CONSUMER_SECRET);
		Configuration configuration = configurationBuilder.build();
		twitterFactory = new TwitterFactory(configuration);
		twitter = twitterFactory.getInstance();
	}

	public TwitterFactory getTwitterFactory()
	{
		return twitterFactory;
	}

	public void setTwitterFactory(AccessToken accessToken)
	{
		twitter = twitterFactory.getInstance(accessToken);
	}

	public Twitter getTwitter()
	{
		return twitter;
	}

	public RequestToken getRequestToken()
	{
		try
		{
			requestToken = twitterFactory.getInstance().getOAuthRequestToken(
					ConstantValues.TWITTER_CALLBACK_URL);
			
		}
		catch (TwitterException e)
		{
			e.printStackTrace();
		}
		return requestToken;
	}

	public AccessToken getAccessToken(String verifier)
	{
		if (accessToken == null)
		{
			try
			{			
				accessToken = twitterFactory.getInstance().getOAuthAccessToken(
						requestToken, verifier);
			}
			catch (TwitterException e)
			{
				System.err.println("Error retrieving access token");
			}
		}
		return accessToken;
	}

	static TwitterUtil instance = new TwitterUtil();

	public static TwitterUtil getInstance()
	{
		return instance;
	}

	public void reset()
	{
		instance = new TwitterUtil();
	}
}
