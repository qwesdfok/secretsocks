package com.qwesdfok.pretend;

/**
 * 该异常表示某个Client的Socket的伪装已经结束
 */
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
