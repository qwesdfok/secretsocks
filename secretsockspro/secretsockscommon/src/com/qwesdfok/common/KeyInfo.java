package com.qwesdfok.common;

public class KeyInfo
{
	public String blockCipher;
	public String byteCipher;
	public String readKey;
	public String writeKey;

	public KeyInfo()
	{
	}

	public KeyInfo(String blockCipher, String byteCipher, String readKey, String writeKey)
	{
		this.blockCipher = blockCipher;
		this.byteCipher = byteCipher;
		this.readKey = readKey;
		this.writeKey = writeKey;
	}
}
