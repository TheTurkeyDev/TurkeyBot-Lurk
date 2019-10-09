package com.theprogrammingturkey.turkeybot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.theprogrammingturkey.ggserver.ServerCore;
import com.theprogrammingturkey.ggserver.ServerCore.Level;
import com.theprogrammingturkey.ggserver.news.INewsData;
import com.theprogrammingturkey.ggserver.news.NewsDispatcher;
import com.theprogrammingturkey.ggserver.services.IServiceCore;
import com.theprogrammingturkey.ggserver.services.ServiceManager;
import com.theprogrammingturkey.turkeybot.commands.ConsoleCommands;
import com.theprogrammingturkey.twitchbot.base.TwitchBot;

public class TurkeyBot extends TwitchBot implements IServiceCore
{

	public static final String VERSION = "1.13";

	private static String dbURL = "";
	private static String dbUser = "";
	private static String dbPassword = "";

	public static TurkeyBot bot;
	public static JsonParser json;
	private static List<String> chat = new ArrayList<String>();

	private String[] keywords = { "turkey", "turkey2349", "chancecubes", "chance cubes", "chancecube", "chance cube", "turkeychancecube" };

	public void onMessage(String channel, String sender, String login, String hostname, String message)
	{
		int j = this.keywords.length;
		for(int i = 0; i < j; i++)
		{
			String keyword = this.keywords[i];
			if((message.toLowerCase().contains(keyword)) /*
															 * && (!sender.equalsIgnoreCase(
															 * "turkey2349"))
															 */)
			{
				String out = "[" + channel + "] " + sender + ": " + message;
				chat.add(out);
				ChatMessageData newsData = new ChatMessageData(channel, out);
				NewsDispatcher.dispatch(newsData);
				return;
			}
		}
	}

	@Override
	public void connectToChannel(Integer channelID)
	{
		super.connectToChannel(channelID);
		String out = "Connected to " + super.getChannelNameFromID(channelID) + "'s channel!";
		ServerCore.output(Level.Info, "TurkeyBot", out);
		JsonObject dataToAdd = new JsonObject();
		dataToAdd.addProperty("destination", "MobileApp");
		dataToAdd.addProperty("purpose", "TurkeyBot");
		JsonObject TBData = new JsonObject();
		TBData.addProperty("action", "ChannelConnect");
		TBData.addProperty("channel", super.getChannelNameFromID(channelID));
		dataToAdd.add("data", TBData);

		// ServerCore.sendFCMMessage(dataToAdd.toString());
	}

	@Override
	public void disconnectFromChannel(Integer channel)
	{
		super.disconnectFromChannel(channel);
		String out = "Disconnected from " + super.getChannelNameFromID(channel) + "'s channel!";
		ServerCore.output(Level.Info, "TurkeyBot", out);

		JsonObject dataToAdd = new JsonObject();
		dataToAdd.addProperty("destination", "MobileApp");
		dataToAdd.addProperty("purpose", "TurkeyBot");
		JsonObject TBData = new JsonObject();
		TBData.addProperty("action", "ChannelDisconnected");
		TBData.addProperty("channel", super.getChannelNameFromID(channel));
		dataToAdd.add("data", TBData);
		// ServerCore.sendFCMMessage(dataToAdd.toString());
	}

	public String capitalizeName(String name)
	{
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	public void addWatchedChannel(Integer channelID, boolean save)
	{
		super.addWatchedChannel(channelID);
		if(save)
			DatabaseCore.addChannel(channelID);
	}

	public void removeWatchedChannel(Integer channelID, boolean save)
	{
		super.removeWatchedChannel(channelID);
		if(save)
			DatabaseCore.removeChannel(channelID);
	}

	public static JsonArray getLastXMessages(int x)
	{
		if(x > chat.size())
			x = chat.size();
		JsonArray messages = new JsonArray();
		for(int i = 1; i <= x; i++)
		{
			int index = chat.size() - i;
			if(index >= 0 && index < chat.size())
				messages.add(chat.get(index));
		}
		return messages;
	}

	public String getServiceID()
	{
		return "turkeyBot";
	}

	public String getServiceName()
	{
		return "TurkeyBot - Lurk";
	}

	public void init()
	{
		try
		{
			Properties properties = new Properties();
			File file = new File(ServiceManager.getConfigFolder().getPath() + "/turkeybot-lurk.prop");
			if(!file.exists())
			{
				file.createNewFile();
				try
				{
					properties.setProperty("Username", "");
					properties.setProperty("OAuthKey", "");
					properties.setProperty("ClientID", "");
					properties.setProperty("DBURL", "");
					properties.setProperty("DBUser", "");
					properties.setProperty("DBPassword", "");
					properties.store(new FileOutputStream(file), "");
				} catch(Exception e)
				{
				}
				ServerCore.output(Level.Severe, "Turkeybot", "Credentials file generated. Please enter information in file and restart the plugin.");
				return;
			}
			FileInputStream iStream = new FileInputStream(file);
			properties.load(iStream);
			botName = properties.getProperty("Username");
			oAuth = properties.getProperty("OAuthKey");
			clientID = properties.getProperty("ClientID");
			dbURL = properties.getProperty("DBURL");
			dbUser = properties.getProperty("DBUser");
			dbPassword = properties.getProperty("DBPassword");
		} catch(Exception e)
		{
			ServerCore.output(Level.Severe, "Turkeybot", "An error occured while trying to access the bots credentials! " + e.getMessage());
			e.printStackTrace();
		}
		bot = this;
		setVerbose(false);
		json = new JsonParser();
		DatabaseCore.initDB(dbURL, dbUser, dbPassword);
		ConsoleCommands.initCommands();
		setMessageDelay(500L);
		for(Integer channelID : DatabaseCore.getChannels())
			this.addWatchedChannel(channelID, false);
		super.connectToTwitch();
	}

	public void stop()
	{
		super.disconnectFromTwitch();
	}

	private static class ChatMessageData implements INewsData
	{
		private String channel;
		private String message;

		public ChatMessageData(String channel, String message)
		{
			this.channel = channel;
			this.message = message;
		}

		public String getData()
		{
			return message;
		}

		public String getDesc()
		{
			return channel;
		}

		public String getServiceID()
		{
			return "turkeyBot";
		}

		public String getTitle()
		{
			return "Chat Message Keyword";
		}

		public boolean hasNotification()
		{
			return true;
		}

	}

	@Override
	public void logInfo(String msg)
	{
		ServerCore.output(Level.Info, "Turkeybot", msg);
	}

	@Override
	public void logError(String msg)
	{
		ServerCore.output(Level.Error, "Turkeybot", msg);
	}
}
