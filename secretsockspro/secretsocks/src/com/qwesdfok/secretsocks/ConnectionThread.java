package com.qwesdfok.secretsocks;

import com.qwesdfok.common.*;
import com.qwesdfok.pretend.ListenerInterface;
import com.qwesdfok.pretend.PolicyManager;
import com.qwesdfok.pretend.PretendListener;
import com.qwesdfok.utils.Log;
import com.qwesdfok.utils.QUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ConnectionThread extends Thread
{
	private static final int buffer_size = 1024;
	private static int thrad_count = 0;

	private class ReceiveDataThread extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				byte[] buffer = new byte[buffer_size];
				int length = -1;
				length = outInputStream.read(buffer);
				while (length != -1)
				{
					callBeforeWriteListener(buffer, 0, length);
					inCipherStream.write(buffer, 0, length);
					inCipherStream.flush();
					try
					{
						length = outInputStream.read(buffer);
					} catch (SocketException e)
					{
						break;
					}
				}
			} catch (Exception e)
			{
				QUtils.printException(e);
			}
		}
	}

	private class UDPReceiveDataThread extends Thread
	{
		private DatagramSocket datagramSocket;

		public UDPReceiveDataThread(DatagramSocket datagramSocket)
		{
			this.datagramSocket = datagramSocket;
		}

		@Override
		public void run()
		{
			try
			{
				byte[] buffer = new byte[buffer_size];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				while (!datagramSocket.isClosed())
				{
					datagramSocket.receive(packet);
					callBeforeWriteListener(packet.getData(), packet.getOffset(), packet.getLength());
					inCipherStream.write(packet.getData(), packet.getOffset(), packet.getLength());
					inCipherStream.flush();
				}
			} catch (Exception e)
			{
				QUtils.printException(e);
			}
		}
	}

	private Socket inSocket;
	private ReceiveDataThread receiveDataThread;
	private UDPReceiveDataThread udpReceiveDataThread;
	private CipherByteStreamInterface inCipherStream;
	private BufferedInputStream outInputStream;
	private BufferedOutputStream outOutputStream;
	private PolicyManager policyManager;
	private List<PretendListener> pretendListenerList;
	private final int threadId;


	public ConnectionThread(CipherByteStreamInterface cipherByteStream, List<PretendListener> pretendListeners, PolicyManager policyManager)
	{
		super("ServerThread_" + thrad_count);
		threadId = thrad_count;
		thrad_count++;
		this.inSocket = cipherByteStream.getSocket();
		this.inCipherStream = cipherByteStream;
		this.policyManager = policyManager;
		this.pretendListenerList = pretendListeners;
	}

	@Override
	public void run()
	{
		try
		{
			//协商验证方式
			byte[] data = inCipherStream.read();
			callBeforeContactListener(data, 0, data.length);
			if (data.length < 3 || data[0] != 5)
				throw new SocksException("所接受的请求不是Socks5请求");
			int nMethods = data[1];
			List<Byte> methods = new ArrayList<>(2 + nMethods);
			for (int i = 2; i < 2 + nMethods; i++)
				methods.add(data[i]);
			//支持0x00的用户验证
			if (!methods.contains((byte) 0))
				throw new SocksException("服务器不支持验证方式");
			inCipherStream.write(new byte[]{0x05, 0x00});
			inCipherStream.flush();

			//接收解析请求
			data = inCipherStream.read();
			callBeforeResolveListener(data, 0, data.length);
			if (data.length < 6 || data[0] != 5)
				throw new SocksException("所接受的请求不是Socks5请求");
			int cmd = data[1];
			int atyp = data[3];
			InetAddress addr = null;
			if (atyp == 1)
				addr = InetAddress.getByAddress(new byte[]{data[4], data[5], data[6], data[7]});
			else if (atyp == 3)
				addr = InetAddress.getByName(new String(data, 5, data[4]));
			else if (atyp == 4)
				addr = InetAddress.getByAddress(new byte[]{data[4], data[5], data[6], data[7], data[8], data[9], data[10], data[11]
						, data[12], data[13], data[14], data[15], data[16], data[17], data[18], data[19]});
			else
				throw new SocksException("不支持的ATYP字段");
			int port = (Byte.toUnsignedInt(data[data.length - 2]) << 8) + Byte.toUnsignedInt(data[data.length - 1]);
			Log.infoLog("" + atyp + " " + addr + ":" + port);
			//解析请求类型
			if (cmd == 0x1)
			{
				//CONNECT请求
				Socket outSocket;
				try
				{
					outSocket = new Socket(addr, port);
				} catch (ConnectException e)
				{
					if (e.getLocalizedMessage().contains("refused"))
					{
						inCipherStream.write(generateReplyData(addr.getAddress(), (byte) 0x05, port));
						inCipherStream.flush();
						Log.infoLog(e.getLocalizedMessage());
					} else if (e.getLocalizedMessage().contains("time out"))
					{
						inCipherStream.write(generateReplyData(addr.getAddress(), (byte) 0x03, port));
						inCipherStream.flush();
						Log.infoLog(e.getLocalizedMessage());
					}
					inCipherStream.close();
					return;
				}
				inCipherStream.write(generateReplyData(outSocket.getLocalAddress().getAddress(), (byte) 0x00, outSocket.getLocalPort()));
				inCipherStream.flush();

				//与外界建立连接
				outInputStream = new BufferedInputStream(outSocket.getInputStream());
				outOutputStream = new BufferedOutputStream(outSocket.getOutputStream());
				receiveDataThread = new ReceiveDataThread();
				receiveDataThread.start();
				data = inCipherStream.read();
				callAfterFirstReadListener(data, 0, data.length);
				while (data != null)
				{
					outOutputStream.write(data, 0, data.length);
					outOutputStream.flush();
					data = inCipherStream.read();
					if (data != null)
						callAfterReadListener(data, 0, data.length);
				}
				outInputStream.close();
				outOutputStream.close();
				inCipherStream.close();
			} else if (cmd == 0x2)
			{
				//BIND请求
				ServerSocket serverSocket = new ServerSocket(0);
				inCipherStream.write(generateReplyData(serverSocket.getInetAddress().getAddress(), (byte) 0x00, serverSocket.getLocalPort()));
				inCipherStream.flush();
				//建立外界连接
				Socket outSocket;
				try
				{
					outSocket = serverSocket.accept();
				} catch (ConnectException e)
				{
					if (e.getLocalizedMessage().contains("refused"))
					{
						inCipherStream.write(generateReplyData(serverSocket.getInetAddress().getAddress(), (byte) 0x05, serverSocket.getLocalPort()));
						inCipherStream.flush();
						Log.infoLog(e.getLocalizedMessage());
					} else if (e.getLocalizedMessage().contains("time out"))
					{
						inCipherStream.write(generateReplyData(serverSocket.getInetAddress().getAddress(), (byte) 0x03, serverSocket.getLocalPort()));
						inCipherStream.flush();
						Log.infoLog(e.getLocalizedMessage());
					}
					inCipherStream.close();
					return;
				}
				inCipherStream.write(generateReplyData(outSocket.getInetAddress().getAddress(), (byte) 0x00, outSocket.getPort()));
				outInputStream = new BufferedInputStream(outSocket.getInputStream());
				outOutputStream = new BufferedOutputStream(outSocket.getOutputStream());
				receiveDataThread = new ReceiveDataThread();
				receiveDataThread.start();
				data = inCipherStream.read();
				callAfterFirstReadListener(data, 0, data.length);
				while (data != null)
				{
					outOutputStream.write(data, 0, data.length);
					outOutputStream.flush();
					data = inCipherStream.read();
					if (data != null)
						callAfterReadListener(data, 0, data.length);
				}
				inCipherStream.close();
				outInputStream.close();
				outOutputStream.close();
			} else if (cmd == 0x3)
			{
				//UDP请求
				DatagramSocket datagramSocket = new DatagramSocket(0);
				inCipherStream.write(generateReplyData(datagramSocket.getLocalAddress().getAddress(), (byte) 0x00, datagramSocket.getPort()));
				inCipherStream.flush();
				udpReceiveDataThread = new UDPReceiveDataThread(datagramSocket);
				udpReceiveDataThread.start();
				data = inCipherStream.read();
				while (data != null)
				{
					DatagramPacket packet = new DatagramPacket(data, 0, data.length, addr, port);
					datagramSocket.send(packet);
					data = inCipherStream.read();
					if (data != null)
						callAfterReadListener(data, 0, data.length);
				}
				inCipherStream.close();
				datagramSocket.close();
			} else
				throw new SocksException("不支持的连接请求类型");
		} catch (Exception e)
		{
			QUtils.printException(e);
		}
	}

	private byte[] generateReplyData(byte[] address, byte rep, int port)
	{
		byte[] portByte = new byte[]{(byte) ((port & 0xff00) >>> 8), (byte) ((port & 0xff))};
		if (address.length == 4)
		{
			return new byte[]{0x05, rep, 0x00, 0x01, address[0], address[1], address[2], address[3], portByte[0], portByte[1]};
		} else if (address.length == 16)
		{
			return new byte[]{0x05, rep, 0x00, 0x04, address[0], address[1], address[2], address[3], address[4],
					address[5], address[6], address[7], address[8], address[9], address[10], address[11], address[12], address[13],
					address[14], address[15], portByte[0], portByte[1]};
		} else
			return new byte[]{0x05, rep, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	}

	private void callBeforeContactListener(byte[] data, int offset, int length)
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.beforeContact(data, offset, length))
				listener.pretendServer.pretend(inSocket, ListenerInterface.TriggerType.BEFORE_CONTACT, policyManager);
		}
	}

	private void callBeforeResolveListener(byte[] data, int offset, int length)
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.beforeResolve(data, offset, length))
				listener.pretendServer.pretend(inSocket, ListenerInterface.TriggerType.BEFORE_RESOLVE, policyManager);
		}
	}

	private void callAfterFirstReadListener(byte[] data, int offset, int length)
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.afterFirstRead(data, offset, length))
				listener.pretendServer.pretend(inSocket, ListenerInterface.TriggerType.AFTER_FIRST_READ, policyManager);
		}
	}

	private void callBeforeWriteListener(byte[] data, int offset, int length)
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.beforeWrite(data, offset, length))
				listener.pretendServer.pretend(inSocket, ListenerInterface.TriggerType.BEFORE_WRITE, policyManager);
		}
	}

	private void callAfterReadListener(byte[] data, int offset, int length)
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.afterRead(data, offset, length))
				listener.pretendServer.pretend(inSocket, ListenerInterface.TriggerType.AFTER_READ, policyManager);
		}
	}
}
