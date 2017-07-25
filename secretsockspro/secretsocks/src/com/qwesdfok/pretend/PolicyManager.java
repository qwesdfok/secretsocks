package com.qwesdfok.pretend;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolicyManager
{
	private static int auto_priority = 0;
	private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private HashMap<String, PretendPolicy> policyMap = new HashMap<>();
	private TreeSet<PretendPolicy> prioritySet = new TreeSet<>(PretendPolicy.prioriTyComparator);

	public void putPolicy(PretendPolicy policy)
	{
		readWriteLock.writeLock().lock();
		if (policy.priority < 0)
		{
			policy.priority = auto_priority;
			auto_priority += 10;
		}
		policyMap.put(policy.name, policy);
		if (prioritySet.contains(policy))
			prioritySet.remove(policy);
		prioritySet.add(policy);
		readWriteLock.writeLock().unlock();
	}

	public void putPolicyWithSamePriority(PretendPolicy policy)
	{
		readWriteLock.writeLock().lock();
		if (policyMap.containsKey(policy.name))
		{
			policy.priority = policyMap.get(policy.name).priority;
		}
		if (policy.priority < 0)
		{
			policy.priority = auto_priority;
			auto_priority += 10;
		}
		policyMap.put(policy.name, policy);
		if (prioritySet.contains(policy))
			prioritySet.remove(policy);
		prioritySet.add(policy);
		readWriteLock.writeLock().unlock();
	}

	public boolean containsPolicy(String policyName)
	{
		readWriteLock.readLock().lock();
		boolean res = policyMap.containsKey(policyName);
		readWriteLock.readLock().unlock();
		return res;
	}

	public synchronized void deletePolicy(String policyName)
	{
		readWriteLock.writeLock().lock();
		policyMap.remove(policyName);
		readWriteLock.writeLock().unlock();
	}

	public boolean isEnabled(String policyName)
	{
		readWriteLock.readLock().lock();
		boolean res = policyMap.get(policyName) != null && policyMap.get(policyName).enabled;
		readWriteLock.readLock().unlock();
		return res;
	}

	public void configStatus(String policyName, boolean enabled)
	{
		readWriteLock.writeLock().lock();
		if (policyMap.containsKey(policyName))
			policyMap.get(policyName).enabled = enabled;
		readWriteLock.writeLock().unlock();
	}

	public boolean inPolicy(String ip)
	{
		return fetchPolicyByIp(ip) != null;
	}

	public void startServer(Socket socket) throws IOException
	{
		String ip = socket.getInetAddress().getHostAddress();
		PretendPolicy policy = fetchPolicyByIp(ip);
		if (policy == null)
			return;
		policy.pretendServer.pretend(socket, ListenerInterface.TriggerType.BEFORE_CONTACT, this);
	}

	public void configPriority(String policyName, int priority)
	{
		readWriteLock.writeLock().lock();
		try
		{
			if(!policyMap.containsKey(policyName))
				return;
			PretendPolicy policy=policyMap.get(policyName);
			prioritySet.remove(policy);
			policy.priority = priority;
			prioritySet.add(policy);
		}finally
		{
			readWriteLock.writeLock().unlock();
		}
	}

	private PretendPolicy fetchPolicyByIp(String ip)
	{
		readWriteLock.readLock();
		try
		{
			Pattern pattern;
			for (PretendPolicy policy : prioritySet)
			{
				if (policy.ipFilter == null || policy.ipFilter.length == 0 || !policy.enabled)
					continue;
				for (String filter : policy.ipFilter)
				{
					pattern = Pattern.compile(filter);
					Matcher matcher = pattern.matcher(ip);
					if (matcher.find())
						return policy;
				}
			}
			return null;
		} finally
		{
			readWriteLock.readLock().unlock();
		}
	}
}
