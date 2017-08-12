package com.qwesdfok.pretend;

import java.util.Comparator;

/**
 * 该类通过priority来进行唯一性的标识。ipAddress为过滤的具体IP，不能用通配符、正则表达式。ipFilter为描述要被过滤IP的正则表达式。
 * 一旦一个IP被过滤，则会交给相应的pretendServer进行处理。
 * priority为优先级，-1时PolicyManager会自动排在队列最后。越低表示优先级越高。
 * enabled表示是否启用该伪装策略。
 * TTLBySecond表示策略生存的时间，按秒计算。
 * 注意：修改优先级请通过{@link PolicyManager#configPriority(String, int)}修改。
 */
public class PretendPolicy implements Comparable<PretendPolicy>
{
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
	public String name = "";
	public String[] ipAddress;
	public String[] ipFilter;
	public PretendServerInterface pretendServer;
	public boolean enabled = true;
	public int timeToLiveBySecond = -1;
	public int priority = -1;

	public PretendPolicy()
	{
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
		return prioriTyComparator.compare(this, o);
	}
}
