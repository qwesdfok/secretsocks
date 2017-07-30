package com.qwesdfok.main;

import com.qwesdfok.common.*;
import com.qwesdfok.secretclient.ClientConfig;
import com.qwesdfok.secretclient.PipeThread;
import com.qwesdfok.utils.Log;

import java.net.ServerSocket;
import java.net.Socket;

public class MainClient
{
	public static void main(String[]argv)
	{
		KeyInfo keyInfo = new KeyInfo("AES-128", "XOR", "qwesdfok", "qwesdfok");
		ClientConfig clientConfig = new ClientConfig(2010, "127.0.0.1", 2020, 1024 * 1024);
		try
		{
			ServerSocket serverSocket = new ServerSocket(clientConfig.listenPort);
			while (true)
			{
				Socket inSocket = serverSocket.accept();
				Socket outSocket = new Socket(clientConfig.remoteHost, clientConfig.remotePort);
				CipherByteStreamInterface outCipherStream = new CipherByteStream(outSocket,
						new AESBlock128Cipher(keyInfo.readKey.getBytes(), keyInfo.writeKey.getBytes()),
						new XORByteCipher(keyInfo.readKey.getBytes(), keyInfo.writeKey.getBytes()), clientConfig.bufferSize);
				PipeThread pipeThread = new PipeThread(inSocket, outCipherStream);
				pipeThread.start();
			}
		} catch (Exception e)
		{
			Log.printException(e);
		}
	}
}
