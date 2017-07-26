package com.qwesdfok.common;

import java.security.GeneralSecurityException;

public interface BlockCipherInterface extends Cloneable
{
	void init() throws Exception;

	byte[]  decrypt(byte[] cipher, int offset, int length) throws GeneralSecurityException;

	byte[] encrypt(byte[] plain, int offset, int length) throws GeneralSecurityException;

	BlockCipherInterface clone();
}
