package com.qwesdfok.pretend;

import java.net.Socket;

public interface PretendServerInterface
{
	/**
	 * <div>接管所有Client发送的数据，用以伪装secretsocks。一旦该方法返回，将会关闭所有的连接。</div>
	 * <div>注意若将this注册到PolicyManager中，需要保证线程安全。</div>
	 * @param socket client的Socket连接
	 * @param triggerType 触发的事件类型
	 * @param policyManager 全局的PolicyManager
	 * @param triggerData 触发Pretend的Data，若为null，则表示PolicyManager或者AfterConnect触发
	 * @param offset 起始字符的index
	 * @param length 字符长度
	 */
	void pretend(Socket socket, EventListenerInterface.TriggerType triggerType, PolicyManager policyManager, byte[] triggerData, int offset, int length);
}
