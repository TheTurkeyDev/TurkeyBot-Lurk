package com.turkey.turkeybot.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.theprogrammingturkey.ggserver.ServerCore;
import com.theprogrammingturkey.ggserver.events.PacketRecievedEvent;
import com.turkey.turkeybot.TurkeyBot;

public class PacketManager extends PacketRecievedEvent
{
	public PacketManager()
	{
		super("turkeybot");
	}

	@Override
	public void onPacketRecievedEvent(JsonElement element)
	{
		JsonObject jsonMessage = element.getAsJsonObject();

		if(!jsonMessage.has("purpose"))
		{
			return;
		}
		String purpose = jsonMessage.get("purpose").getAsString();
		if(purpose.equalsIgnoreCase("ping"))
		{
			JsonObject toSend = new JsonObject();
			toSend.addProperty("purpose", "pong");
			ServerCore.sendFCMMessage(toSend.toString());
		}
		else if((purpose.equalsIgnoreCase("TurkeyBot")) && (jsonMessage.has("data")))
		{
			JsonObject data = jsonMessage.get("data").getAsJsonObject();
			if(data.has("action"))
			{
				String action = data.get("action").getAsString();
				if(action.equalsIgnoreCase("reconnect"))
				{
					TurkeyBot.bot.reconnectBot();
				}
				else if(action.equalsIgnoreCase("initialConnect"))
				{
					JsonObject toSend = new JsonObject();
					toSend.addProperty("purpose", "message");
					toSend.addProperty("destination", "MobileApp");
					JsonObject dataToAdd = new JsonObject();
					dataToAdd.addProperty("purpose", "TurkeyBot");
					JsonObject TBData = new JsonObject();
					TBData.addProperty("action", "Info");
					JsonArray channels = new JsonArray();
					for(String c : TurkeyBot.bot.getConnectChannels())
					{
						channels.add(c);
					}
					TBData.add("channels", channels);
					TBData.add("messages", TurkeyBot.getLastXMessages(10));
					dataToAdd.add("data", TBData);
					dataToAdd.addProperty("notification_title", "TurkeyBot");
					dataToAdd.addProperty("notification_body", "Initial connection data recieved");
					toSend.add("data", dataToAdd);
					ServerCore.sendFCMMessage(toSend.toString());
				}
			}
		}
	}
}
