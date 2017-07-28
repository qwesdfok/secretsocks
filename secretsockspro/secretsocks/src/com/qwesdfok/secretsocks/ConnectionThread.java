package com.qwesdfok.secretsocks;

import com.qwesdfok.common.CipherByteStreamInterface;
import com.qwesdfok.common.IgnoreCloseException;
import com.qwesdfok.common.SocksException;
import com.qwesdfok.pretend.EventListenerInterface;
import com.qwesdfok.pretend.PolicyManager;
import com.qwesdfok.pretend.PretendException;
import com.qwesdfok.pretend.PretendListener;
import com.qwesdfok.utils.Log;
import com.qwesdfok.utils.QUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class ConnectionThread extends Thread
{
	private static final int buffer_size = 1024;
	private static int thread_count = 0;
	private static int receive_thread_count = 0;

	private class ReceiveDataThread extends Thread
	{
		private final int receiveThreadId;

		public ReceiveDataThread()
		{
			super("ReceiveThread_" + receive_thread_count);
			receiveThreadId = receive_thread_count;
			receive_thread_count++;
		}

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
					readByteCount += length;
					try
					{
						length = outInputStream.read(buffer);
					} catch (SocketException e)
					{
						Log.infoLog("ReceiveThreadId:" + receiveThreadId + " s<-w closed");
						break;
					}
				}
			} catch (Exception e)
			{
				QUtils.printException(e);
			} finally
			{
				closeAll();
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
					readByteCount += packet.getLength();
					inCipherStream.write(packet.getData(), packet.getOffset(), packet.getLength());
					inCipherStream.flush();
				}
			} catch (Exception e)
			{
				QUtils.printException(e);
			} finally
			{
				closeAll();
			}
		}
	}

	private Socket inSocket;
	private ReceiveDataThread receiveDataThread;
	private UDPReceiveDataThread udpReceiveDataThread;
	private CipherByteStreamInterface inCipherStream;
	private Socket outSocket;
	private DatagramSocket datagramSocket;
	private BufferedInputStream outInputStream;
	private BufferedOutputStream outOutputStream;
	private PolicyManager policyManager;
	private List<PretendListener> pretendListenerList;
	private long readByteCount, writeByteCount;
	private final int threadId;


	public ConnectionThread(CipherByteStreamInterface cipherByteStream, List<PretendListener> pretendListeners, PolicyManager policyManager)
	{
		super("ServerThread_" + thread_count);
		threadId = thread_count;
		thread_count++;
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
			callAfterConnectListener(inSocket.getInetAddress());
			//协商验证方式
			byte[] look = inCipherStream.look();
			callBeforeContactListener(look, 0, look.length);
			byte[] data = inCipherStream.read();
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
			look = inCipherStream.look();
			callBeforeResolveListener(look, 0, look.length);
			data = inCipherStream.read();
			if (data.length < 6 || data[0] != 5)
				throw new SocksException("所接受的请求不是Socks5请求");
			byte version = data[0];
			byte cmd = data[1];
			byte resv = data[2];
			byte atyp = data[3];
			InetAddress addr = null;
			if (atyp == 0x01)
				addr = InetAddress.getByAddress(new byte[]{data[4], data[5], data[6], data[7]});
			else if (atyp == 0x03)
				addr = InetAddress.getByName(new String(data, 5, data[4]));
			else if (atyp == 0x04)
				addr = InetAddress.getByAddress(new byte[]{data[4], data[5], data[6], data[7], data[8], data[9], data[10], data[11]
						, data[12], data[13], data[14], data[15], data[16], data[17], data[18], data[19]});
			else
				throw new SocksException("不支持的ATYP字段");
			int port = (Byte.toUnsignedInt(data[data.length - 2]) << 8) + Byte.toUnsignedInt(data[data.length - 1]);
			Log.infoLog("ThreadId:" + threadId + " AType:" + atyp + " Host:" + addr + ":" + port);
			callAfterResolveListener(addr, cmd, version, resv, data, 0, data.length);
			//解析请求类型
			if (cmd == 0x1)
			{
				if (!resolveConnectRequest(addr, port))
					return;
			} else if (cmd == 0x2)
			{
				if (!resolveBindRequest())
					return;
			} else if (cmd == 0x3)
			{
				resolveUDPRequest(addr, port);
			} else
			{
				throw new SocksException("不支持的连接请求类型");
			}
			Log.infoLog(inSocket.getRemoteSocketAddress().toString() + " closed");
		} catch (IgnoreCloseException e)
		{
			Log.infoLog("ThreadId:" + threadId + " c->s closed");
		} catch (PretendException e)
		{
			Log.infoLog(e.getLocalizedMessage());
		} catch (Exception e)
		{
			Log.errorLog("ThreadId:" + threadId + " ReadByte: " + readByteCount + ", writeByte: " + writeByteCount);
			QUtils.printException(e);
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
				QUtils.printException(e);
			}
		}
		if (inCipherStream != null && !inCipherStream.isClosed())
		{
			try
			{
				inCipherStream.close();
			} catch (Exception e)
			{
				QUtils.printException(e);
			}
		}
		if (datagramSocket != null && !datagramSocket.isClosed())
		{
			datagramSocket.close();
		}
		if (outSocket != null && !outSocket.isClosed())
		{
			try
			{
				outSocket.close();
			} catch (Exception e)
			{
				QUtils.printException(e);
			}
		}
	}

	private void resolveUDPRequest(InetAddress addr, int port) throws IOException, GeneralSecurityException, PretendException
	{
		//UDP请求
		Log.warningLog("UDP Request");
		byte[] data, look;
		datagramSocket = new DatagramSocket(0);
		inCipherStream.write(generateReplyData(datagramSocket.getLocalAddress().getAddress(), (byte) 0x00, datagramSocket.getPort()));
		inCipherStream.flush();
		//转发外界数据
		udpReceiveDataThread = new UDPReceiveDataThread(datagramSocket);
		udpReceiveDataThread.start();
		//转发客户端数据
		look = inCipherStream.look();
		callAfterFirstReadListener(look, 0, look.length);
		data = inCipherStream.read();
		while (data != null)
		{
			DatagramPacket packet = new DatagramPacket(data, 0, data.length, addr, port);
			datagramSocket.send(packet);
			look = inCipherStream.look();
			callAfterReadListener(look, 0, look.length);
			data = inCipherStream.read();
		}
	}

	private boolean resolveBindRequest() throws IOException, GeneralSecurityException, PretendException
	{
		//BIND请求
		Log.warningLog("Bind Request");
		ServerSocket serverSocket = new ServerSocket(0);
		inCipherStream.write(generateReplyData(serverSocket.getInetAddress().getAddress(), (byte) 0x00, serverSocket.getLocalPort()));
		inCipherStream.flush();
		//等待外界连接的接入
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
			return false;
		}
		//通知客户端连接已经建立
		inCipherStream.write(generateReplyData(outSocket.getInetAddress().getAddress(), (byte) 0x00, outSocket.getPort()));
		inCipherStream.flush();
		startForwardData();
		return true;
	}

	private boolean resolveConnectRequest(InetAddress addr, int port) throws IOException, GeneralSecurityException, PretendException
	{
		//CONNECT请求
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
			return false;
		}
		inCipherStream.write(generateReplyData(outSocket.getLocalAddress().getAddress(), (byte) 0x00, outSocket.getLocalPort()));
		inCipherStream.flush();
		startForwardData();
		return true;
	}

	private void startForwardData() throws IOException, GeneralSecurityException, PretendException
	{
		//与外界建立连接
		outInputStream = new BufferedInputStream(outSocket.getInputStream());
		outOutputStream = new BufferedOutputStream(outSocket.getOutputStream());
		//转发外界数据
		Log.infoLog("ThreadId:" + threadId + " Start forward by ");
		receiveDataThread = new ReceiveDataThread();
		receiveDataThread.start();
		//转发客户端数据
		byte[] look = inCipherStream.look();
		if (look != null)
			callAfterFirstReadListener(look, 0, look.length);
		byte[] data = inCipherStream.read();
		while (data != null)
		{
			outOutputStream.write(data, 0, data.length);
			outOutputStream.flush();
			writeByteCount += data.length;
			look = inCipherStream.look();
			if (look != null)
				callAfterReadListener(look, 0, look.length);
			try
			{
				data = inCipherStream.read();
			} catch (IOException e)
			{
				throw new IgnoreCloseException();
			}
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

	private void callAfterConnectListener(InetAddress clientAddress) throws PretendException
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.afterConnect(clientAddress))
			{
				listener.pretendServer.pretend(inSocket, EventListenerInterface.TriggerType.AFTER_CONNECT, policyManager, null, 0, 0);
				throw new PretendException("Pretend after connect:" + clientAddress.toString());
			}
		}
	}

	private void callBeforeContactListener(byte[] data, int offset, int length) throws PretendException
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.beforeContact(data, offset, length))
			{
				listener.pretendServer.pretend(inSocket, EventListenerInterface.TriggerType.BEFORE_CONTACT, policyManager, data, offset, length);
				throw new PretendException("Pretend before contact:" + inSocket.getRemoteSocketAddress().toString());
			}
		}
	}

	private void callBeforeResolveListener(byte[] data, int offset, int length) throws PretendException
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.beforeResolve(data, offset, length))
			{
				listener.pretendServer.pretend(inSocket, EventListenerInterface.TriggerType.BEFORE_RESOLVE, policyManager, data, offset, length);
				throw new PretendException("Pretend before resolve:" + inSocket.getRemoteSocketAddress().toString());
			}
		}
	}

	private void callAfterResolveListener(InetAddress requestAddress, byte cmd, byte version, byte resv, byte[] data, int offset, int length) throws PretendException
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.afterResolve(requestAddress, cmd, version, resv))
			{
				listener.pretendServer.pretend(inSocket, EventListenerInterface.TriggerType.AFTER_RESOLVE, policyManager, data, offset, length);
				throw new PretendException("Pretend before resolve:" + requestAddress.toString());
			}
		}
	}

	private void callAfterFirstReadListener(byte[] data, int offset, int length) throws PretendException
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.afterFirstRead(data, offset, length))
			{
				listener.pretendServer.pretend(inSocket, EventListenerInterface.TriggerType.AFTER_FIRST_READ, policyManager, data, offset, length);
				throw new PretendException("Pretend after first read:" + inSocket.getRemoteSocketAddress().toString());
			}
		}
	}

	private void callBeforeWriteListener(byte[] data, int offset, int length) throws PretendException
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.beforeWrite(data, offset, length))
			{
				listener.pretendServer.pretend(inSocket, EventListenerInterface.TriggerType.BEFORE_WRITE, policyManager, data, offset, length);
				throw new PretendException("Pretend before write:" + inSocket.getRemoteSocketAddress().toString());
			}
		}
	}

	private void callAfterReadListener(byte[] data, int offset, int length) throws PretendException
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.afterRead(data, offset, length))
			{
				listener.pretendServer.pretend(inSocket, EventListenerInterface.TriggerType.AFTER_READ, policyManager, data, offset, length);
				throw new PretendException("Pretend after read:" + inSocket.getRemoteSocketAddress().toString());
			}
		}
	}
}
