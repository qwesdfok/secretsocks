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
	private volatile boolean webServerStarted = false;
	private final Object lock = new Object();

	@Prefix("/")
	static class PretendServer
	{
		@Get("/")
		public Payload get()
		{
			return new Payload("<h1>Welcome to ChatRoom<h1><div>Only invited users can access the ChatRoom</div><form action='login' method='post'><div><input type=text' name='userName'/></div><div><input type='password' name='password'/></div><div><input type='submit' value='Submit'/></div></form>");
		}

		@Post("/login")
		public Payload post(Context context, Query query)
		{
			return new Payload("<h1>Permission Denied<h1><div>Invalid userName(" + query.get("userName") + ") or password<div>");
		}
	}

	@Override
	public void pretend(Socket socket, EventListenerInterface.TriggerType triggerType, PolicyManager policyManager, byte[] triggerData, int offset, int length)
	{
		Log.infoLog("pretend http server");
		if (!webServerStarted)
		{
			synchronized (lock)
			{
				if (!webServerStarted)
				{
					WebServer webServer = new WebServer().configure(routes -> routes.add(new PretendServer()));
					webServer.start(9080);
					webServerStarted = true;
				}
			}
		}
		try
		{
			PretendPolicy policy = new PretendPolicy();
			policy.ipAddress = new String[]{socket.getInetAddress().getHostAddress()};
			policy.pretendServer = this;
			policyManager.putPolicy(policy);
			Socket outSocket = new Socket("localhost", 9080);
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
