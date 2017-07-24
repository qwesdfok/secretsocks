package com.qwesdfok.secretclient;

import com.qwesdfok.common.ConnectionInfo;
import com.qwesdfok.utils.QUtils;

import java.net.ServerSocket;
import java.net.Socket;

public class ClientService
{
	public static void main(String[] argv)
	{
		ConnectionInfo connectionInfo = new ConnectionInfo("127.0.0.1", 9999, "AES-128", "qwesdfok", "qwesdfok");
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.bufferSize = 1024*1024;
		try
		{
			ServerSocket serverSocket = new ServerSocket(8888);
			while (true)
			{
				Socket inSocket = serverSocket.accept();
				Socket outSocket = new Socket(connectionInfo.address, connectionInfo.port);
				PipeThread pipeThread = new PipeThread(inSocket, outSocket, connectionInfo, clientConfig);
				pipeThread.start();
			}
		} catch (Exception e)
		{
			QUtils.printException(e);
		}
	}
}
