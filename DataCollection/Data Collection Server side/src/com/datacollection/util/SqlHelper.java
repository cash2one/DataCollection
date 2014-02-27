package com.datacollection.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlHelper {

	private Connection c=null;
	private Statement s=null;
	private ResultSet rs=null;
	
	public SqlHelper(){
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void executeUpdate(String sql){
		try {
			s =c.createStatement();
			s.executeUpdate(sql);
			s.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ResultSet executeQuery(String query){
		try {
			s = c.createStatement();
			rs = s.executeQuery( query );
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	     return rs;
	}
	
	
	public Connection getConnection(){
		return c;
	}
	
	public void close(){
		try {
			if(rs!=null&&!rs.isClosed())rs.close();
			if(s!=null&&!s.isClosed()) s.close();
			if(c!=null&&!c.isClosed()) c.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
