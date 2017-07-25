package com.qwesdfok.pretend;

import java.util.Comparator;

public class PretendPolicy implements Comparable<PretendPolicy>
{
	public String name = "";
	public String[] ipFilter;
	public PretendServerInterface pretendServer;
	public boolean enabled = true;
	public int timeToLiveBySecond = -1;
	public int priority = -1;

	public static Comparator<PretendPolicy> prioriTyComparator = new Comparator<PretendPolicy>()
	{
		@Override
		public int compare(PretendPolicy o1, PretendPolicy o2)
		{
			if (o1 == null || o2 == null)
				throw new NullPointerException();
			return Integer.compare(o1.priority, o2.priority);
		}
	};

	public PretendPolicy()
	{
	}

	public PretendPolicy(String name, String[] ipFilter, PretendServerInterface pretendServer, int timeToLiveBySecond, int priority)
	{
		this.name = name;
		this.ipFilter = ipFilter;
		this.pretendServer = pretendServer;
		this.timeToLiveBySecond = timeToLiveBySecond;
		this.priority = priority;
	}

	public PretendPolicy(String name, String[] ipFilter, PretendServerInterface pretendServer, int priority)
	{
		this.name = name;
		this.ipFilter = ipFilter;
		this.pretendServer = pretendServer;
		this.priority = priority;
	}

	public PretendPolicy(String name, String[] ipFilter, PretendServerInterface pretendServer)
	{

		this.name = name;
		this.ipFilter = ipFilter;
		this.pretendServer = pretendServer;
	}

	@Override
	public int compareTo(PretendPolicy o)
	{
		if (o == this)
			return 0;
		if (o == null)
			throw new NullPointerException();
		return this.name.compareTo(o.name);
	}
}
