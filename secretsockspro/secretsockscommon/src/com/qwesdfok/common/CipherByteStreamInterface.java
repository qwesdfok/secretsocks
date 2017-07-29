package com.qwesdfok.common;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;

public interface CipherByteStreamInterface
{
	/**
	 * 获得所包装的Socket对象
	 * @return Socket对象
	 */
	Socket getSocket();

	/**
	 * 用于预览未进行解密的数据。建议只有当buffer为0时才进行阻塞读取操作，或者read()方法每次只解析一个数据包，look()缓存将要解析的下个数据包。
	 * @return 未解密的数据
	 * @throws IOException
	 */
	byte[] look() throws IOException;

	/**
	 * 读取解密后的数据。建议保持与write()发送的数据的完整性。
	 * @return 解密后的数据
	 * @throws IOException
	 * @throws GeneralSecurityException 解密算法解密过程发生异常
	 */
	byte[] read() throws IOException, GeneralSecurityException;

	/**
	 * 加密输入的数据，建议不缓存数据而全部发送（可能需要填充算法）。
	 * @param data 输入数据
	 * @throws IOException
	 * @throws GeneralSecurityException 加密过程发生异常
	 */
	void write(byte[] data) throws IOException, GeneralSecurityException;

	/**
	 * 加密输入的数据，建议不缓存数据而全部发送（可能需要填充算法）。
	 * @param data 输入数据
	 * @param offset 起始字符的index
	 * @param length 字符长度
	 * @throws IOException
	 * @throws GeneralSecurityException 加密过程发生异常
	 */
	void write(byte[] data, int offset, int length) throws IOException, GeneralSecurityException;

	void flush() throws IOException;

	void close() throws IOException;

	boolean isClosed();
}
