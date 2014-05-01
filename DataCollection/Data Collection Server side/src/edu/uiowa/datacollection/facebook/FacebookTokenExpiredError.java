package edu.uiowa.datacollection.facebook;

import facebook4j.FacebookException;

public class FacebookTokenExpiredError extends Throwable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6542481013169131681L;
	public static final int TOKEN_EXPIRED_ERROR = 190;
	private String phoneNumber;
	private FacebookException error;

	public FacebookTokenExpiredError(FacebookException e)
	{
		phoneNumber = "";
		error = e;
	}
	
	public FacebookTokenExpiredError(String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber)
	{
		this.phoneNumber = phoneNumber;
	}
	
	public String getPhoneNumber()
	{
		return phoneNumber;
	}
	
	public String getMessage()
	{
		return error.getMessage();
	}
}
