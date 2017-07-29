package com.qwesdfok.pretend;

import com.qwesdfok.utils.Log;
import com.qwesdfok.utils.QUtils;
import net.codestory.http.Context;
import net.codestory.http.Query;
import net.codestory.http.WebServer;
import net.codestory.http.annotations.Get;
import net.codestory.http.annotations.Post;
import net.codestory.http.annotations.Prefix;
import net.codestory.http.payload.Payload;

import java.io.IOException;
import java.net.Socket;

public class HttpPretendServer implements PretendServerInterface
{
	protected final int listenPort;
	protected volatile boolean webServerStarted = false;
	protected WebServer webServer;
	private final Object lock = new Object();

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
		if (!webServerStarted)
		{
			synchronized (lock)
			{
				if (!webServerStarted)
				{
					webServer = new WebServer().configure(routes -> routes.add(new DefaultWebPage()));
					webServer.start(listenPort);
					webServerStarted = true;
				}
			}
		}
	}

	@Override
	public void stopServer()
	{
		if(webServer!=null)
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
			policyManager.putPolicy(policy);
			Socket outSocket = new Socket("localhost", listenPort);
			outSocket.getOutputStream().write(triggerData);
			outSocket.getOutputStream().flush();
			NoCipherForwardStream forwardStream = new NoCipherForwardStream(socket, outSocket);
			forwardStream.startForward();
		} catch (Exception e)
		{
			QUtils.printException(e);
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