package com.qwesdfok.common;

import com.qwesdfok.common.BlockCipherInterface;
import com.qwesdfok.common.CipherResult;

import java.security.NoSuchAlgorithmException;

public class NoCipher implements BlockCipherInterface
{
	@Override
	public void init() throws Exception
	{

	}

	@Override
	public byte[] decrypt(byte[] cipher, int offset, int length)
	{
		byte[] data = new byte[length];
		System.arraycopy(cipher, offset, data, 0, length);
		return data;
	}

	@Override
	public byte[] encrypt(byte[] plain, int offset, int length)
	{
		byte[] data = new byte[length];
		System.arraycopy(plain, offset, data, 0, length);
		return data;
	}
}
