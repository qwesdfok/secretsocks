package com.qwesdfok.pretend;

import com.qwesdfok.utils.Log;
import com.qwesdfok.utils.QUtils;
import net.codestory.http.WebServer;

import java.net.URL;
import java.nio.file.Paths;

public class HttpsPretendServer extends HttpPretendServer
{
	private final Object lock = new Object();

	public HttpsPretendServer()
	{
		super(2443);
	}

	public HttpsPretendServer(int listenPort)
	{
		super(listenPort);
	}

	@Override
	public void startServer()
	{
		Log.infoLog("pretend https server");
		if (!webServerStarted)
		{
			synchronized (lock)
			{
				if (!webServerStarted)
				{
					try
					{
						webServer = new WebServer().configure(routes -> routes.add(new DefaultWebPage()));
						URL publicCTR = this.getClass().getResource("/certificate/server.crt");
						URL privateCTR = this.getClass().getResource("/certificate/server.der");
						webServer.startSSL(listenPort, Paths.get(publicCTR.toURI()), Paths.get(privateCTR.toURI()));
						webServerStarted = true;
					} catch (Exception e)
					{
						QUtils.printException(e);
					}
				}
			}
		}
	}
}
