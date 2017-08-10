package com.qwesdfok.pretend;

import com.qwesdfok.utils.Log;
import net.codestory.http.WebServer;

import java.net.URL;
import java.nio.file.Paths;

public class HttpsPretendServer extends HttpPretendServer
{
	public HttpsPretendServer()
	{
		super(2443);
	}

	@Override
	public void startServer()
	{
		Log.infoLog("pretend https server");
		try
		{
			webServer = new WebServer().configure(routes -> routes.add(new DefaultWebPage()));
			URL publicCTR = this.getClass().getResource("/certificate/server.crt");
			URL privateCTR = this.getClass().getResource("/certificate/server.der");
			webServer.startSSL(listenPort, Paths.get(publicCTR.toURI()), Paths.get(privateCTR.toURI()));
		} catch (Exception e)
		{
			Log.printException(e);
		}
	}
}
