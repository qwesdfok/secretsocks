package com.qwesdfok.utils;

import java.util.Calendar;

/**
 * Created by qwesd on 2015/11/20.
 */
public class Log
{
	private static int logSetting = 0xffffffff;
	public static final int TRACE_LOG = 0x1;
	public static final int DEBUG_LOG = 0x2;
	public static final int INFO_LOG = 0x4;
	public static final int WARNING_LOG = 0x8;
	public static final int ERROR_LOG = 0x10;
	public static final int OUTPUT_LOG = 0x20;
	public static final int TIME_LOG = 0x40;
	public static final int LABEL_LOG = 0x80;

	public static void simplyLog(String data)
	{
		if (!data.endsWith("\n"))
			data = data + "\n";
		writeToStdOut(data);
	}

	public static void outputLog(String output)
	{
		if ((logSetting & OUTPUT_LOG) != 0) wrapper(((logSetting & LABEL_LOG) != 0) ? "[OUTPUT] " + output : output);
	}

	public static void traceLog(String trace)
	{
		if ((logSetting & TRACE_LOG) != 0) wrapper(((logSetting & LABEL_LOG) != 0) ? "[TRACE] " + trace : trace);
	}

	public static void debugLog(String debug)
	{
		if ((logSetting & DEBUG_LOG) != 0) wrapper(((logSetting & LABEL_LOG) != 0) ? "[DEBUG] " + debug : debug);
	}

	public static void infoLog(String info)
	{
		if ((logSetting & INFO_LOG) != 0) wrapper(((logSetting & LABEL_LOG) != 0) ? "[INFO] " + info : info);
	}

	public static void warningLog(String warning)
	{
		if ((logSetting & WARNING_LOG) != 0)
			wrapper(((logSetting & LABEL_LOG) != 0) ? "[WARNING] " + warning : warning);
	}

	public static void errorLog(String error)
	{
		if ((logSetting & ERROR_LOG) != 0) wrapper(((logSetting & LABEL_LOG) != 0) ? "[ERROR] " + error : error);
	}

	private static void wrapper(String str)
	{
		Calendar calendar = Calendar.getInstance();
		if (!str.endsWith("\n"))
			str = str + "\n";
		String time = String.format("[%04d-%02d-%02d|%02d:%02d:%02d] %s",
				calendar.get(Calendar.YEAR), (calendar.get(Calendar.MONTH) + 1), calendar.get(Calendar.DAY_OF_MONTH),
				calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), str);
		if ((logSetting & TIME_LOG) != 0)
			writeToStdOut(time);
		else
			writeToStdOut(str);
	}

	private static void writeToStdOut(String str)
	{
		System.out.print(str);
	}

	public static void printError(Exception e)
	{
		Log.errorLog(e.getClass().getName() + e.getLocalizedMessage());
		e.printStackTrace();
		if (e.getCause() != null)
			Log.errorLog("Cause" + e.getCause().getClass().getName() + e.getCause().getLocalizedMessage());
	}

	public static void enableLog(int logIndex)
	{
		logSetting |= logIndex;
	}

	public static void disableLog(int logIndex)
	{
		logSetting &= ~logIndex;
	}
}