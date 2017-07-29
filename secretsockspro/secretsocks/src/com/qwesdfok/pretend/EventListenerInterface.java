package com.qwesdfok.pretend;

import java.net.InetAddress;

/**
 * 返回值true表示需要进行伪装
 */
public interface EventListenerInterface
{
	/**
	 * 连接建立之后而没有读取任何数据
	 *
	 * @param clientAddress client或者嗅探端的地址
	 * @return true表示需要进行伪装
	 */
	boolean afterConnect(InetAddress clientAddress);

	/**
	 * 建立连接后读取的第一份未进行解密过的数据，一般在此进行其他协议的判断，以进行初步的伪装。
	 * 例子可见{@link com.qwesdfok.pretend.HttpListener}或者{@link com.qwesdfok.pretend.HttpsListener}
	 *
	 * @param data   读取的未解密数据
	 * @param offset
	 * @param length
	 * @return
	 */
	boolean beforeContact(byte[] data, int offset, int length);

	/**
	 * 该事件发生在Sock5协议第一次协商之后，请求解析之前。
	 *
	 * @param data   未解密过的数据
	 * @param offset
	 * @param length
	 * @return
	 */
	boolean beforeResolve(byte[] data, int offset, int length);

	/**
	 * 该事件发生在Socks5协议的请求解析之后，与外界Internet连接建立之前
	 *
	 * @param requestAddress 所请求的外界的地址
	 * @param cmd            请求类型。0x01 Connect请求，0x02 Bind请求，0x03 UDP请求
	 * @param version        socks5版本必须为0x05
	 * @param resv           Sock5保留字段 0x00
	 * @return
	 */
	boolean afterResolve(InetAddress requestAddress, byte cmd, byte version, byte resv);

	/**
	 * 事件发生在第一次从client数据读取之后
	 *
	 * @param data   未解密的数据
	 * @param offset
	 * @param length
	 * @return
	 */
	boolean afterFirstRead(byte[] data, int offset, int length);

	/**
	 * 事件发生在第二次以及之后的每次从client端数据读取之后
	 *
	 * @param data   未解密的数据
	 * @param offset
	 * @param length
	 * @return
	 */
	boolean afterRead(byte[] data, int offset, int length);

	/**
	 * 事件发生在每次数据写入给client之前，不包括Socks5协议内容
	 *
	 * @param data   未加密数据
	 * @param offset
	 * @param length
	 * @return
	 */
	boolean beforeWrite(byte[] data, int offset, int length);

	enum TriggerType
	{
		POLICY_MANAGER, AFTER_CONNECT, BEFORE_CONTACT, BEFORE_RESOLVE, AFTER_RESOLVE, AFTER_FIRST_READ, AFTER_READ, BEFORE_WRITE
	}
}
