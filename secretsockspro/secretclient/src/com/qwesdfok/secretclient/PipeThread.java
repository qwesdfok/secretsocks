package com.qwesdfok.secretclient;

import com.qwesdfok.common.AESBlock128Cipher;
import com.qwesdfok.common.CipherByteStream;
import com.qwesdfok.common.ConnectionInfo;
import com.qwesdfok.common.XORByteCipher;
import com.qwesdfok.utils.QUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;


public class PipeThread extends Thread
{
	private static final int buffer_size = 1024;
	private static int thread_count = 0;

	private class ReceiveDataThread extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				while (true)
				{
					byte[] data = null;
					try
					{
						data = outCipherStream.read();
					} catch (SocketException e)
					{
						break;
					}
					if (data == null)
						break;
					inOutputStream.write(data);
//					Log.infoLog(threadId + "<-c<-:" + QUtils.byteToHexStr(data, 0, data.length > 10 ? 10 : data.length));
					inOutputStream.flush();
				}
			} catch (Exception e)
			{
				QUtils.printException(e);
			}
		}
	}

	private Socket inSocket;
	private Socket outSocket;
	private ConnectionInfo connectionInfo;
	private ClientConfig clientConfig;
	private ReceiveDataThread receiveDataThread;
	private InputStream inInputStream;
	private OutputStream inOutputStream;
	private CipherByteStream outCipherStream;
	private final int threadId;
	private int phase = 0;


	public PipeThread(Socket inSocket, Socket outSocket, ConnectionInfo connectionInfo, ClientConfig clientConfig)
	{
		super("clientThread_" + thread_count);
		threadId = thread_count;
		thread_count++;
		this.inSocket = inSocket;
		this.outSocket = outSocket;
		this.connectionInfo = connectionInfo;
		this.clientConfig = clientConfig;
	}

	@Override
	public void run()
	{
		try
		{
			inInputStream = new BufferedInputStream(inSocket.getInputStream());
			inOutputStream = new BufferedOutputStream(inSocket.getOutputStream());
			outCipherStream = new CipherByteStream(outSocket,
					new AESBlock128Cipher(connectionInfo.readKey.getBytes(), connectionInfo.writeKey.getBytes()),
					new XORByteCipher(connectionInfo.readKey.getBytes(), connectionInfo.writeKey.getBytes()), clientConfig.bufferSize);
			receiveDataThread = new ReceiveDataThread();
			receiveDataThread.start();
			byte[] buffer = new byte[buffer_size];
			int length = inInputStream.read(buffer);
			while (length != -1)
			{
				//encrypt
				outCipherStream.write(buffer, 0, length);
				outCipherStream.flush();
//				Log.infoLog(threadId + "->c->:" + QUtils.byteToHexStr(buffer, 0, length));
				length = inInputStream.read(buffer);
			}
			inOutputStream.close();
			inInputStream.close();
			outCipherStream.close();
		} catch (Exception e)
		{
			QUtils.printException(e);
		}
	}
}
