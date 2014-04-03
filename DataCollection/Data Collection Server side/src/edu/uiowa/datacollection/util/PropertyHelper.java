package edu.uiowa.datacollection.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class PropertyHelper
{

	private String urladdress;

	/*
	 * Other properties can be added here
	 */
	public PropertyHelper(String fileName) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		ArrayList<String> propertyList = new ArrayList<String>();
		String line;
		while ((line = br.readLine()) != null)
		{
			propertyList.add(line);
		}
		br.close();
		for (int i = 0; i < propertyList.size(); i++)
		{
			String property = propertyList.get(i);
			String tmp[] = property.split("=");
			if (tmp[0].equals("URL"))
			{
				this.urladdress = tmp[1];
			}
		}

	}

	public String getURLAddress()
	{
		return urladdress;
	}
}
