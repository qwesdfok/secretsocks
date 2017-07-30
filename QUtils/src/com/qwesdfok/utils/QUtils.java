package com.qwesdfok.utils;

public class QUtils
{
	public static String byteToHexStr(byte[] bytes, int offset, int length)
	{
		char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
		char str[] = new char[length * 2];
		int k = 0;
		for (int i = offset; i < length; i++)
		{
			byte byte0 = bytes[i];
			str[k++] = hexDigits[byte0 >>> 4 & 0xf];
			str[k++] = hexDigits[byte0 & 0xf];
		}
		return new String(str);
	}
}
