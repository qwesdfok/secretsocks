package com.qwesdfok.pretend;

import com.qwesdfok.common.NoCipherForwardStream;
import com.qwesdfok.utils.Log;
import net.codestory.http.WebServer;

import java.io.IOException;
import java.net.Socket;

public class HttpPretendServer implements PretendServerInterface
{
	protected final int listenPort;
	protected WebServer webServer;

	public HttpPretendServer()
	{
		this(2080);
	}

	public HttpPretendServer(int listenPort)
	{
		this.listenPort = listenPort;
	}

	@Override
	public void startServer()
	{
		Log.infoLog("pretend http server");
		webServer = new WebServer().configure(routes -> routes.add(new DefaultWebPage()));
		webServer.start(listenPort);
	}

	@Override
	public void stopServer()
	{
		if (webServer != null)
		{
			webServer.stop();
		}
	}

	@Override
	public void pretend(Socket socket, EventListenerInterface.TriggerType triggerType, PolicyManager policyManager, byte[] triggerData, int offset, int length)
	{
		try
		{
			PretendPolicy policy = new PretendPolicy();
			policy.ipAddress = new String[]{socket.getInetAddress().getHostAddress()};
			policy.pretendServer = this;
			Socket outSocket = new Socket("localhost", listenPort);
			if (triggerData != null)
			{
				outSocket.getOutputStream().write(triggerData);
				outSocket.getOutputStream().flush();
			}
			NoCipherForwardStream forwardStream = new NoCipherForwardStream(socket, outSocket);
			forwardStream.startForward();
		} catch (Exception e)
		{
			Log.printException(e);
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
