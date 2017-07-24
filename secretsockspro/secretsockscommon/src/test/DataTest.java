package test;

import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;

public class DataTest
{
	@Test
	public void connect()
	{
		try
		{
			Socket socket = new Socket("127.0.0.1", 1180);
			BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
			BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());

			byte[] buffer = new byte[1024];
			int length = 0;
			outputStream.write(new byte[]{0x05, 0x01, 0x00});
			outputStream.flush();
			length = inputStream.read(buffer);
			outputStream.write(new byte[]{0x05, 0x01, 0x00, 0x01, (byte)0x8A, (byte) 0xc5, (byte) 0x69, (byte) 0xdc, 0x27, 0x10});
			outputStream.flush();
			length = inputStream.read(buffer);
			inputStream.close();
			outputStream.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Test
	public void test()
	{
		int a=1;
		System.out.println("233");
	}
}
