package com.qwesdfok.pretend;

public interface ListenerInterface
{
	enum TriggerType
	{
		BEFORE_CONTACT, BEFORE_RESOLVE, BEFORE_FIRST_TRANSMISSION, AFTER_READ, BEFORE_WRITE
	}

	boolean beforeContact(byte[] data);

	boolean beforeResolve(byte[] data);

	boolean beforeFirstTransmission(byte[] data);

	boolean afterRead(byte[] data);

	boolean beforeWrite(byte[] data);
}
