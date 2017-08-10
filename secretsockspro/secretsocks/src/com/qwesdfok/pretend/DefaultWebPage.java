package com.qwesdfok.pretend;

import net.codestory.http.Context;
import net.codestory.http.Query;
import net.codestory.http.annotations.Get;
import net.codestory.http.annotations.Post;
import net.codestory.http.annotations.Prefix;
import net.codestory.http.payload.Payload;

/**
 * 默认的Web服务的页面，有两个页面，/与/login。
 */
@Prefix("/")
public class DefaultWebPage
{
	@Get("/")
	public Payload get()
	{
		return new Payload("<head><meta charset='UTF-8'><title>Login</title></head><body><h1>Welcome to ChatRoom<h1><div>Only invited users can access the ChatRoom</div><form action='login' method='post'><div><input type=text' name='userName'/></div><div><input type='password' name='password'/></div><div><input type='submit' value='Submit'/></div></form></body>");
	}

	@Post("/login")
	public Payload post(Context context, Query query)
	{
		return new Payload("<head><meta charset='UTF-8'><title>Hello</title></head><body><h1>Permission Denied<h1><div>Invalid userName(" + query.get("userName") + ") or password<div></body>");
	}
}
