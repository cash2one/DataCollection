package edu.uiowa.datacollection.facebook;

import facebook4j.FacebookException;

public class FacebookUnhandledException extends Throwable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3541404434330988847L;
	public static final int TOKEN_EXPIRED_ERROR = 190;
	private String phoneNumber;
	private FacebookException facebookError;

	public FacebookUnhandledException(FacebookException e)
	{
		phoneNumber = "";
		this.facebookError = e;
	}

	public FacebookUnhandledException(FacebookException e, String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
		this.facebookError = e;
	}

	public void setPhoneNumber(String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
	}

	public String getPhoneNumber()
	{
		return phoneNumber;
	}
	
	public FacebookException getFacebookException()
	{
		return facebookError;
	}
}
