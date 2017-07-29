package com.qwesdfok.pretend;

/**
 * 用于整合数据
 */
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
