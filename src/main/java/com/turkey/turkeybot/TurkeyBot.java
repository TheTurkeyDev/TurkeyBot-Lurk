package com.turkey.turkeybot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jibble.pircbot.PircBot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.theprogrammingturkey.ggserver.ServerCore;
import com.theprogrammingturkey.ggserver.ServerCore.Level;
import com.theprogrammingturkey.ggserver.news.INewsData;
import com.theprogrammingturkey.ggserver.news.NewsDispatcher;
import com.theprogrammingturkey.ggserver.services.IServiceCore;
import com.theprogrammingturkey.ggserver.services.ServiceManager;
import com.turkey.turkeybot.commands.ConsoleCommands;
import com.turkey.turkeybot.files.JsonFile;
import com.turkey.turkeybot.util.StreamCheckThread;

public class TurkeyBot extends PircBot implements IServiceCore
{
	public static final String VERSION = "2.1";
	public static TurkeyBot bot;
	public static JsonParser json;
	private static List<String> chat = new ArrayList<String>();

	private boolean connected = false;
	private StreamCheckThread streamcheck;
	private List<String> connectChannels = new ArrayList<String>();
	private List<String> watchedChannels = new ArrayList<String>();
	private String[] keywords = { "turkey", "turkey2349", "chancecubes", "chance cubes", "chancecube", "chance cube", "turkeychancecube" };
	private JsonFile saveFile;

	public void onMessage(String channel, String sender, String login, String hostname, String message)
	{
		String[] arrayOfString;
		int j = (arrayOfString = this.keywords).length;
		for(int i = 0; i < j; i++)
		{
			String keyword = arrayOfString[i];
			if((message.toLowerCase().contains(keyword)) && (!sender.equalsIgnoreCase("turkey2349")))
			{
				String out = "[" + channel + "] " + sender + ": " + message;
				chat.add(out);
				ChatMessageData newsData = new ChatMessageData(channel, out);
				NewsDispatcher.dispatch(newsData);
				return;
			}
		}
	}

	public void onJoin(String channel, String sender, String login, String hostname)
	{
	}

	public void onPart(String channel, String sender, String login, String hostname)
	{
	}

	public void reconnectBot()
	{
		disconnect();
		ServerCore.output(Level.Alert, "TurkeyBot", "Reconnecting....");
		this.connected = false;
		this.streamcheck.stopThread();
		for(int i = 0; i < this.connectChannels.size(); i++)
		{
			String c = (String) this.connectChannels.get(i);
			disconnectFromChannel(c);
		}
		this.connectChannels.clear();
		connectToTwitch();
	}

	private boolean connectToTwitch()
	{
		ServerCore.output(Level.Info, "TurkeyBot", "Connecting to twitch....");
		setName(SecretStuff.botName);
		try
		{
			connect("irc.twitch.tv", 6667, SecretStuff.oAuth);
		} catch(Exception e)
		{
			if(!e.getMessage().equalsIgnoreCase("The PircBot is already connected to an IRC server.  Disconnect first."))
			{
				this.connected = false;
				ServerCore.output(Level.Error, "TurkeyBot", "Could not connect to Twitch! \n" + e.getMessage());
				return false;
			}
		}
		this.connected = true;
		ServerCore.output(Level.Info, "TurkeyBot", "Connected!");
		this.streamcheck = new StreamCheckThread(10);
		this.streamcheck.initCurrencyThread();
		connectToChannel("#turkey2349");
		return true;
	}

	public void connectToChannel(String channel)
	{
		if(!this.connected)
		{
			return;
		}
		if(!channel.startsWith("#"))
		{
			channel = "#" + channel;
		}
		if(this.connectChannels.contains(channel))
		{
			return;
		}
		channel = channel.toLowerCase();
		this.connectChannels.add(channel);
		joinChannel(channel);
		String out = "Connected to " + channel.substring(1) + "'s channel!";
		ServerCore.output(Level.Info, "TurkeyBot", out);
		JsonObject dataToAdd = new JsonObject();
		dataToAdd.addProperty("destination", "MobileApp");
		dataToAdd.addProperty("purpose", "TurkeyBot");
		JsonObject TBData = new JsonObject();
		TBData.addProperty("action", "ChannelConnect");
		TBData.addProperty("channel", channel.substring(1));
		dataToAdd.add("data", TBData);

		// ServerCore.sendFCMMessage(dataToAdd.toString());
	}

	public void disconnectFromChannel(String channel)
	{
		if(!channel.startsWith("#"))
		{
			channel = "#" + channel;
		}
		partChannel(channel);
		this.connectChannels.remove(channel);
		String out = "Disconnected from " + channel.substring(1) + "'s channel!";
		ServerCore.output(Level.Info, "TurkeyBot", out);

		JsonObject dataToAdd = new JsonObject();
		dataToAdd.addProperty("destination", "MobileApp");
		dataToAdd.addProperty("purpose", "TurkeyBot");
		JsonObject TBData = new JsonObject();
		TBData.addProperty("action", "ChannelDisconnected");
		TBData.addProperty("channel", channel.substring(1));
		dataToAdd.add("data", TBData);

		// ServerCore.sendFCMMessage(dataToAdd.toString());
	}

	public String capitalizeName(String name)
	{
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	public boolean didConnect()
	{
		return this.connected;
	}

	public void addWatchedChannel(String name, boolean save)
	{
		if(!name.startsWith("#"))
		{
			name = "#" + name;
		}
		name = name.toLowerCase();

		this.watchedChannels.add(name);
		if(save)
		{
			this.saveFile.addWatched(name);
		}
	}

	public void removeWatchedChannel(String name, boolean save)
	{
		if(!name.startsWith("#"))
		{
			name = "#" + name;
		}
		this.watchedChannels.remove(name);
		if(save)
		{
			this.saveFile.removeWatched(name);
		}
	}

	public List<String> getConnectChannels()
	{
		return this.connectChannels;
	}

	public List<String> getWatchedChannels()
	{
		return this.watchedChannels;
	}

	public boolean isConnectedToChannel(String name)
	{
		if(!name.startsWith("#"))
		{
			name = "#" + name;
		}
		return this.connectChannels.contains(name);
	}

	public List<String> getNonConnectedChannels()
	{
		List<String> toReturn = new ArrayList<String>();
		toReturn.addAll(this.watchedChannels);
		toReturn.removeAll(this.connectChannels);
		return toReturn;
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
		return "Turkey Bot - Lurk";
	}

	public void init()
	{
		try
		{
			Properties properties = new Properties();
			File file = new File(ServiceManager.getConfigFolder().getPath() + "/turkeybot-lurk/credentials.prop");
			if(!file.exists())
			{
				file.createNewFile();
				try
				{
					properties.setProperty("Username", "");
					properties.setProperty("OAuthKey", "");
					properties.store(new FileOutputStream(file), "");
				} catch(Exception e)
				{
				}
				ServerCore.output(Level.Severe, "Turkeybot", "Credentials file generated. Please enter information in file and restart the plugin.");
				return;
			}
			FileInputStream iStream = new FileInputStream(file);
			properties.load(iStream);
			SecretStuff.botName = properties.getProperty("Username");
			SecretStuff.oAuth = properties.getProperty("OAuthKey");
		} catch(Exception e)
		{
			ServerCore.output(Level.Severe, "Turkeybot", "An error occured while trying to access the bots credentials! " + e.getMessage());
			e.printStackTrace();
		}
		bot = this;
		setVerbose(false);
		json = new JsonParser();
		try
		{
			this.saveFile = new JsonFile();
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		ConsoleCommands.initCommands();
		setMessageDelay(500L);
		connectToTwitch();
	}

	public void stop()
	{
		if(this.connected)
		{
			disconnect();
			this.connected = false;
			this.streamcheck.stopThread();
			for(int i = 0; i < this.connectChannels.size(); i++)
			{
				String c = (String) this.connectChannels.get(i);
				disconnectFromChannel(c);
			}
			this.connectChannels.clear();
		}
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

	}
}
