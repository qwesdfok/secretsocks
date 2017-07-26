package com.qwesdfok.secretclient;

import com.qwesdfok.common.*;
import com.qwesdfok.utils.QUtils;

import java.net.ServerSocket;
import java.net.Socket;

public class ClientService
{
	public static void main(String[] argv)
	{
		KeyInfo keyInfo = new KeyInfo("AES-128", "XOR", "qwesdfok", "qwesdfok");
		ClientConfig clientConfig = new ClientConfig("127.0.0.1", 9999, 1024 * 1024);
		try
		{
			ServerSocket serverSocket = new ServerSocket(8888);
			while (true)
			{
				Socket inSocket = serverSocket.accept();
				Socket outSocket = new Socket(clientConfig.host, clientConfig.port);
				CipherByteStreamInterface outCipherStream = new CipherByteStream(outSocket,
						new AESBlock128Cipher(keyInfo.readKey.getBytes(), keyInfo.writeKey.getBytes()),
						new XORByteCipher(keyInfo.readKey.getBytes(), keyInfo.writeKey.getBytes()), clientConfig.bufferSize);
				PipeThread pipeThread = new PipeThread(inSocket, outCipherStream);
				pipeThread.start();
			}
		} catch (Exception e)
		{
			QUtils.printException(e);
		}
	}
}
