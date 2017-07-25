package com.qwesdfok.pretend;

public interface ListenerInterface
{
	enum TriggerType
	{
		BEFORE_CONTACT, BEFORE_RESOLVE, AFTER_FIRST_READ, AFTER_READ, BEFORE_WRITE
	}

	boolean beforeContact(byte[] data, int offset, int length);

	boolean beforeResolve(byte[] data, int offset, int length);

	boolean afterFirstRead(byte[] data, int offset, int length);

	boolean afterRead(byte[] data, int offset, int length);

	boolean beforeWrite(byte[] data, int offset, int length);
}
