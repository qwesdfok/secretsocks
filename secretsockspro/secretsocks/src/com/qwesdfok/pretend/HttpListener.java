package com.qwesdfok.pretend;

import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpListener extends EventListenerAdaptor
{
	@Override
	public boolean beforeContact(byte[] data, int offset, int length)
	{
		try
		{
			//HTTP protocol
			String httpRequest = new String(data);
			Pattern pattern = Pattern.compile("(?:HEAD|GET|POST|PUT|DELETE|TRACE|CONNECT|OPTIONS) .* HTTP/.*");
			Matcher matcher = pattern.matcher(httpRequest);
			if (matcher.find())
				return true;

			//ClientSocks5Connection
			return false;
		} catch (Exception e)
		{
			return false;
		}
	}
}
