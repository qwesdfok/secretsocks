package com.qwesdfok.main;


import com.qwesdfok.common.CipherByteStream;
import com.qwesdfok.common.CipherByteStreamInterface;
import com.qwesdfok.common.CipherManager;
import com.qwesdfok.common.KeyInfo;
import com.qwesdfok.pretend.*;
import com.qwesdfok.secretsocks.ConnectionThread;
import com.qwesdfok.secretsocks.ServerConfig;
import com.qwesdfok.utils.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by qwesdfok on 2017/3/23.
 */
public class ServerThread extends Thread
{
	/**
	 * 用于清理已经结束的connection线程
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
					Thread.sleep(10 * 1000);
					synchronized (processThreadList)
					{
						processThreadList.removeIf(thread -> !thread.isAlive());
					}
				} catch (InterruptedException e)
				{
					break;
				}
			}
		}
	}

	private static AtomicBoolean running = new AtomicBoolean(false);
	private final ArrayList<ConnectionThread> processThreadList = new ArrayList<>();
	private PolicyManager policyManager = new PolicyManager();
	private ServerSocket serverSocket;
	private List<PretendListener> pretendListenerList;
	private KeyInfo keyInfo;
	private ServerConfig serverConfig;
	private CleanThread cleanThread;
	private String blockCipherType;
	private String byteCipherType;

	public ServerThread()
	{
		super("ServerThread");
		cleanThread = new CleanThread();
		cleanThread.start();
	}

	public void config(CommandReader.CommandResult result)
	{
		this.keyInfo = new KeyInfo(null, null, result.readKey, result.writeKey);
		this.serverConfig = new ServerConfig(result.serverHost, result.serverPort, result.bufferSize);
		if (serverConfig.listenAddress == null)
			serverConfig.listenAddress = "0.0.0.0";
		if (serverConfig.bufferSize <= 0)
			serverConfig.bufferSize = 1024 * 1024;
		this.pretendListenerList = new ArrayList<>();
		pretendListenerList.add(new PretendListener(new HttpListener(), new HttpPretendServer()));
		pretendListenerList.add(new PretendListener(new HttpsListener(), new HttpsPretendServer()));
		blockCipherType = result.blockCipherType;
		byteCipherType = result.byteCipherType;
	}

	@Override
	public void run()
	{
		try
		{
			boolean r = running.getAndSet(true);
			if (r)
			{
				Log.errorLog("已存在Daemon服务");
				return;
			}
			serverSocket = new ServerSocket(serverConfig.listenPort, 0, InetAddress.getByName(serverConfig.listenAddress));
			serverSocket.setSoTimeout(100);
			Socket inSocket = null;
			while (true)
			{
				try
				{
					inSocket = serverSocket.accept();
				} catch (SocketTimeoutException e)
				{
					//需要关闭Server线程
					if (Thread.currentThread().isInterrupted())
					{
						policyManager.shutdown();
						cleanThread.interrupt();
						cleanThread.join();
						//join之后只有该线程可能更新processThreadList，不需要同步
						Iterator<ConnectionThread> iterator = processThreadList.iterator();
						while (iterator.hasNext())
						{
							ConnectionThread thread = iterator.next();
							if (thread.isAlive())
							{
								thread.interrupt();
								thread.closeAll();
								iterator.remove();
							}
						}
						break;
					}
					continue;
				}
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
						CipherManager.getBlockInstance(blockCipherType, keyInfo.readKey.getBytes(), keyInfo.writeKey.getBytes()),
						CipherManager.getByteCipherInterface(byteCipherType, keyInfo.readKey.getBytes(), keyInfo.writeKey.getBytes()),
						serverConfig.bufferSize);
				ConnectionThread connectionThread = new ConnectionThread(inCipherStream, pretendListenerList, policyManager);
				synchronized (processThreadList)
				{
					processThreadList.add(connectionThread);
				}
				connectionThread.start();
			}

		} catch (Exception e)
		{
			Log.printException(e);
		}
	}

	public void restart()
	{
		if (serverSocket != null)
		{
			if (!serverSocket.isClosed())
			{
				try
				{
					serverSocket.close();
				} catch (Exception e)
				{
					//ignore
				}
			}
			try
			{
				Thread.sleep(1000);
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		}
		try
		{
			serverSocket = new ServerSocket(serverConfig.listenPort, 0, InetAddress.getByName(serverConfig.listenAddress));
		} catch (IOException e)
		{
			Log.printException(e);
		}
	}

	public static boolean isRunning()
	{
		return running.get();
	}
}
