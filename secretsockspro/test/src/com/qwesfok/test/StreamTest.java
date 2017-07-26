package com.qwesfok.test;

import com.qwesdfok.common.AESBlock128Cipher;
import com.qwesdfok.common.CipherByteStream;
import com.qwesdfok.common.XORByteCipher;
import com.qwesdfok.utils.QUtils;
import org.testng.annotations.Test;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class StreamTest
{
	@Test
	public void client()
	{
		try
		{
			Socket socket = new Socket("127.0.0.1", 6666);
			CipherByteStream stream = new CipherByteStream(socket, new AESBlock128Cipher("qwesdfok".getBytes()), new XORByteCipher("qwesdfok".getBytes()), 1024);
			stream.write("1234567890123456789".getBytes());
			stream.write("1234567890123456789".getBytes());
			stream.flush();
			stream.close();
		} catch (Exception e)
		{
			QUtils.printException(e);
		}
	}

	@Test
	public void server()
	{
		try
		{
			ServerSocket serverSocket = new ServerSocket(6666,0, InetAddress.getByName("0.0.0.0"));
			Socket socket = serverSocket.accept();
			CipherByteStream stream = new CipherByteStream(socket, new AESBlock128Cipher("qwesdfok".getBytes()), new XORByteCipher("qwesdfok".getBytes()), 64);
			byte[] data = stream.read();
			while (data != null)
			{
				System.out.println(new String(data));
				data = stream.read();
			}
			stream.close();
		} catch (Exception e)
		{
			QUtils.printException(e);
		}
	}
}
