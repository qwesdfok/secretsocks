package com.qwesdfok.pretend;

public class PretendListener
{
	public ListenerInterface listener;
	public PretendServerInterface pretendServer;

	public PretendListener(ListenerInterface listener, PretendServerInterface pretendServer)
	{
		this.listener = listener;
		this.pretendServer = pretendServer;
	}

	public PretendListener()
	{

	}
}
