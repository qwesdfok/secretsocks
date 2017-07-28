package com.qwesdfok.common;

import java.io.IOException;

public class IgnoreCloseException extends IOException
{
	public IgnoreCloseException()
	{
	}

	public IgnoreCloseException(String message)
	{
		super(message);
	}
}
