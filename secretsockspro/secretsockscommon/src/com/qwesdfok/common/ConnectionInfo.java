package com.qwesdfok.common;

public class ConnectionInfo
{
	public String address;
	public int port;
	public String cipher;
	public String readKey;
	public String writeKey;

	public ConnectionInfo()
	{
	}

	public ConnectionInfo(String address, int port, String cipher, String readKey, String writeKey)
	{
		this.address = address;
		this.port = port;
		this.cipher = cipher;
		this.readKey = readKey;
		this.writeKey = writeKey;
	}
}
