package com.qwesdfok.secretclient;

public class ClientConfig
{
	public int localPort;
	public String remoteHost;
	public int remotePort;
	public int bufferSize;

	public ClientConfig()
	{
	}

	public ClientConfig(int localPort, String remoteHost, int remotePort, int bufferSize)
	{
		this.localPort = localPort;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.bufferSize = bufferSize;
	}
}
