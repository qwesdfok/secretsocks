package com.qwesdfok.common;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public class XORByteCipher implements ByteCipherInterface
{
	private byte[] decryptKey;
	private byte[] encryptKey;
	private SecureRandom encryptRandom;
	private SecureRandom decryptRandom;

	public XORByteCipher(byte[] key)
	{
		this(key, key);
	}

	public XORByteCipher(byte[] decryptKey, byte[] encryptKey)
	{
		this.decryptKey = decryptKey;
		this.encryptKey = encryptKey;
	}

	@Override
	public void init() throws Exception
	{
		encryptRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
		decryptRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
		encryptRandom.setSeed(encryptKey);
		decryptRandom.setSeed(decryptKey);
	}

	@Override
	public byte decrypt(byte cipher) throws GeneralSecurityException
	{
		return (byte) (cipher ^ ((byte) decryptRandom.nextInt()));
	}

	@Override
	public byte encrypt(byte plain) throws GeneralSecurityException
	{
		return (byte) (plain ^ ((byte) encryptRandom.nextInt()));
	}
}
