package com.qwesdfok.common;

public class CipherManager
{
	public static final String BLOCK_AES_128 = "AES-128";
	public static final String BYTE_XOR = "XOR";

	public static String[] getSupportBlockCipherType()
	{
		return new String[]{BLOCK_AES_128};
	}

	public static String[] getSupportByteCipherType()
	{
		return new String[]{BYTE_XOR};
	}

	public static BlockCipherInterface getBlockInstance(String blockCipherType, byte[] readKey, byte[] writeKey)
	{
		if (BLOCK_AES_128.equalsIgnoreCase(blockCipherType))
			return new AESBlock128Cipher(readKey, writeKey);
		return new AESBlock128Cipher(readKey, writeKey);
	}

	public static ByteCipherInterface getByteCipherInterface(String byteCipherType, byte[] readKey, byte[] writeKey)
	{
		if (BYTE_XOR.equalsIgnoreCase(byteCipherType))
			return new XORByteCipher(readKey, writeKey);
		return new XORByteCipher(readKey, writeKey);
	}
}
