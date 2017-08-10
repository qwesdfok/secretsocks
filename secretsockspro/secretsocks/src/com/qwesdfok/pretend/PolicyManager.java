package com.qwesdfok.pretend;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolicyManager
{
	static AtomicInteger listener_count = new AtomicInteger(0);

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

	private int auto_priority = 0;
	private ReentrantReadWriteLock policyRWLock = new ReentrantReadWriteLock();
	private ReentrantReadWriteLock listenerRWLock = new ReentrantReadWriteLock();
	private HashMap<String, PretendPolicy> policyMap = new HashMap<>();
	private TreeSet<PretendPolicy> prioritySet = new TreeSet<>(PretendPolicy.prioriTyComparator);
	private TimerCountThread timerCountThread;
	private ArrayList<PretendListener> pretendListenerList = new ArrayList<>();
	private ConcurrentHashMap<PretendServerInterface, AtomicBoolean> serverInitMap = new ConcurrentHashMap<>();

	public PolicyManager()
	{
		timerCountThread = new TimerCountThread();
		timerCountThread.start();
	}

	/**
	 * 添加一个Policy，若优先级为负，则自动添加优先级，并位于最后。
	 * 若存在同名的policy，则删除原有的；若不存在同名同优先级的，则添加。
	 * 若存在不同名同优先级的policy，则抛出异常。
	 *
	 * @param policy
	 */
	public void putPolicy(PretendPolicy policy)
	{
		policyRWLock.writeLock().lock();
		try
		{
			if (!policyMap.containsKey(policy.name) && prioritySet.contains(policy))
				throw new RuntimeException("存在不同名同优先级policy");
			if (policy.priority < 0)
			{
				policy.priority = auto_priority;
				auto_priority += 10;
			}
			if (policy.timeToLiveBySecond == 0)
				policy.timeToLiveBySecond = 1;
			policyMap.put(policy.name, policy);
			prioritySet.removeIf(p -> p.name.equals(policy.name));
			prioritySet.add(policy);
			serverInitMap.putIfAbsent(policy.pretendServer, new AtomicBoolean(false));
			if (prioritySet.size() != policyMap.size())
				throw new RuntimeException("请使用PolicyManager.config*()进行优先级和名字的修改");
		} finally
		{
			policyRWLock.writeLock().unlock();
		}
	}

	/**
	 * 添加一个policy，并且若存在与之一样的名字的policy，则优先级原有的一样。
	 *
	 * @param policy
	 */
	public void putPolicyWithSamePriority(PretendPolicy policy)
	{
		policyRWLock.writeLock().lock();
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
			prioritySet.removeIf(p -> p.name.equals(policy.name));
			policyMap.put(policy.name, policy);
			prioritySet.add(policy);
			serverInitMap.putIfAbsent(policy.pretendServer, new AtomicBoolean(false));
			if (prioritySet.size() != policyMap.size())
				throw new RuntimeException("请使用PolicyManager.config*()进行优先级和名字的修改");
		} finally
		{
			policyRWLock.writeLock().unlock();
		}
	}

	public boolean containsPolicy(String policyName)
	{
		policyRWLock.readLock().lock();
		boolean res = policyMap.containsKey(policyName);
		policyRWLock.readLock().unlock();
		return res;
	}

	public synchronized void deletePolicy(String policyName)
	{
		policyRWLock.writeLock().lock();
		try
		{
			if (!policyMap.containsKey(policyName))
				return;
			prioritySet.remove(policyMap.get(policyName));
			policyMap.remove(policyName);
			if (policyMap.size() != prioritySet.size())
				throw new RuntimeException("请使用PolicyManager.config*()进行优先级和名字的修改");
		} finally
		{
			policyRWLock.writeLock().unlock();
		}
	}

	public boolean isEnabled(String policyName)
	{
		policyRWLock.readLock().lock();
		boolean res = policyMap.get(policyName) != null && policyMap.get(policyName).enabled;
		policyRWLock.readLock().unlock();
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
		policyRWLock.writeLock().lock();
		if (policyMap.containsKey(policyName))
			policyMap.get(policyName).enabled = enabled;
		policyRWLock.writeLock().unlock();
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
		if (!serverInitMap.get(policy.pretendServer).getAndSet(true))
		{
			policy.pretendServer.startServer();
		}
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
		policyRWLock.writeLock().lock();
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
			policyRWLock.writeLock().unlock();
		}
	}

	/**
	 * 修改名字
	 *
	 * @param policyName
	 * @param newName
	 */
	public void configName(String policyName, String newName)
	{
		if (!policyMap.containsKey(policyName))
			return;
		policyRWLock.writeLock().lock();
		PretendPolicy policy = policyMap.get(policyName);
		policy.name = newName;
		policyMap.put(policy.name, policy);
		policyRWLock.writeLock().unlock();
	}

	/**
	 * 不会调用{@link PretendServerInterface#startServer()}，若delete后再添加，则还会调用上述方法。
	 *
	 * @param listener
	 */
	public void addPretendListener(PretendListener listener)
	{
		if (listener == null)
			return;
		listenerRWLock.writeLock().lock();
		pretendListenerList.add(listener);
		serverInitMap.putIfAbsent(listener.pretendServer, new AtomicBoolean(false));
		listenerRWLock.writeLock().unlock();
	}

	public boolean containsPretendListener(PretendListener listener)
	{
		listenerRWLock.readLock().lock();
		boolean res = pretendListenerList.contains(listener);
		listenerRWLock.readLock().unlock();
		return res;
	}

	/**
	 * 会调用{@link PretendServerInterface#stopServer()}
	 *
	 * @param listener
	 */
	public void deletePretendListener(PretendListener listener)
	{
		listenerRWLock.writeLock().lock();
		if (pretendListenerList.contains(listener))
		{
			listener.pretendServer.stopServer();
			serverInitMap.get(listener.pretendServer).set(false);
			pretendListenerList.remove(listener);
		}
		listenerRWLock.writeLock().unlock();
	}

	/**
	 * 不会调用{@link PretendServerInterface#startServer()}，若delete后再添加，则还会调用上述方法。
	 *
	 * @param listeners
	 */
	public void addPretendListener(List<PretendListener> listeners)
	{
		if (listeners == null)
			return;
		listenerRWLock.writeLock().lock();
		for (PretendListener listener : listeners)
		{
			pretendListenerList.add(listener);
			serverInitMap.putIfAbsent(listener.pretendServer, new AtomicBoolean(false));
		}
		listenerRWLock.writeLock().unlock();
	}

	public void callAfterConnectListener(Socket inSocket, InetAddress clientAddress) throws PretendException
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.afterConnect(clientAddress))
			{
				if (!serverInitMap.get(listener.pretendServer).getAndSet(true))
				{
					listener.pretendServer.startServer();
				}
				listener.pretendServer.pretend(inSocket, EventListenerInterface.TriggerType.AFTER_CONNECT, this, null, 0, 0);
				throw new PretendException("Pretend after connect:" + clientAddress.toString());
			}
		}
	}

	public void callBeforeContactListener(Socket inSocket, byte[] data, int offset, int length) throws PretendException
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.beforeContact(data, offset, length))
			{
				if (!serverInitMap.get(listener.pretendServer).getAndSet(true))
				{
					listener.pretendServer.startServer();
				}
				listener.pretendServer.pretend(inSocket, EventListenerInterface.TriggerType.BEFORE_CONTACT, this, data, offset, length);
				throw new PretendException("Pretend before contact:" + inSocket.getRemoteSocketAddress().toString());
			}
		}
	}

	public void callBeforeResolveListener(Socket inSocket, byte[] data, int offset, int length) throws PretendException
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.beforeResolve(data, offset, length))
			{
				if (!serverInitMap.get(listener.pretendServer).getAndSet(true))
				{
					listener.pretendServer.startServer();
				}
				listener.pretendServer.pretend(inSocket, EventListenerInterface.TriggerType.BEFORE_RESOLVE, this, data, offset, length);
				throw new PretendException("Pretend before resolve:" + inSocket.getRemoteSocketAddress().toString());
			}
		}
	}

	public void callAfterResolveListener(Socket inSocket, InetAddress requestAddress, byte cmd, byte version, byte resv, byte[] data, int offset, int length) throws PretendException
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.afterResolve(requestAddress, cmd, version, resv))
			{
				if (!serverInitMap.get(listener.pretendServer).getAndSet(true))
				{
					listener.pretendServer.startServer();
				}
				listener.pretendServer.pretend(inSocket, EventListenerInterface.TriggerType.AFTER_RESOLVE, this, data, offset, length);
				throw new PretendException("Pretend before resolve:" + requestAddress.toString());
			}
		}
	}

	public void callAfterFirstReadListener(Socket inSocket, byte[] data, int offset, int length) throws PretendException
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.afterFirstRead(data, offset, length))
			{
				if (!serverInitMap.get(listener.pretendServer).getAndSet(true))
				{
					listener.pretendServer.startServer();
				}
				listener.pretendServer.pretend(inSocket, EventListenerInterface.TriggerType.AFTER_FIRST_READ, this, data, offset, length);
				throw new PretendException("Pretend after first read:" + inSocket.getRemoteSocketAddress().toString());
			}
		}
	}

	public void callBeforeWriteListener(Socket inSocket, byte[] data, int offset, int length) throws PretendException
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.beforeWrite(data, offset, length))
			{
				if (!serverInitMap.get(listener.pretendServer).getAndSet(true))
				{
					listener.pretendServer.startServer();
				}
				listener.pretendServer.pretend(inSocket, EventListenerInterface.TriggerType.BEFORE_WRITE, this, data, offset, length);
				throw new PretendException("Pretend before write:" + inSocket.getRemoteSocketAddress().toString());
			}
		}
	}

	public void callAfterReadListener(Socket inSocket, byte[] data, int offset, int length) throws PretendException
	{
		for (PretendListener listener : pretendListenerList)
		{
			if (listener.listener.afterRead(data, offset, length))
			{
				if (!serverInitMap.get(listener.pretendServer).getAndSet(true))
				{
					listener.pretendServer.startServer();
				}
				listener.pretendServer.pretend(inSocket, EventListenerInterface.TriggerType.AFTER_READ, this, data, offset, length);
				throw new PretendException("Pretend after read:" + inSocket.getRemoteSocketAddress().toString());
			}
		}
	}

	public void shutdown()
	{
		timerCountThread.interrupt();
	}

	private PretendPolicy fetchPolicyByIp(String ip)
	{
		policyRWLock.readLock().lock();
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
			policyRWLock.readLock().unlock();
		}
	}
}
