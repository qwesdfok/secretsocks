package com.qwesdfok.common;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * <p>ByteCipherInterface的一种默认实现，采用按位异或的方式进行加密，并且允许加密和解密的密钥不相同。</p>
 * <p>默认使用SUN公司（Oracle公司）提供的SHA1PRNG算法的SecureRandom进行密钥的生成</p>
 */
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
