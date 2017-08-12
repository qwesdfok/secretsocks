package com.qwesdfok.common;

public class CipherManager
{
	public static final String BLOCK_AES_128 = "AES-128";
	public static final String BYTE_XOR = "XOR";
	public static final String[] BLOCK_CIPHER_TYPE_LIST = new String[]{BLOCK_AES_128};
	public static final String[] BYTE_CIPHER_TYPE_LIST = new String[]{BYTE_XOR};

	public static String[] getSupportBlockCipherType()
	{
		return BLOCK_CIPHER_TYPE_LIST;
	}

	public static String[] getSupportByteCipherType()
	{
		return BYTE_CIPHER_TYPE_LIST;
	}

	public static boolean containsBlockCipherType(String blockCipher)
	{
		for (String block : BLOCK_CIPHER_TYPE_LIST)
		{
			if (block.equalsIgnoreCase(blockCipher))
				return true;
		}
		return false;
	}

	public static boolean containsByteCipherType(String byteCipher)
	{
		for (String byta : BYTE_CIPHER_TYPE_LIST)
		{
			if (byta.equalsIgnoreCase(byteCipher))
				return true;
		}
		return false;
	}

	public static BlockCipherInterface getBlockNewInstance(String blockCipherType, byte[] readKey, byte[] writeKey)
	{
		if (BLOCK_AES_128.equalsIgnoreCase(blockCipherType))
			return new AESBlock128Cipher(readKey, writeKey);
		return new AESBlock128Cipher(readKey, writeKey);
	}

	public static ByteCipherInterface getByteCipherNewInstance(String byteCipherType, byte[] readKey, byte[] writeKey)
	{
		if (BYTE_XOR.equalsIgnoreCase(byteCipherType))
			return new XORByteCipher(readKey, writeKey);
		return new XORByteCipher(readKey, writeKey);
	}
}
