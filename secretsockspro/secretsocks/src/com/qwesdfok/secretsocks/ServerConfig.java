package com.qwesdfok.secretsocks;

public class ServerConfig
{
	public String listenAddress;
	public int listenPort;
	public int bufferSize;

	public ServerConfig()
	{
	}

	public ServerConfig(String listenAddress, int listenPort, int bufferSize)
	{
		this.listenAddress = listenAddress;
		this.listenPort = listenPort;
		this.bufferSize = bufferSize;
	}
}
