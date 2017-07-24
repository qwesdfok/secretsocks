package com.qwesdfok.common;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface CipherByteStreamInterface
{
	byte[] read() throws IOException, GeneralSecurityException;

	void write(byte[] data) throws IOException, GeneralSecurityException;

	void write(byte[] data, int offset, int length) throws IOException, GeneralSecurityException;

	void flush() throws IOException;

	void close() throws IOException;
}
