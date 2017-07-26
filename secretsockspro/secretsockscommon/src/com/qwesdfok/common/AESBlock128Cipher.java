package com.qwesdfok.common;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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

	public void init() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException
	{
		KeyGenerator decryptKeyGenerator = KeyGenerator.getInstance("AES");
		KeyGenerator encryptKeyGenerator = KeyGenerator.getInstance("AES");
		decryptKeyGenerator.init(128, new SecureRandom(decryptPassword));
		encryptKeyGenerator.init(128, new SecureRandom(encryptPassword));
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

	@Override
	public AESBlock128Cipher clone()
	{
		try
		{
			Object object = super.clone();
			AESBlock128Cipher cipher = ((AESBlock128Cipher) object);
			cipher.init();
			return cipher;
		} catch (CloneNotSupportedException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e)
		{
			return null;
		}
	}
}
