package com.qwesdfok.common;

public class CipherResult
{
	public boolean needMore;
	public int usedLength;
	public byte[] data;

	public CipherResult()
	{
	}

	public CipherResult(boolean needMore, int usedLength, byte[] data)
	{
		this.needMore = needMore;
		this.usedLength = usedLength;
		this.data = data;
	}
}
