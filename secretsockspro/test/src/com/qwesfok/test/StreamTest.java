package com.qwesfok.test;

import com.qwesdfok.common.AESBlock128Cipher;
import com.qwesdfok.common.CipherByteStream;
import com.qwesdfok.common.CipherByteStreamInterface;
import com.qwesdfok.common.XORByteCipher;
import com.qwesdfok.utils.Log;
import org.testng.annotations.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

public class StreamTest
{
	private byte[] passwordKey = "qwesdfok".getBytes();

	@Test
	public void client()
	{
		try
		{
			Socket socket = new Socket("127.0.0.1", 2020);
//			Socket socket = new Socket("138.197.105.220", 2020);
			CipherByteStream stream = new CipherByteStream(socket, new AESBlock128Cipher(passwordKey), new XORByteCipher(passwordKey), 1024);
			stream.write("474554202F20485454502F312E310D0A486F73743A207777772E62696C6962696C692E636F6D0D0A436F6E6E656374696F6E3A206B6565702D616C6976650D0A557067726164652D496E7365637572652D52657175657374733A20310D0A557365722D4167656E743A204D6F7A696C6C612F352E30202857696E646F7773204E542031302E303B2057696E36343B2078363429204170706C655765624B69742F3533372E333620284B48544D4C2C206C696B65204765636B6F29204368726F6D652F35392E302E333037312E313135205361666172692F3533372E33360D0A4163636570743A20746578742F68746D6C2C6170706C69636174696F6E2F7868746D6C2B786D6C2C6170706C69636174696F6E2F786D6C3B713D302E392C696D6167652F776562702C696D6167652F61706E672C2A2F2A3B713D302E380D0A4163636570742D456E636F64696E673A20677A69702C206465666C6174650D0A4163636570742D4C616E67756167653A207A682D434E2C7A683B713D302E380D0A436F6F6B69653A206674733D313439323736373737353B207067765F7076693D313933323236383534343B20554D5F64697374696E637469643D313562386665343966366235662D30346664396436386462303732612D33393662346530382D3166613430302D313562386665343966366361643B207369643D69616E64793866".getBytes());
			stream.flush();
			Log.infoLog(new String(stream.read()));
			stream.write("622E636F6D25324662667325324666616365253246336539633363643631316430336162303963366362653233316161386462663066626531363934632E6A70673B20434E5A5A44415441323732343939393D636E7A7A5F656964253344313435363839363030302D31".getBytes());
			stream.flush();
			Log.infoLog(new String(stream.read()));
			stream.close();
		} catch (Exception e)
		{
			Log.printException(e);
		}
	}

	@Test
	public void server()
	{
		try
		{
			ServerSocket serverSocket = new ServerSocket(2020, 0, InetAddress.getByName("0.0.0.0"));
			Socket socket = serverSocket.accept();
			CipherByteStream stream = new CipherByteStream(socket, new AESBlock128Cipher("qwesdfok".getBytes()), new XORByteCipher("qwesdfok".getBytes()), 1024);
			byte[] look = stream.look();
			byte[] data = stream.read();
			while (data != null)
			{
				System.out.println(new String(data));
				look = stream.look();
				data = stream.read();
			}
			stream.close();
		} catch (Exception e)
		{
			Log.printException(e);
		}
	}

	@Test
	public void nioServer()
	{
		try
		{
			Selector selector = Selector.open();

			ServerSocketChannel currentChanel = ServerSocketChannel.open();
			currentChanel.socket().setReuseAddress(true);
			currentChanel.bind(new InetSocketAddress("0.0.0.0", 2020));
			currentChanel.configureBlocking(false);
			currentChanel.register(selector, SelectionKey.OP_ACCEPT);
			while (true)
			{
				int size = selector.select(100);
				if (size == 0)
					continue;
				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
				while (iterator.hasNext())
				{
					SelectionKey key = iterator.next();
					iterator.remove();
					if (key.isAcceptable())
					{
						ServerSocketChannel channel = ((ServerSocketChannel) key.channel());
						Socket inSocket = channel.accept().socket();
						CipherByteStreamInterface inCipherStream = new CipherByteStream(inSocket,
								new AESBlock128Cipher(passwordKey), new XORByteCipher(passwordKey), 10 * 1024);
						byte[] data = inCipherStream.read();
						Log.infoLog(new String(data));
						while (data != null)
						{
							inCipherStream.write("received".getBytes());
							inCipherStream.flush();
							Thread.sleep(100);
							data = inCipherStream.read();
							if (data != null)
								Log.infoLog(new String(data));
						}
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
