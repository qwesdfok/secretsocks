package com.qwesfok.test;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class InvalidConnectTest
{
	@Test
	public void socks5ConnectTest()
	{
		try
		{
			Socket socket = new Socket("127.0.0.1", 8888);
			BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
			BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());

			byte[] buffer = new byte[1024];
			int length = 0;
			outputStream.write(new byte[]{0x00, 0x01, 0x00});
			outputStream.flush();
			length = inputStream.read(buffer);
			System.out.println(new String(buffer, 0, length));
			inputStream.close();
			outputStream.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Test
	public void httpConnectTest()
	{
		try
		{
			HttpClient httpClient = HttpClients.createDefault();
			HttpGet get = new HttpGet("http://localhost:8888/");
			HttpResponse response = httpClient.execute(get);
			System.out.println(EntityUtils.toString(response.getEntity()));
			System.out.println("End");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Test
	public void socketConnectTest()
	{
		try
		{
			Socket socket = new Socket("localhost", 9999);
			String data = "GET / HTTP/1.1\r\n" +
					"Host: localhost:8888\r\n" +
					"Connection: keep-alive\r\n" +
					"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\n" +
					"Upgrade-Insecure-Requests: 1\r\n" +
					"User-Agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Maxthon/5.0.4.3000 Chrome/47.0.2526.73 Safari/537.36\r\n" +
					"DNT: 1\r\n" +
					"Accept-Encoding: gzip, deflate\r\n" +
					"Accept-Language: zh-CN\r\n" +
					"\r\n";
			InputStream inputStream = socket.getInputStream();
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(data.getBytes());
			outputStream.flush();
			byte[] buffer = new byte[1024];
			int length = inputStream.read(buffer);
			System.out.println(new String(buffer, 0, length));
			System.out.println("End");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
