package com.turkey.turkeybot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import com.theprogrammingturkey.twitchbot.base.TwitchBot;
import com.turkey.turkeybot.commands.ConsoleCommands;
import com.turkey.turkeybot.files.JsonFile;

public class TurkeyBot extends TwitchBot implements IServiceCore {

	public static final String VERSION = "1.13";
	public static TurkeyBot bot;
	public static JsonParser json;
	private static List<String> chat = new ArrayList<String>();

	private String[] keywords = { "turkey", "turkey2349", "chancecubes", "chance cubes", "chancecube", "chance cube",
			"turkeychancecube" };
	private JsonFile saveFile;

	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		int j = this.keywords.length;
		for (int i = 0; i < j; i++) {
			String keyword = this.keywords[i];
			if ((message.toLowerCase().contains(keyword)) /* && (!sender.equalsIgnoreCase("turkey2349")) */) {
				String out = "[" + channel + "] " + sender + ": " + message;
				chat.add(out);
				ChatMessageData newsData = new ChatMessageData(channel, out);
				NewsDispatcher.dispatch(newsData);
				return;
			}
		}
	}

	@Override
	public String connectToChannel(String channel) {
		channel = super.connectToChannel(channel);
		String out = "Connected to " + super.getChannelNameFromID(channel) + "'s channel!";
		ServerCore.output(Level.Info, "TurkeyBot", out);
		JsonObject dataToAdd = new JsonObject();
		dataToAdd.addProperty("destination", "MobileApp");
		dataToAdd.addProperty("purpose", "TurkeyBot");
		JsonObject TBData = new JsonObject();
		TBData.addProperty("action", "ChannelConnect");
		TBData.addProperty("channel", super.getChannelNameFromID(channel));
		dataToAdd.add("data", TBData);
		return channel;

		// ServerCore.sendFCMMessage(dataToAdd.toString());
	}

	@Override
	public String disconnectFromChannel(String channel) {
		channel = super.disconnectFromChannel(channel);
		String out = "Disconnected from " + super.getChannelNameFromID(channel) + "'s channel!";
		ServerCore.output(Level.Info, "TurkeyBot", out);

		JsonObject dataToAdd = new JsonObject();
		dataToAdd.addProperty("destination", "MobileApp");
		dataToAdd.addProperty("purpose", "TurkeyBot");
		JsonObject TBData = new JsonObject();
		TBData.addProperty("action", "ChannelDisconnected");
		TBData.addProperty("channel", super.getChannelNameFromID(channel));
		dataToAdd.add("data", TBData);
		return channel;

		// ServerCore.sendFCMMessage(dataToAdd.toString());
	}

	public String capitalizeName(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	public void addWatchedChannel(String name, boolean save) {
		String channel = super.addWatchedChannel(name);
		if (save) {
			this.saveFile.addWatched(channel);
		}
	}

	public void removeWatchedChannel(String name, boolean save) {
		String channel = super.removeWatchedChannel(name);
		if (save) {
			this.saveFile.removeWatched(channel);
		}
	}

	public static JsonArray getLastXMessages(int x) {
		if (x > chat.size())
			x = chat.size();
		JsonArray messages = new JsonArray();
		for (int i = 1; i <= x; i++) {
			int index = chat.size() - i;
			if (index >= 0 && index < chat.size())
				messages.add(chat.get(index));
		}
		return messages;
	}

	public String getServiceID() {
		return "turkeyBot";
	}

	public String getServiceName() {
		return "TurkeyBot - Lurk";
	}

	public void init() {
		try {
			Properties properties = new Properties();
			File file = new File(ServiceManager.getConfigFolder().getPath() + "/turkeybot-lurk/credentials.prop");
			if (!file.exists()) {
				file.createNewFile();
				try {
					properties.setProperty("Username", "");
					properties.setProperty("OAuthKey", "");
					properties.setProperty("ClientID", "");
					properties.store(new FileOutputStream(file), "");
				} catch (Exception e) {
				}
				ServerCore.output(Level.Severe, "Turkeybot",
						"Credentials file generated. Please enter information in file and restart the plugin.");
				return;
			}
			FileInputStream iStream = new FileInputStream(file);
			properties.load(iStream);
			botName = properties.getProperty("Username");
			oAuth = properties.getProperty("OAuthKey");
			clientID = properties.getProperty("ClientID");
		} catch (Exception e) {
			ServerCore.output(Level.Severe, "Turkeybot",
					"An error occured while trying to access the bots credentials! " + e.getMessage());
			e.printStackTrace();
		}
		bot = this;
		setVerbose(false);
		json = new JsonParser();
		try {
			this.saveFile = new JsonFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ConsoleCommands.initCommands();
		setMessageDelay(500L);
		super.connectToTwitch();
	}

	public void stop() {
		super.disconnectFromTwitch();
	}

	private static class ChatMessageData implements INewsData {
		private String channel;
		private String message;

		public ChatMessageData(String channel, String message) {
			this.channel = channel;
			this.message = message;
		}

		public String getData() {
			return message;
		}

		public String getDesc() {
			return channel;
		}

		public String getServiceID() {
			return "turkeyBot";
		}

		public String getTitle() {
			return "Chat Message Keyword";
		}

		public boolean hasNotification() {
			return true;
		}

	}

	@Override
	public void logInfo(String msg) {
		ServerCore.output(Level.Info, "Turkeybot", msg);
	}

	@Override
	public void logError(String msg) {
		ServerCore.output(Level.Error, "Turkeybot", msg);
	}
}
