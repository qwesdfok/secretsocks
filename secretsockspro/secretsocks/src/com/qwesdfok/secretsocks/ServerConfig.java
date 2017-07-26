package com.qwesdfok.secretsocks;

public class ServerConfig
{
	public String listenAddress;
	public int port;
	public int bufferSize;

	public ServerConfig()
	{
	}

	public ServerConfig(String listenAddress, int port, int bufferSize)
	{
		this.listenAddress = listenAddress;
		this.port = port;
		this.bufferSize = bufferSize;
	}
}
