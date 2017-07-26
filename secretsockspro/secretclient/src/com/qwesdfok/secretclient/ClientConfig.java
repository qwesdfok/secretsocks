package com.qwesdfok.secretclient;

public class ClientConfig
{
	public String host;
	public int port;
	public int bufferSize;

	public ClientConfig()
	{
	}

	public ClientConfig(String host, int port, int bufferSize)
	{
		this.host = host;
		this.port = port;
		this.bufferSize = bufferSize;
	}
}
