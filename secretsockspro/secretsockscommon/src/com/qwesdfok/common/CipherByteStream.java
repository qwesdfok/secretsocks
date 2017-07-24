package com.qwesdfok.common;

import com.qwesdfok.utils.QUtils;

import java.io.*;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

public class CipherByteStream implements CipherByteStreamInterface
{
	private class Header
	{
		byte cipherType;
		byte control;
		int dataLength;
		int headLength;

		Header()
		{
		}

		Header(byte cipherType, byte control, int dataLength)
		{
			this.cipherType = cipherType;
			this.control = control;
			this.dataLength = dataLength;
		}

		byte[] encodeHeader() throws GeneralSecurityException
		{
			//                       1111 1111|111 11111|11 111111|1 1111111
			headLength = 2;
			headLength += (dataLength & 0x70000000) != 0 ? 5 : (dataLength & 0x0fe00000) != 0 ? 4 : (dataLength & 0x001fc000) != 0 ? 3 : (dataLength & 0x00003F80) != 0 ? 2 : 1;
			byte[] head = new byte[headLength];
			head[0] = cipherType;
			head[1] = control;
			for (int i = 2; i < headLength; i++)
				head[i] = (byte) ((dataLength >>> (headLength - i - 1) * 7) | 0x80);
			head[head.length - 1] = (byte) (head[head.length - 1] & 0x7f);
			for (int i = 0; i < head.length; i++)
			{
				head[i] = byteCipher.encrypt(head[i]);
			}
			return head;
		}

		public boolean decodeHeader(byte[] data, int offset, int length) throws GeneralSecurityException
		{
			if (length <= 3)
				return false;
			cipherType = byteCipher.decrypt(data[offset]);
			control = byteCipher.decrypt(data[offset + 1]);
			ArrayList<Byte> dataLengthBytes = new ArrayList<>(5);
			for (int i = offset + 2; i < offset + length; i++)
			{
				byte lb = byteCipher.decrypt(data[i]);
				dataLengthBytes.add((byte) (lb & 0x7f));
				if ((lb & 0x80) == 0)
					break;
			}
			headLength = 2 + dataLengthBytes.size();
			dataLength = 0;
			for (int i = 0; i < dataLengthBytes.size(); i++)
			{
				dataLength = (dataLength << 7) + dataLengthBytes.get(i);
			}
			return length >= headLength + dataLength;
		}
	}

	private class DecodeResult
	{
		byte[] result;
		int usedLength;
	}

	private Socket socket;
	private final InputStream inputStream;
	private final OutputStream outputStream;
	private BlockCipherInterface blockCipher;
	private ByteCipherInterface byteCipher;
	private byte[] readBuffer;
	private int readLength = 0;
	private int bufferSize;


	public CipherByteStream(Socket socket, BlockCipherInterface blockCipher, ByteCipherInterface byteCipher, int bufferSize) throws IOException
	{
		this.socket = socket;
		this.inputStream = new BufferedInputStream(socket.getInputStream());
		this.outputStream = new BufferedOutputStream(socket.getOutputStream());
		this.blockCipher = blockCipher;
		this.byteCipher = byteCipher;
		this.bufferSize = bufferSize;
		this.readBuffer = new byte[bufferSize];
		try
		{
			blockCipher.init();
			byteCipher.init();
		} catch (Exception e)
		{
			QUtils.printException(e);
			throw new SocksException("初始化加密算法失败", e);
		}
	}

	private DecodeResult readFromComplete(byte[] data, Header header) throws GeneralSecurityException
	{
		DecodeResult result = new DecodeResult();
		result.result = blockCipher.decrypt(data, header.headLength, header.dataLength);
		result.usedLength = header.headLength + header.dataLength;
		return result;
	}

	@Override
	public byte[] read() throws IOException, GeneralSecurityException
	{
		synchronized (inputStream)
		{
			Header header = new Header();
			boolean alreadyDecodeHeader = false;
			if (readLength > 0)
			{
				alreadyDecodeHeader = true;
				if (header.decodeHeader(readBuffer, 0, readLength))
				{
					DecodeResult result = readFromComplete(readBuffer, header);
					System.arraycopy(readBuffer, result.usedLength, readBuffer, 0, readLength - result.usedLength);
					readLength = readLength - result.usedLength;
					return result.result;
				}
			}
			int length = inputStream.read(readBuffer, readLength, readBuffer.length - readLength);
			if (length == -1)
				return null;
			readLength += length;
			boolean complete;
			if (alreadyDecodeHeader)
				complete = readLength >= header.headLength + header.dataLength;
			else
				complete = header.decodeHeader(readBuffer, 0, readLength);
			if (complete)
			{
				DecodeResult result = readFromComplete(readBuffer, header);
				System.arraycopy(readBuffer, result.usedLength, readBuffer, 0, readLength - result.usedLength);
				readLength = readLength - result.usedLength;
				return result.result;
			}
			int remainSize = header.headLength + header.dataLength - readLength;
			byte[] totalBuffer = new byte[readLength + remainSize];
			System.arraycopy(readBuffer, 0, totalBuffer, 0, readLength);
			int totalLength = readLength;
			while (totalLength < totalBuffer.length)
			{
				length = inputStream.read(totalBuffer, totalLength, totalBuffer.length - totalLength);
				if (length == -1)
					return null;
				totalLength += length;
			}
			DecodeResult result = readFromComplete(totalBuffer, header);
			readBuffer = totalBuffer;
			readLength = 0;
			return result.result;
		}
	}

	@Override
	public void write(byte[] data) throws IOException, GeneralSecurityException
	{
		write(data, 0, data.length);
	}

	/**
	 * cipherType: 1 字节 加密类型
	 * control   : 1 字节 控制字节
	 * dataLength: 1-n 字节 数据长度，每字节的第1 bit若为1表示后一字节也是长度字节，若为0 bit表示该字节为最后一个长度字节
	 * data      : dataLength
	 *
	 * @param data
	 * @param offset
	 * @param length
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	@Override
	public void write(byte[] data, int offset, int length) throws IOException, GeneralSecurityException
	{
		synchronized (outputStream)
		{
			if (length == 0)
				return;
			byte[] result = blockCipher.encrypt(data, 0, length);
			Header header = new Header((byte) 0x01, (byte) 0x00, result.length);
			byte[] head = header.encodeHeader();
			byte[] buffer = new byte[head.length + result.length];
			System.arraycopy(head, 0, buffer, 0, head.length);
			System.arraycopy(result, 0, buffer, head.length, result.length);
			outputStream.write(buffer);
		}
	}

	@Override
	public void flush() throws IOException
	{
		outputStream.flush();
	}

	@Override
	public void close() throws IOException
	{
		socket.close();
	}
}
