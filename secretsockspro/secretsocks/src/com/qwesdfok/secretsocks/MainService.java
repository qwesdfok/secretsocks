package com.qwesdfok.secretsocks;

import com.qwesdfok.common.ConnectionInfo;

import java.net.ServerSocket;
import java.net.Socket;

public class MainService
{
	public static void main(String[] argv)
	{
		try
		{
			ConnectionInfo connectionInfo = new ConnectionInfo("127.0.0.1", 9999, "AES-128", "qwesdfok", "qwesdfok");
			ServerConfig serverConfig = new ServerConfig();
			serverConfig.bufferSize = 1024*1024;
			ServerSocket serverSocket = new ServerSocket(9999);
			while (true)
			{
				Socket inSocket = serverSocket.accept();
				ConnectionThread connectionThread = new ConnectionThread(inSocket,connectionInfo,serverConfig);
				connectionThread.start();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
