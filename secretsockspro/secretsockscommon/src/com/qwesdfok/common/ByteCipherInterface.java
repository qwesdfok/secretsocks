package com.qwesdfok.common;

import java.security.GeneralSecurityException;

public interface ByteCipherInterface extends Cloneable
{
	void init() throws Exception;

	byte decrypt(byte cipher) throws GeneralSecurityException;

	byte encrypt(byte plain) throws GeneralSecurityException;

	ByteCipherInterface clone();
}
