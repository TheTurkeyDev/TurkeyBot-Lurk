package com.theprogrammingturkey.turkeybot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseCore
{
	private static Connection con;

	public static void initDB(String url, String user, String password)
	{
		try
		{
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
			con = DriverManager.getConnection(url, user, password);
		} catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static void addChannel(int channelID)
	{
		try
		{
			Statement stmt = con.createStatement();
			stmt.execute("INSERT INTO `turkeybot_lurk`.`watched_channels` (channel_id) VALUES (" + channelID + ")");
		} catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static void removeChannel(int channelID)
	{
		try
		{
			Statement stmt = con.createStatement();
			stmt.execute("DELETE FROM `turkeybot_lurk`.`watched_channels` WHERE (`channel_id` = '" + channelID + "');");
		} catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public static List<Integer> getChannels()
	{
		List<Integer> toReturn = new ArrayList<>();
		try
		{
			Statement stmt = con.createStatement();
			ResultSet response = stmt.executeQuery("SELECT * FROM `turkeybot_lurk`.`watched_channels`;");
			while(response.next())
			{
				toReturn.add(response.getInt("channel_id"));
			}
		} catch(SQLException e)
		{
			e.printStackTrace();
		}

		return toReturn;

	}
}
