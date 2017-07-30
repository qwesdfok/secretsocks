package com.qwesdfok.main;


import com.qwesdfok.utils.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Created by qwesdfok on 2017/3/22.
 */
public class DaemonThread extends Thread
{
	static final String COMMAND_SEND = "command_send";
	static final String LISTENER_SEND = "listener_send";
	private final ServerThread serverThread = new ServerThread();
	private CommandReader.CommandResult result;
	private CountDownLatch latch = new CountDownLatch(1);

	public DaemonThread()
	{
		super("DaemonThread");
	}

	public void init(String[] argv)
	{
		this.result = CommandReader.startReader(argv);
	}

	@Override
	public void run()
	{
		try
		{
			Selector selector = Selector.open();
			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.socket().setReuseAddress(true);
			serverSocketChannel.bind(new InetSocketAddress("localhost", result.daemonPort));
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			latch.countDown();

			while (true)
			{
				int size = selector.select(100);
				try
				{
					Thread.sleep(100);
				} catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
				if (Thread.currentThread().isInterrupted())
				{
					Log.outputLog("正在停止Process线程");
					try
					{
						serverThread.interrupt();
						for (SelectionKey key : selector.keys())
						{
							key.cancel();
						}
						selector.close();
					} catch (IOException e)
					{
						Log.errorLog("Process线程停止失败");
					}
					Log.outputLog("等待Server服务关闭");
					try
					{
						serverThread.join();
					} catch (InterruptedException e)
					{
						Log.outputLog("Server服务等待超时");
					}
					Log.outputLog("服务已关闭");
					return;
				}
				if (size == 0)
					continue;
				iterKeys(selector);
			}
		} catch (IOException e)
		{
			Log.printException(e);
		}
	}

	public void waitForInit() throws InterruptedException
	{
		latch.await();
	}

	private void iterKeys(Selector selector) throws IOException
	{
		Set<SelectionKey> selectedKeys = selector.selectedKeys();
		Iterator<SelectionKey> iterator = selectedKeys.iterator();
		while (iterator.hasNext())
		{
			SelectionKey key = iterator.next();
			iterator.remove();
			if (key.isAcceptable())
			{
				ServerSocketChannel channel = ((ServerSocketChannel) key.channel());
				SocketChannel socket = channel.accept();
				socket.configureBlocking(false);
				socket.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, false);
			} else if (key.isReadable())
			{
				SocketChannel channel = ((SocketChannel) key.channel());
				if (!((Boolean) key.attachment()))
				{
					ByteBuffer buffer = ByteBuffer.allocate(32);
					int count = channel.read(buffer);
					if (!COMMAND_SEND.equals(new String(buffer.array(), 0, count)))
					{
						channel.close();
					} else
					{
						key.attach(true);
					}
				} else
				{
					String data = readFromChannel(channel);
					if (data == null)
						continue;
					dealCommand(CommandReader.startReader(data));
				}
			} else if (key.isWritable())
			{
				SocketChannel channel = ((SocketChannel) key.channel());
				if (((Boolean) key.attachment()))
				{
					channel.write(ByteBuffer.wrap(LISTENER_SEND.getBytes()));
					key.interestOps(SelectionKey.OP_READ);
				}
			}
		}
	}

	public static String readFromChannel(SocketChannel channel) throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocate(512);
		ArrayList<byte[]> bufferList = new ArrayList<>();
		int count = channel.read(buffer);
		if (count == 0)
			return "";
		if (count == -1)
		{
			channel.close();
			return null;
		}
		while (count == buffer.capacity())
		{
			bufferList.add(buffer.array().clone());
			buffer.clear();
			count = channel.read(buffer);
		}
		byte[] last = new byte[count];
		System.arraycopy(buffer.array(), 0, last, 0, count);
		bufferList.add(last);

		byte[] data = last;
		if (bufferList.size() > 1)
		{
			data = new byte[(bufferList.size() - 1) * buffer.capacity() + last.length];
			for (int i = 0; i < bufferList.size() - 1; i++)
			{
				System.arraycopy(bufferList.get(i), 0, data, i * buffer.capacity(), buffer.capacity());
			}
			System.arraycopy(last, 0, data, (bufferList.size() - 1) * buffer.capacity(), last.length);
		}
		return new String(data);
	}

	private void dealCommand(CommandReader.CommandResult result)
	{
		if (result.manipulate == null || "start".equals(result.manipulate))
		{
			if (ServerThread.isRunning())
			{
				Log.outputLog("Server已运行");
			} else
			{
				if (configServerEnv())
				{
					serverThread.start();
					Log.outputLog("Server服务已启动");
				} else
				{
					Log.errorLog("Server服务启动失败");
				}
			}
		} else if ("stop".equals(result.manipulate))
		{
			Thread.currentThread().interrupt();
		} else if ("restart".equals(result.manipulate))
		{
			if (configServerEnv())
			{
				serverThread.restart();
				Log.outputLog("Server服务已重新启动");
			}
		}
	}

	private boolean configServerEnv()
	{
		if (result.serverHost == null || result.serverPort < 0)
		{
			Log.errorLog("缺失参数Host和Port");
			return false;
		}
		serverThread.config(result);
		return true;
	}
}
