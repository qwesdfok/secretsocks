package com.qwesdfok.pretend;

import java.net.InetAddress;

public interface EventListenerInterface
{
	enum TriggerType
	{
		POLICY_MANAGER, AFTER_CONNECT, BEFORE_CONTACT, BEFORE_RESOLVE, AFTER_RESOLVE, AFTER_FIRST_READ, AFTER_READ, BEFORE_WRITE
	}

	boolean afterConnect(InetAddress clientAddress);

	boolean beforeContact(byte[] data, int offset, int length);

	boolean beforeResolve(byte[] data, int offset, int length);

	boolean afterResolve(InetAddress requestAddress, byte cmd, byte version, byte resv);

	boolean afterFirstRead(byte[] data, int offset, int length);

	boolean afterRead(byte[] data, int offset, int length);

	boolean beforeWrite(byte[] data, int offset, int length);
}
