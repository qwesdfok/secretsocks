package com.qwesdfok.secretsocks;

import com.qwesdfok.common.ConnectionInfo;
import com.qwesdfok.pretend.PolicyManager;
import com.qwesdfok.pretend.PretendListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainService
{
	public static void main(String[] argv)
	{
		try
		{
			ConnectionInfo connectionInfo = new ConnectionInfo("127.0.0.1", 9999, "AES-128", "qwesdfok", "qwesdfok");
			ServerConfig serverConfig = new ServerConfig();
			serverConfig.bufferSize = 1024 * 1024;
			ServerSocket serverSocket = new ServerSocket(9999);
			PolicyManager policyManager = new PolicyManager();
			List<PretendListener> pretendListenerList = new ArrayList<>();
			while (true)
			{
				Socket inSocket = serverSocket.accept();
				if (policyManager.inPolicy(inSocket.getInetAddress().getHostAddress()))
				{
					policyManager.startServer(inSocket);
					continue;
				}
				ConnectionThread connectionThread = new ConnectionThread(inSocket, connectionInfo, serverConfig, pretendListenerList, policyManager);
				connectionThread.start();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
