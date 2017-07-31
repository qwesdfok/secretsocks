package com.qwesdfok.clientmain;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaderSAX2Factory;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigReader
{
	public static class ConfigServerData
	{
		public String comment = "";
		public String address = "";
		public int port = 2020;
		public String readKey = "secretsocks";
		public String writeKey = "secretsocks";
		public String blockCipherType = "AES-128";
		public String byteCipherType = "XOR";
		public int localPort = 2010;
		public boolean autoEnabled = false;
		public int bufferSize = 128 * 1024;
		public boolean enabled = false;

		public void copy(ConfigServerData data)
		{
			comment = data.comment;
			address = data.address;
			port = data.port;
			localPort = data.localPort;
			readKey = data.readKey;
			writeKey = data.writeKey;
			blockCipherType = data.blockCipherType;
			byteCipherType = data.byteCipherType;
			bufferSize = data.bufferSize;
		}
	}

	private static final String defaultConfigFile = "<config>\n<servers>\n</servers>\n</config>";
	private String configPath;
	private Document document;

	public ConfigReader(String configPath) throws IOException
	{
		this.configPath = configPath;
		File file = new File(configPath);
		if (!file.exists())
		{
			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(defaultConfigFile.getBytes());
			outputStream.flush();
			outputStream.close();
		}
		try
		{
			document = new SAXBuilder(new XMLReaderSAX2Factory(false)).build(file);
		} catch (Exception e)
		{
			boolean isDeleted = file.delete();
			if (!isDeleted)
				throw new IOException("无法删除文件");
			FileOutputStream outputStream = new FileOutputStream(file);
			outputStream.write(defaultConfigFile.getBytes());
			outputStream.flush();
			outputStream.close();
			try
			{
				document = new SAXBuilder(new XMLReaderSAX2Factory(false)).build(file);
			} catch (JDOMException e1)
			{
				throw new IOException("无法读取配置文件");
			}
		}
	}

	public List<ConfigServerData> fetchServerData()
	{
		Element config = document.getRootElement();
		List<ConfigServerData> data = new ArrayList<>();
		if (config.getChild("servers") == null)
			return data;
		List<Element> serverList = config.getChild("servers").getChildren("server");
		for (Element element : serverList)
		{
			ConfigServerData serverData = new ConfigServerData();
			serverData.comment = element.getAttributeValue("comment");
			serverData.address = element.getAttributeValue("address");
			serverData.port = Integer.parseInt(element.getAttributeValue("port"));
			serverData.blockCipherType = element.getAttributeValue("blockCipherType");
			serverData.byteCipherType = element.getAttributeValue("byteCipherType");
			serverData.readKey = element.getAttributeValue("readKey");
			serverData.writeKey = element.getAttributeValue("writeKey");
			serverData.localPort = Integer.parseInt(element.getAttributeValue("localPort"));
			serverData.autoEnabled = Boolean.valueOf(element.getAttributeValue("autoEnabled"));
			serverData.bufferSize = Integer.parseInt(element.getAttributeValue("bufferSize"));
			data.add(serverData);
		}
		return data;
	}

	public void updateServerConfig(List<ConfigServerData> serverData)
	{
		Element config = document.getRootElement();
		if (config.getChild("servers") == null)
			config.addContent(new Element("servers"));
		Element servers = config.getChild("servers");
		servers.removeContent();
		for (ConfigServerData data : serverData)
		{
			Element server = new Element("server");
			server.setAttribute("comment", data.comment);
			server.setAttribute("address", data.address);
			server.setAttribute("port", Integer.toString(data.port));
			server.setAttribute("readKey", data.readKey);
			server.setAttribute("writeKey", data.writeKey);
			server.setAttribute("blockCipherType", data.blockCipherType);
			server.setAttribute("byteCipherType", data.byteCipherType);
			server.setAttribute("localPort", Integer.toString(data.localPort));
			server.setAttribute("autoEnabled", Boolean.toString(data.autoEnabled));
			server.setAttribute("bufferSize", Integer.toString(data.bufferSize));
			servers.addContent(server);
		}
		Format format = Format.getPrettyFormat();
		format.setEncoding("utf-8");
		format.setIndent("  ");
		XMLOutputter output = new XMLOutputter(format);
		try
		{
			output.output(document, new FileOutputStream(new File(configPath)));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
