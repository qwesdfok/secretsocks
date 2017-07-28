package com.qwesdfok.pretend;

public class PretendListener
{
	public EventListenerInterface listener;
	public PretendServerInterface pretendServer;

	public PretendListener(EventListenerInterface listener, PretendServerInterface pretendServer)
	{
		this.listener = listener;
		this.pretendServer = pretendServer;
	}

	public PretendListener()
	{

	}
}
