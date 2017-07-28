package com.qwesdfok.pretend;

public class HttpsListener extends EventListenerAdaptor
{
	@Override
	public boolean beforeContact(byte[] data, int offset, int length)
	{
		//https protocol
		byte contentType = data[0];
		byte majorVersion = data[1];
		byte minorVersion = data[2];
		int tslLength = (Byte.toUnsignedInt(data[3]) << 8) + Byte.toUnsignedInt(data[4]);
		if ((0x14 <= contentType && contentType <= 0x18) && majorVersion == 0x03
				&& (minorVersion == 0x00 || minorVersion == 0x01 || minorVersion == 0x02 || minorVersion == 0x03)
				&& tslLength == length - 5)
			return true;

		//ClientSocks5Connect
		return false;
	}
}
