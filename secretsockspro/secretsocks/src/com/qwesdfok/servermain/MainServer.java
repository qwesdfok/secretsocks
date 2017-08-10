package com.qwesdfok.servermain;


import com.qwesdfok.utils.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by qwesdfok on 2017/3/2.
 * 2010 Client listener port
 * 2020 Server listener port
 * 2030 Daemon listener port
 * 2080 http pretend server
 * 2443 https pretend server
 */
public class MainServer
{
	public static void main(String[] argv)
	{
		try
		{
			Log.disableLog(Log.TIME_LOG);
			Log.disableLog(Log.LABEL_LOG);
			if (argv.length == 0)
			{
				Log.errorLog("请输入--help或-h来获取帮助");
				return;
			}
			CommandReader.CommandResult result = CommandReader.startReader(argv);
			Log.infoLog("host:" + result.serverHost);
			Log.infoLog("port:" + result.serverPort);
			Log.infoLog("daemonPort:" + result.daemonPort);
			Log.infoLog("readKey:" + result.readKey);
			Log.infoLog("writeKey:" + result.writeKey);
			Log.infoLog("blockCipher:" + result.blockCipherType);
			Log.infoLog("byteCipher:" + result.byteCipherType);
			Log.infoLog("bufferSize:" + result.bufferSize);
			try
			{
				sendCommand(result);
				Log.outputLog("已提交修改，结果请查询服务器日志");
				return;
			} catch (IOException e)
			{
				//启动服务
			}
			DaemonThread daemonThread = new DaemonThread();
			daemonThread.init(argv);
			daemonThread.start();
			try
			{
				daemonThread.waitForInit();
			} catch (InterruptedException e)
			{
				return;
			}
			try
			{
				sendCommand(result);
				Log.outputLog("已提交修改，结果请查询服务器日志");
			} catch (IOException e)
			{
				Log.errorLog("目标主机未响应请求，请查看地址和端口是否正确");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void sendCommand(CommandReader.CommandResult result) throws IOException
	{
		Socket socket = new Socket("localhost", result.daemonPort);
		InputStream inputStream = socket.getInputStream();
		OutputStream outputStream = socket.getOutputStream();
		try
		{
			outputStream.write(DaemonThread.COMMAND_SEND.getBytes());
			outputStream.flush();
			byte[] buffer = new byte[64];
			int size = inputStream.read(buffer);
			String response = new String(buffer, 0, size);
			if (!DaemonThread.LISTENER_SEND.equals(response))
				return;
			outputStream.write(result.toCommand().getBytes());
			outputStream.flush();
		} finally
		{
			try
			{
				socket.close();
			} catch (IOException e)
			{
				//ignore
			}
		}
	}
}
