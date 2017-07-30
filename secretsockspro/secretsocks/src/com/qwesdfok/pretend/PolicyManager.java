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

	private class TimerCountThread extends Thread
	{
		private long last = 0;

		@Override
		public void run()
		{
			last = System.currentTimeMillis();
			while (true)
			{
				try
				{
					Thread.sleep(333);
					long current = System.currentTimeMillis();
					if (current - last >= 1000)
					{
						for (PretendPolicy policy : prioritySet)
						{
							if (policy.timeToLiveBySecond < 0)
								continue;
							policy.timeToLiveBySecond--;
							if (policy.timeToLiveBySecond == 0)
							{
								prioritySet.remove(policy);
								policyMap.remove(policy.name);
							}
						}
					}
				} catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
					return;
				}
			}
		}
	}

	private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private HashMap<String, PretendPolicy> policyMap = new HashMap<>();
	private TreeSet<PretendPolicy> prioritySet = new TreeSet<>(PretendPolicy.prioriTyComparator);
	private TimerCountThread timerCountThread;

	public PolicyManager()
	{
		timerCountThread = new TimerCountThread();
		timerCountThread.start();
	}

	/**
	 * 添加一个Policy，若优先级为负，则自动添加优先级，并位于最后。
	 * 若存在同名同优先级的policy，则删除原有的，若不存在同名同优先级的，则添加。
	 * 若存在同名不同优先级或者不同名同优先级，则抛出异常。
	 *
	 * @param policy
	 */
	public void putPolicy(PretendPolicy policy)
	{
		readWriteLock.writeLock().lock();
		try
		{
			if (policyMap.containsKey(policy.name) && !prioritySet.contains(policy) || !policyMap.containsKey(policy.name) && prioritySet.contains(policy))
				throw new RuntimeException("存在同名不同优先级或者不同名同优先级的policy");
			if (policy.priority < 0)
			{
				policy.priority = auto_priority;
				auto_priority += 10;
			}
			if (policy.timeToLiveBySecond == 0)
				policy.timeToLiveBySecond = 1;
			policyMap.put(policy.name, policy);
			if (prioritySet.contains(policy))
				prioritySet.remove(policy);
			prioritySet.add(policy);
			if (prioritySet.size() != policyMap.size())
				throw new RuntimeException("请使用PolicyManager.configPolicy()进行优先级的修改");
		} finally
		{
			readWriteLock.writeLock().unlock();
		}
	}

	/**
	 * 添加一个policy，并且若存在与之一样的名字的policy，则优先级原有的一样。
	 *
	 * @param policy
	 */
	public void putPolicyWithSamePriority(PretendPolicy policy)
	{
		readWriteLock.writeLock().lock();
		try
		{
			if (policyMap.containsKey(policy.name))
			{
				policy.priority = policyMap.get(policy.name).priority;
			}
			if (policy.priority < 0)
			{
				policy.priority = auto_priority;
				auto_priority += 10;
			}
			if (policy.timeToLiveBySecond == 0)
				policy.timeToLiveBySecond = 1;
			if (policyMap.get(policy.name) != null && prioritySet.contains(policyMap.get(policy.name)))
				prioritySet.remove(policyMap.get(policy.name));
			policyMap.put(policy.name, policy);
			prioritySet.add(policy);
			if (prioritySet.size() != policyMap.size())
				throw new RuntimeException("请使用PolicyManager.configPolicy()进行优先级的修改");
		} finally
		{
			readWriteLock.writeLock().unlock();
		}
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
		try
		{
			if (!policyMap.containsKey(policyName))
				return;
			prioritySet.remove(policyMap.get(policyName));
			policyMap.remove(policyName);
			if (policyMap.size() != prioritySet.size())
				throw new RuntimeException("请使用PolicyManager.configPolicy()进行优先级的修改");
		} finally
		{
			readWriteLock.writeLock().unlock();
		}
	}

	public boolean isEnabled(String policyName)
	{
		readWriteLock.readLock().lock();
		boolean res = policyMap.get(policyName) != null && policyMap.get(policyName).enabled;
		readWriteLock.readLock().unlock();
		return res;
	}

	/**
	 * 修改Policy的启用状态
	 *
	 * @param policyName
	 * @param enabled
	 */
	public void configStatus(String policyName, boolean enabled)
	{
		readWriteLock.writeLock().lock();
		if (policyMap.containsKey(policyName))
			policyMap.get(policyName).enabled = enabled;
		readWriteLock.writeLock().unlock();
	}

	/**
	 * 检查IP是否存在对应的Policy
	 *
	 * @param ip
	 * @return true表示该IP需要使用Policy进行伪装
	 */
	public boolean inPolicy(String ip)
	{
		return fetchPolicyByIp(ip) != null;
	}

	/**
	 * 对该Socket进行伪装
	 *
	 * @param socket
	 * @throws IOException
	 * @throws PretendException 表示伪装结束
	 */
	public void startServer(Socket socket) throws IOException, PretendException
	{
		String ip = socket.getInetAddress().getHostAddress();
		PretendPolicy policy = fetchPolicyByIp(ip);
		if (policy == null)
			return;
		policy.pretendServer.pretend(socket, EventListenerInterface.TriggerType.POLICY_MANAGER, this, null, 0, 0);
		throw new PretendException("Policy denied :" + socket.getRemoteSocketAddress());
	}

	/**
	 * 配置优先级
	 *
	 * @param policyName
	 * @param priority
	 */
	public void configPriority(String policyName, int priority)
	{
		readWriteLock.writeLock().lock();
		try
		{
			if (!policyMap.containsKey(policyName))
				return;
			PretendPolicy policy = policyMap.get(policyName);
			prioritySet.remove(policy);
			policy.priority = priority;
			prioritySet.add(policy);
		} finally
		{
			readWriteLock.writeLock().unlock();
		}
	}

	public void shutdown()
	{
		timerCountThread.interrupt();
	}

	private PretendPolicy fetchPolicyByIp(String ip)
	{
		readWriteLock.readLock().lock();
		try
		{
			Pattern pattern;
			for (PretendPolicy policy : prioritySet)
			{
				if (!policy.enabled)
					continue;
				if (policy.ipAddress != null && policy.ipAddress.length != 0)
				{
					for (String address : policy.ipAddress)
					{
						if (ip.equals(address))
							return policy;
					}
				}
				if (policy.ipFilter != null && policy.ipFilter.length != 0)
				{
					for (String filter : policy.ipFilter)
					{
						pattern = Pattern.compile(filter);
						Matcher matcher = pattern.matcher(ip);
						if (matcher.find())
							return policy;
					}
				}
			}
			return null;
		} finally
		{
			readWriteLock.readLock().unlock();
		}
	}
}
