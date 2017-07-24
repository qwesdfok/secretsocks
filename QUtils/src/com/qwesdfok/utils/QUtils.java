package com.qwesdfok.utils;

public class QUtils
{
	public static boolean totalTrace = true;

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

	public static void printException(Exception e)
	{
		if (totalTrace)
		{
			e.printStackTrace();
		} else
		{
			Log.errorLog(e.getClass().getSimpleName() + ":" + e.getLocalizedMessage());
			StackTraceElement element = e.getStackTrace()[0];
			Log.errorLog(element.getFileName() + "!" + element.getMethodName() + ":" + element.getLineNumber());
			if (e.getCause() != null)
				printException(e.getCause());
		}
	}

	private static void printException(Throwable e)
	{
		Log.errorLog(e.getClass().getSimpleName() + ":" + e.getLocalizedMessage());
		if (e.getStackTrace().length > 0)
		{
			StackTraceElement element = e.getStackTrace()[0];
			Log.errorLog(element.getFileName() + "!" + element.getMethodName() + ":" + element.getLineNumber());
		}
		if (e.getCause() != null)
			printException(e.getCause());
	}
}
