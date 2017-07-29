package com.qwesdfok.secretclient;

public class ClientConfig
{
	public int listenPort;
	public String remoteHost;
	public int remotePort;
	public int bufferSize;

	public ClientConfig()
	{
	}

	public ClientConfig(int listenPort, String remoteHost, int remotePort, int bufferSize)
	{
		this.listenPort = listenPort;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.bufferSize = bufferSize;
	}
}
