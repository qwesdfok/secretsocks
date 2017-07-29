package com.qwesdfok.pretend;

import java.net.InetAddress;

/**
 * 可以让用户重写某一个监听器而不需要实现接口中的其他方法。
 */
public class EventListenerAdaptor implements EventListenerInterface
{
	@Override
	public boolean afterConnect(InetAddress clientAddress)
	{
		return false;
	}

	@Override
	public boolean afterResolve(InetAddress requestAddress, byte cmd, byte version, byte resv)
	{
		return version != 0x05 || resv != 0x00 || !(cmd == 0x01 || cmd == 0x02 || cmd == 0x03);
	}

	@Override
	public boolean beforeContact(byte[] data, int offset, int length)
	{
		return false;
	}

	@Override
	public boolean beforeResolve(byte[] data, int offset, int length)
	{
		return false;
	}

	@Override
	public boolean afterFirstRead(byte[] data, int offset, int length)
	{
		return false;
	}

	@Override
	public boolean afterRead(byte[] data, int offset, int length)
	{
		return false;
	}

	@Override
	public boolean beforeWrite(byte[] data, int offset, int length)
	{
		return false;
	}
}
