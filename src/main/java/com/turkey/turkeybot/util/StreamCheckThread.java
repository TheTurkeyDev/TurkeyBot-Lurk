package com.turkey.turkeybot.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.turkey.turkeybot.TurkeyBot;

public class StreamCheckThread implements Runnable
{
	private int delay;
	private boolean run = false;
	private Thread thread;

	public StreamCheckThread(int delay)
	{
		this.delay = delay;
	}

	public void initCurrencyThread()
	{
		this.run = true;
		if((this.thread == null) || (!this.thread.isAlive()))
		{
			this.thread = new Thread(this);
			this.thread.start();
		}
	}

	public void run()
	{
		while(this.run)
		{
			checkStreams();
			try
			{
				synchronized(this)
				{
					wait(60000 * this.delay);
				}
			} catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		try
		{
			this.thread.interrupt();
			this.thread.join();
		} catch(InterruptedException localInterruptedException1)
		{
		}
	}

	private void checkStreams()
	{
		for(String stream : TurkeyBot.bot.getWatchedChannels())
		{
			String result = "";
			try
			{
				URL url = new URL("https://api.twitch.tv/kraken/streams/" + stream.substring(1));
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.addRequestProperty("Client-ID", "eetv1xsijh2ksbhb3l9kmmlcn6x4a7o");
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line = "";
				while((line = reader.readLine()) != null)
				{
					result = result + line;
				}
				reader.close();
			} catch(Exception e)
			{
				System.out.println("Failed to get api info for stream: " + stream);
			}
			if(!result.equalsIgnoreCase(""))
			{
				JsonElement jsonresp = TurkeyBot.json.parse(result);
				if(!(jsonresp.getAsJsonObject().get("stream") instanceof JsonNull))
				{
					if(!TurkeyBot.bot.isConnectedToChannel(stream))
					{
						TurkeyBot.bot.connectToChannel(stream);
					}
				}
				else if((TurkeyBot.bot.isConnectedToChannel(stream)) && (!stream.equalsIgnoreCase("#turkey2349")))
				{
					TurkeyBot.bot.disconnectFromChannel(stream);
				}
			}
		}
	}

	public void stopThread()
	{
		this.run = false;
	}

	public boolean isRunning()
	{
		return this.run;
	}
}
