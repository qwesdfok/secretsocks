package com.qwesdfok.secretsocks;

import com.qwesdfok.common.*;
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
	private static int thread_id = 0;

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
	private CipherByteStream inCipherStream;
	private BufferedInputStream outInputStream;
	private BufferedOutputStream outOutputStream;
	private ConnectionInfo connectionInfo;
	private ServerConfig serverConfig;
	private final int id;


	public ConnectionThread(Socket inSocket, ConnectionInfo connectionInfo, ServerConfig serverConfig)
	{
		super("" + thread_id);
		id = thread_id;
		thread_id++;
		this.inSocket = inSocket;
		this.connectionInfo = connectionInfo;
		this.serverConfig = serverConfig;
	}

	@Override
	public void run()
	{
		try
		{
			inCipherStream = new CipherByteStream(inSocket,
					new AESBlock128Cipher(connectionInfo.readKey.getBytes(), connectionInfo.writeKey.getBytes()),
					new XORByteCipher(connectionInfo.readKey.getBytes(), connectionInfo.writeKey.getBytes()), serverConfig.bufferSize);

			byte[] data = inCipherStream.read();

			//协商验证方式
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

			//接收connect请求
			data = inCipherStream.read();
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
			//建立外界连接
			if (cmd == 0x1)
			{
				//CONNECT请求
				Socket outSocket = new Socket(addr, port);
				inCipherStream.write(generateReplyData(outSocket.getLocalAddress().getAddress(), outSocket.getLocalPort()));
				inCipherStream.flush();
				outInputStream = new BufferedInputStream(outSocket.getInputStream());
				outOutputStream = new BufferedOutputStream(outSocket.getOutputStream());
				receiveDataThread = new ReceiveDataThread();
				receiveDataThread.start();
				data = inCipherStream.read();
				while (data != null)
				{
					outOutputStream.write(data, 0, data.length);
					outOutputStream.flush();
					data = inCipherStream.read();
				}
				outInputStream.close();
				outOutputStream.close();
				inCipherStream.close();
			} else if (cmd == 0x2)
			{
				//BIND请求
				ServerSocket serverSocket = new ServerSocket(0);
				inCipherStream.write(generateReplyData(serverSocket.getInetAddress().getAddress(), serverSocket.getLocalPort()));
				inCipherStream.flush();
				Socket outSocket = serverSocket.accept();
				inCipherStream.write(generateReplyData(outSocket.getInetAddress().getAddress(), outSocket.getPort()));
				outInputStream = new BufferedInputStream(outSocket.getInputStream());
				outOutputStream = new BufferedOutputStream(outSocket.getOutputStream());
				receiveDataThread = new ReceiveDataThread();
				receiveDataThread.start();
				data = inCipherStream.read();
				while (data != null)
				{
					outOutputStream.write(data, 0, data.length);
					outOutputStream.flush();
					data = inCipherStream.read();
				}
				inCipherStream.close();
				outInputStream.close();
				outOutputStream.close();
			} else if (cmd == 0x3)
			{
				//UDP请求
				DatagramSocket datagramSocket = new DatagramSocket(0);
				inCipherStream.write(generateReplyData(datagramSocket.getLocalAddress().getAddress(), datagramSocket.getPort()));
				inCipherStream.flush();
				udpReceiveDataThread = new UDPReceiveDataThread(datagramSocket);
				udpReceiveDataThread.start();
				data = inCipherStream.read();
				while (data != null)
				{
					DatagramPacket packet = new DatagramPacket(data, 0, data.length, addr, port);
					datagramSocket.send(packet);
					data = inCipherStream.read();
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

	private byte[] generateReplyData(byte[] address, int port)
	{
		byte[] portByte = new byte[]{(byte) ((port & 0xff00) >>> 8), (byte) ((port & 0xff))};
		if (address.length == 4)
		{
			return new byte[]{0x05, 0x00, 0x00, 0x01, address[0], address[1], address[2], address[3], portByte[0], portByte[1]};
		} else if (address.length == 16)
		{
			return new byte[]{0x05, 0x00, 0x00, 0x04, address[0], address[1], address[2], address[3], address[4],
					address[5], address[6], address[7], address[8], address[9], address[10], address[11], address[12], address[13],
					address[14], address[15], portByte[0], portByte[1]};
		} else
			return new byte[]{0x05, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	}
}
