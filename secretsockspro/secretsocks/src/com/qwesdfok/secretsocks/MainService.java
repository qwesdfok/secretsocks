package com.qwesdfok.secretsocks;

import com.qwesdfok.common.*;
import com.qwesdfok.pretend.*;
import com.qwesdfok.utils.Log;

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
			ServerConfig serverConfig = new ServerConfig(null, 2020, 1024 * 1024);
			InetAddress address = null;
			if (serverConfig.listenAddress != null)
				address = InetAddress.getByName(serverConfig.listenAddress);
			ServerSocket serverSocket = new ServerSocket(serverConfig.listenPort, 0, address);
			PolicyManager policyManager = new PolicyManager();
			List<PretendListener> pretendListenerList = new ArrayList<>();
			pretendListenerList.add(new PretendListener(new HttpListener(), new HttpPretendServer()));
			pretendListenerList.add(new PretendListener(new HttpsListener(), new HttpsPretendServer()));
			while (true)
			{
				Socket inSocket = serverSocket.accept();
				if (policyManager.inPolicy(inSocket.getInetAddress().getHostAddress()))
				{
					try
					{
						policyManager.startServer(inSocket);
					} catch (PretendException e)
					{
						Log.infoLog(e.getLocalizedMessage());
					}
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
