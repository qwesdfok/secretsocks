package com.qwesdfok.secretclient;

import com.qwesdfok.common.CipherByteStreamInterface;
import com.qwesdfok.utils.Log;
import com.qwesdfok.utils.QUtils;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;


public class PipeThread extends Thread
{
	private static final int buffer_size = 1024;
	private static AtomicInteger thread_count = new AtomicInteger(0);
	private static AtomicInteger receive_thread_count = new AtomicInteger(0);

	/**
	 * 用于接收Server发送的数据，并转发给App
	 */
	private class ReceiveDataThread extends Thread
	{

		public ReceiveDataThread()
		{
			super("ReceiveThread_" + receive_thread_count.getAndIncrement());
		}

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
						Log.infoLog(Thread.currentThread().getName() + " c<-s closed");
						break;
					}
					if (data == null)
						break;
					inOutputStream.write(data);
//					Log.infoLog("[" + threadId + "]<-c<-:" + QUtils.byteToHexStr(data, 0, data.length));
					inOutputStream.flush();
				}
			} catch (Exception e)
			{
				Log.printException(e);
			} finally
			{
				closeAll();
			}
		}
	}

	private Socket inSocket;
	private ReceiveDataThread receiveDataThread;
	private InputStream inInputStream;
	private OutputStream inOutputStream;
	private CipherByteStreamInterface outCipherStream;


	/**
	 * @param inSocket         App的Socket对象
	 * @param cipherByteStream 实现了某加密算法的IO流
	 */
	public PipeThread(Socket inSocket, CipherByteStreamInterface cipherByteStream)
	{
		super("clientThread_" + thread_count.getAndIncrement());
		this.inSocket = inSocket;
		this.outCipherStream = cipherByteStream;
	}

	@Override
	public void run()
	{
		try
		{
			inInputStream = new BufferedInputStream(inSocket.getInputStream());
			inOutputStream = new BufferedOutputStream(inSocket.getOutputStream());

			receiveDataThread = new ReceiveDataThread();
			receiveDataThread.start();

			//接收App的数据并转发给Server
			byte[] buffer = new byte[buffer_size];
			int length = inInputStream.read(buffer);
			while (length != -1)
			{
				//encrypt
				outCipherStream.write(buffer, 0, length);
				outCipherStream.flush();
//				Log.infoLog("[" + threadId + "]->c->:" + QUtils.byteToHexStr(buffer, 0, length));
				try
				{
					length = inInputStream.read(buffer);
				} catch (IOException e)
				{
					Log.infoLog(Thread.currentThread().getName() + " a->c closed");
					break;
				}
			}
		} catch (Exception e)
		{
			Log.printException(e);
		} finally
		{
			closeAll();
		}
	}

	private void closeAll()
	{
		if (inSocket != null && !inSocket.isClosed())
		{
			try
			{
				inSocket.close();
			} catch (IOException e)
			{
				//ignore
			}
		}
		if (outCipherStream != null && outCipherStream.getSocket() != null && !outCipherStream.getSocket().isClosed())
		{
			try
			{
				outCipherStream.close();
			} catch (IOException e)
			{
				//ignore
			}
		}
	}
}
