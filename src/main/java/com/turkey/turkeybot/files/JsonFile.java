package com.turkey.turkeybot.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.theprogrammingturkey.ggserver.ServerCore;
import com.theprogrammingturkey.ggserver.ServerCore.Level;
import com.theprogrammingturkey.ggserver.services.ServiceManager;
import com.turkey.turkeybot.TurkeyBot;

public class JsonFile
{
	private File file;
	private JsonObject mainFile;

	public JsonFile() throws IOException
	{
		this.file = new File(ServiceManager.getConfigFolder().getPath() + "/turkeybot-lurk/Stuff.json");
		if(!this.file.exists())
		{
			this.file.getParentFile().mkdirs();
			this.file.createNewFile();
		}
		else
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.file)));
			String result = "";
			String line = "";
			while((line = reader.readLine()) != null)
			{
				result = result + line;
			}
			reader.close();
			this.mainFile = TurkeyBot.json.parse(result).getAsJsonObject();
			for(JsonElement s : this.mainFile.get("Channels").getAsJsonArray())
			{
				TurkeyBot.bot.addWatchedChannel(s.getAsString(), false);
			}
		}
	}

	public void addWatched(String channel)
	{
		this.mainFile.get("Channels").getAsJsonArray().add(new JsonPrimitive(channel));
		save();
	}

	public void removeWatched(String channel)
	{
		JsonArray watched = this.mainFile.get("Channels").getAsJsonArray();
		for(int i = 0; i < watched.size(); i++)
		{
			JsonElement e = watched.get(i);
			if(e.getAsString().equalsIgnoreCase(channel))
			{
				watched.remove(e);
				save();
				return;
			}
		}
	}

	public void save()
	{
		try
		{
			FileOutputStream outputStream = new FileOutputStream(this.file);
			OutputStreamWriter writer = new OutputStreamWriter(outputStream);
			writer.append(this.mainFile.toString());
			writer.close();
			outputStream.close();
		} catch(IOException ex)
		{
			ServerCore.output(Level.Error, "TurkeyBot", "Could not write to json file");
		}
	}
}
