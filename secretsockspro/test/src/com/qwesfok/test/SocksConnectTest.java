package com.qwesfok.test;

import com.qwesdfok.pretend.PretendPolicy;
import net.codestory.http.Context;
import net.codestory.http.Query;
import net.codestory.http.WebServer;
import net.codestory.http.annotations.Get;
import net.codestory.http.annotations.Post;
import net.codestory.http.annotations.Prefix;
import net.codestory.http.payload.Payload;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.TreeSet;

public class SocksConnectTest
{
	@Test
	public void connect()
	{
		try
		{
			Socket socket = new Socket("127.0.0.1", 1180);
			BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
			BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());

			byte[] buffer = new byte[1024];
			int length = 0;
			outputStream.write(new byte[]{0x05, 0x01, 0x00});
			outputStream.flush();
			length = inputStream.read(buffer);
			outputStream.write(new byte[]{0x05, 0x01, 0x00, 0x01, (byte) 0x8A, (byte) 0xc5, (byte) 0x69, (byte) 0xdc, 0x27, 0x10});
			outputStream.flush();
			length = inputStream.read(buffer);
			inputStream.close();
			outputStream.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Test
	public void test()
	{
		TreeSet<PretendPolicy> treeSet = new TreeSet<>();
		PretendPolicy policy1 = new PretendPolicy();
		policy1.priority = 1;
		PretendPolicy policy2 = new PretendPolicy();
		policy2.priority = 2;
		treeSet.add(policy2);
		treeSet.add(policy1);
		treeSet.remove(policy1);
		for (PretendPolicy policy : treeSet)
		{
			System.out.println(policy.priority);
		}
	}

	@Test
	public void httpServerTest() throws InterruptedException
	{
		WebServer webServer = new WebServer().configure(routes -> routes.add(new PretendServer()));
		webServer.start(9090);
		while (true)
		{
			Thread.sleep(100);
		}
	}

	@Prefix("/")
	static class PretendServer
	{
		@Get("/")
		public Payload get()
		{
			return new Payload("<h1>Hello<h1><form action='login' method='post'><div><input type=text' name='userName'/></div><div><input type='password' name='password'/></div><div><input type='submit' value='Submit'/></div></form>");
		}

		@Post("/login")
		public Payload post(Context context, Query query)
		{
			return new Payload("<h1>Permission Denied<h1><div>Invalid userName(" + query.get("userName") + ") or password<div>");
		}
	}
}
