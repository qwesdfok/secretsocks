package com.qwesdfok.common;

import com.qwesdfok.utils.Log;

import java.io.*;
import java.net.Socket;

public class NoCipherForwardStream
{
	private class ReceiveDataThread extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				byte[] buffer = new byte[bufferSize];
				int length = outInputStream.read(buffer);
				while (length != -1)
				{
					inOutputStream.write(buffer, 0, length);
					inOutputStream.flush();
//					Log.infoLog("Receive:" + new String(buffer, 0, length));
					try
					{
						length = outInputStream.read(buffer);
					} catch (Exception e)
					{
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
	}

	private static final int default_buffer_size = 64 * 1024;
	private Socket inSocket;
	private Socket outSocket;
	private ReceiveDataThread receiveDataThread;
	private InputStream inInputStream;
	private OutputStream inOutputStream;
	private InputStream outInputStream;
	private OutputStream outOutputStream;
	private final int bufferSize;

	public NoCipherForwardStream(Socket inSocket, Socket outSocket)
	{
		this(inSocket, outSocket, default_buffer_size);
	}

	public NoCipherForwardStream(Socket inSocket, Socket outSocket, int bufferSize)
	{
		if (bufferSize <= 0)
			bufferSize = default_buffer_size;
		this.inSocket = inSocket;
		this.outSocket = outSocket;
		this.bufferSize = bufferSize;
	}

	public void startForward() throws IOException
	{
		try
		{
			inInputStream = new BufferedInputStream(inSocket.getInputStream());
			inOutputStream = new BufferedOutputStream(inSocket.getOutputStream());
			outInputStream = new BufferedInputStream(outSocket.getInputStream());
			outOutputStream = new BufferedOutputStream(outSocket.getOutputStream());

			receiveDataThread = new ReceiveDataThread();
			receiveDataThread.start();
			byte[] buffer = new byte[bufferSize];
			int length = inInputStream.read(buffer);
			while (length != -1)
			{
				outOutputStream.write(buffer, 0, length);
				outOutputStream.flush();
//			Log.infoLog("Write:" + new String(buffer, 0, length));
				length = inInputStream.read(buffer);
			}
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
				Log.printException(e);
			}
		}
		if (outSocket != null && !outSocket.isClosed())
		{
			try
			{
				outSocket.close();
			} catch (IOException e)
			{
				Log.printException(e);
			}
		}
	}
}
