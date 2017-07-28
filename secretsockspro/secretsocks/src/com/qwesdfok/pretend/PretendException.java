package com.qwesdfok.pretend;

public class PretendException extends Exception
{
	public PretendException()
	{
	}

	public PretendException(String message)
	{
		super(message);
	}

	public PretendException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
