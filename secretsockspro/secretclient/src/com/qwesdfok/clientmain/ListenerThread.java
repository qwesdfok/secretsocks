package com.qwesdfok.clientmain;

import com.qwesdfok.common.CipherByteStream;
import com.qwesdfok.common.CipherByteStreamInterface;
import com.qwesdfok.common.CipherManager;
import com.qwesdfok.common.KeyInfo;
import com.qwesdfok.secretclient.ClientConfig;
import com.qwesdfok.secretclient.PipeThread;
import com.qwesdfok.utils.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ListenerThread extends Thread
{
	/**
	 * 用于清理已经结束的pipe线程
	 */
	private class CleanThread extends Thread
	{
		public CleanThread()
		{
			super("CleanThread");
		}

		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					Thread.sleep(11 * 1000);
					synchronized (pipeThreadList)
					{
						pipeThreadList.removeIf(thread -> !thread.isAlive());
					}
				} catch (InterruptedException e)
				{
					break;
				}
			}
		}
	}

	private static AtomicInteger thread_count = new AtomicInteger(0);
	private ServerSocket serverSocket;
	private final ArrayList<PipeThread> pipeThreadList = new ArrayList<>();
	private KeyInfo keyInfo;
	private ClientConfig clientConfig;
	private CleanThread cleanThread;

	public ListenerThread(KeyInfo keyInfo, ClientConfig clientConfig)
	{
		super("ListenerThread_" + thread_count.getAndIncrement());
		this.keyInfo = keyInfo;
		this.clientConfig = clientConfig;
		cleanThread = new CleanThread();
		cleanThread.start();
	}

	@Override
	public void run()
	{
		try
		{
			serverSocket = new ServerSocket(clientConfig.localPort);
			serverSocket.setSoTimeout(100);
			while (true)
			{
				Socket inSocket = null;
				try
				{
					inSocket = serverSocket.accept();
				} catch (SocketTimeoutException e)
				{
					if (Thread.interrupted())
					{
						// /结束该Listener线程
						cleanThread.interrupt();
						try
						{
							serverSocket.close();
						} catch (IOException e1)
						{
							//ignore
						}
						try
						{
							cleanThread.join(100);
						} catch (InterruptedException e1)
						{
							//ignore
						}
						//join之后只有该线程可能更新processThreadList，不需要同步
						for (PipeThread thread : pipeThreadList)
						{
							if (thread.isAlive())
							{
								thread.interrupt();
								thread.closeAll();
							}
						}
						pipeThreadList.clear();
						break;
					}
					continue;
				}
				Socket outSocket = new Socket(clientConfig.remoteHost, clientConfig.remotePort);
				CipherByteStreamInterface outCipherStream = new CipherByteStream(outSocket,
						CipherManager.getBlockNewInstance(keyInfo.blockCipher, keyInfo.readKey.getBytes(), keyInfo.writeKey.getBytes()),
						CipherManager.getByteCipherNewInstance(keyInfo.byteCipher, keyInfo.readKey.getBytes(), keyInfo.writeKey.getBytes()),
						clientConfig.bufferSize);
				PipeThread pipeThread = new PipeThread(inSocket, outCipherStream);
				synchronized (pipeThreadList)
				{
					pipeThreadList.add(pipeThread);
				}
				pipeThread.start();
			}
		} catch (Exception e)
		{
			Log.printException(e);
		} finally
		{
			if (cleanThread.isAlive())
				cleanThread.interrupt();
			if(serverSocket!=null&&!serverSocket.isClosed())
			{
				try
				{
					serverSocket.close();
				} catch (IOException e)
				{
					//ignore
				}
			}
		}
	}

	public boolean equalsWithServerConfig(String address,int remotePort,int localPort)
	{
		return clientConfig.remoteHost.equals(address) && clientConfig.remotePort ==remotePort && clientConfig.localPort == localPort;
	}

	public KeyInfo getKeyInfo()
	{
		return keyInfo;
	}

	public ClientConfig getClientConfig()
	{
		return clientConfig;
	}
}
