package com.qwesdfok.common;

import java.security.GeneralSecurityException;

public class NoCipher implements BlockCipherInterface, ByteCipherInterface
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

	@Override
	public byte decrypt(byte cipher) throws GeneralSecurityException
	{
		return cipher;
	}

	@Override
	public byte encrypt(byte plain) throws GeneralSecurityException
	{
		return plain;
	}
}
