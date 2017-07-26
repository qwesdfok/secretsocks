package com.qwesdfok.secretsocks;

import com.qwesdfok.common.*;
import com.qwesdfok.pretend.PolicyManager;
import com.qwesdfok.pretend.PretendListener;

import java.net.InetAddress;
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
			KeyInfo keyInfo = new KeyInfo("AES-128", "XOR", "qwesdfok", "qwesdfok");
			ServerConfig serverConfig = new ServerConfig(null, 9999, 1024 * 1024);
			InetAddress address = null;
			if (serverConfig.listenAddress != null)
				address = InetAddress.getByName(serverConfig.listenAddress);
			ServerSocket serverSocket = new ServerSocket(serverConfig.port, 0, address);
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
				CipherByteStreamInterface inCipherStream = new CipherByteStream(inSocket,
						new AESBlock128Cipher(keyInfo.readKey.getBytes(), keyInfo.writeKey.getBytes()),
						new XORByteCipher(keyInfo.readKey.getBytes(), keyInfo.writeKey.getBytes()), serverConfig.bufferSize);
				ConnectionThread connectionThread = new ConnectionThread(inCipherStream, pretendListenerList, policyManager);
				connectionThread.start();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
