package datacollection;

public class User
{
	/**
	 * The User's unique Facebook ID
	 */
	private String id;
	
	/**
	 * The user's name, it can only be filled in following a Facebook graph request.
	 */
	private String name = "";

	/**
	 * This creates a User object, which is used to hold Facebook User ID's
	 * @param id The unique Facebook ID
	 */
	public User(String id)
	{
		this.id = id;
	}

	/**
	 * 
	 * @return The User's unique Facebook ID
	 */
	public String getId()
	{
		return id;
	}
	
	/**
	 * Overrides Object's equals method, uses the Facebook ID for comparison.
	 */
	public boolean equals(Object other)
	{
		if (other instanceof User)
		{
			return (((User)other).getId().equals(id));
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
			return id;
		else
			return name;
	}
	
	/**
	 * The simplist hashCode method, use the string representation of the ID
	 * and use Java's build in string.hashCode method 
	 */
	public int hashCode()
	{
		return id.hashCode();
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
}
