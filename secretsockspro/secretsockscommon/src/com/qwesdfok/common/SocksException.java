package com.qwesdfok.common;

import java.io.IOException;

public class SocksException extends IOException
{
	public SocksException()
	{
	}

	public SocksException(String message)
	{
		super(message);
	}

	public SocksException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
