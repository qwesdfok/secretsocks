package com.qwesdfok.common;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

/**
 * <p>BlockCipherInterface的一种默认实现，采用AES-128加密算法，并且允许加密和解密的密钥不相同。</p>
 * <p>默认使用SUN公司（Oracle公司）提供的SHA1PRNG算法的SecureRandom进行密钥的生成</p>
 */
public class AESBlock128Cipher implements BlockCipherInterface
{
	private final byte[] decryptPassword;
	private final byte[] encryptPassword;
	private Cipher decryptCipher;
	private Cipher encryptCipher;

	public AESBlock128Cipher(byte[] password)
	{
		this(password, password);
	}

	public AESBlock128Cipher(byte[] decryptPassword, byte[] encryptPassword)
	{
		this.decryptPassword = decryptPassword;
		this.encryptPassword = encryptPassword;
	}

	public void init() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, NoSuchProviderException
	{
		KeyGenerator decryptKeyGenerator = KeyGenerator.getInstance("AES");
		KeyGenerator encryptKeyGenerator = KeyGenerator.getInstance("AES");
		SecureRandom encryptRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
		SecureRandom decryptRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
		encryptRandom.setSeed(encryptPassword);
		decryptRandom.setSeed(decryptPassword);
		encryptKeyGenerator.init(128, encryptRandom);
		decryptKeyGenerator.init(128, decryptRandom);
		SecretKeySpec decryptKeySpec = new SecretKeySpec(decryptKeyGenerator.generateKey().getEncoded(), "AES");
		SecretKeySpec encryptKeySpec = new SecretKeySpec(encryptKeyGenerator.generateKey().getEncoded(), "AES");
		decryptCipher = Cipher.getInstance("AES");
		encryptCipher = Cipher.getInstance("AES");
		decryptCipher.init(Cipher.DECRYPT_MODE, decryptKeySpec);
		encryptCipher.init(Cipher.ENCRYPT_MODE, encryptKeySpec);
	}

	@Override
	public byte[] decrypt(byte[] cipher, int offset, int length) throws BadPaddingException, IllegalBlockSizeException
	{
		return decryptCipher.doFinal(cipher, offset, length);
	}

	@Override
	public byte[] encrypt(byte[] plain, int offset, int length) throws BadPaddingException, IllegalBlockSizeException
	{
		return encryptCipher.doFinal(plain, offset, length);
	}
}
