package com.qwesdfok.pretend;

import net.codestory.http.Context;
import net.codestory.http.Query;
import net.codestory.http.annotations.Get;
import net.codestory.http.annotations.Post;
import net.codestory.http.annotations.Prefix;
import net.codestory.http.payload.Payload;

@Prefix("/")
public class DefaultWebPage
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