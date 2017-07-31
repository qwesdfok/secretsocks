package com.qwesdfok.servermain;

import com.qwesdfok.utils.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by qwesdfok on 2017/3/6.
 */
public class CommandReader
{
	private static final List<String> commandNameList = new ArrayList<>();

	public static class Command
	{
		String name;
		List<String> argv;

		public Command(String name, List<String> argv)
		{
			this.name = name;
			this.argv = argv;
		}

		public Command()
		{
		}
	}

	public static class CommandResult
	{
		String serverHost = null;
		String manipulate = null;
		String readKey;
		String writeKey;
		String blockCipherType = "AES-128";
		String byteCipherType = "XOR";
		int serverPort = -1;
		int daemonPort = 2030;
		int bufferSize = -1;

		public String toCommand()
		{
			StringBuilder buffer = new StringBuilder();
			if (serverHost != null)
			{
				buffer.append("-s ");
				buffer.append(serverHost).append(" ");
				buffer.append(serverPort).append(" ");
			}
			if (manipulate != null)
			{
				buffer.append("-m ");
				buffer.append(manipulate).append(" ");
			}
			buffer.append("-d ").append(daemonPort).append(" ");
			buffer.append("-r \"").append(readKey).append("\" ");
			buffer.append("-w \"").append(writeKey).append("\" ");
			buffer.append("-b ").append(bufferSize).append(" ");
			return buffer.toString();
		}
	}

	static
	{
		addCommandName("--help|-h");
		addCommandName("--server|-s");
		addCommandName("--tutorial|-t");
		addCommandName("--log|-l");
		addCommandName("--manipulate|-m");
		addCommandName("--daemon|-d");
		addCommandName("--key|-k");
		addCommandName("--readkey|-r");
		addCommandName("--writekey|-w");
		addCommandName("--buffersize|-b");
		addCommandName("--cipher|-c");
	}

	public CommandReader()
	{
	}

	public static CommandResult startReader(String data)
	{
		if (data == null)
			return null;
		ArrayList<String> list = new ArrayList<>();
		int index = 0;
		int start = 0;
		boolean p = false;
		while (index < data.length())
		{
			if (data.charAt(index) == '"' || data.charAt(index) == '\'')
			{
				if (p)
				{
					list.add(data.substring(start, index));
					start = index + 1;
					p = false;
				} else
				{
					p = true;
					start = index + 1;
				}
			} else if (data.charAt(index) == ' ' && !p)
			{
				if (start != index)
				{
					list.add(data.substring(start, index));
					start = index + 1;
				} else
				{
					start++;
				}
			}
			index++;
		}
		return startReader(list.toArray(new String[0]));
	}

	public static CommandResult startReader(String[] argv)
	{
		List<Command> commandList = new ArrayList<>();
		int index = 0;
		while (index < argv.length && commandNameList.contains(argv[index]))
		{
			String name = argv[index];
			ArrayList<String> argBuffer = new ArrayList<>();
			index++;
			while (index < argv.length && !commandNameList.contains(argv[index]))
			{
				argBuffer.add(argv[index]);
				index++;
			}
			commandList.add(new Command(name, argBuffer));
		}
		CommandResult result = new CommandResult();
		for (CommandReader.Command command : commandList)
		{
			if ("--help".equals(command.name) || "-h".equals(command.name))
				commandHelp();
			else if ("--server".equals(command.name) || "-s".equals(command.name))
				commandServer(command.argv, result);
			else if ("--tutorial".equals(command.name) || "-t".equals(command.name))
				commandTutorial();
			else if ("--log".equals(command.name) || "-l".equals(command.name))
				commandLog(command.argv);
			else if ("--manipulate".equals(command.name) || "-m".equals(command.name))
				commandManipulate(command.argv, result);
			else if ("--daemon".equals(command.name) || "-d".equals(command.name))
				commandDaemon(command.argv, result);
			else if ("--key".equals(command.name) || "-k".equals(command.name))
				commandKey(command.argv, result);
			else if ("--readkey".equals(command.name) || "-r".equals(command.name))
				commandReadKey(command.argv, result);
			else if ("--writekey".equals(command.name) || "-w".equals(command.name))
				commandWriteKey(command.argv, result);
			else if ("--buffersize".equals(command.name) || "-b".equals(command.name))
				commandBufferSize(command.argv, result);
			else if ("--cipher".equals(command.name) || "-c".equals(command.name))
				commandCipher(command.argv, result);
		}
		return result;
	}

	private static void addCommandName(String name)
	{
		String[] split = name.split("\\|");
		for (String s : split)
		{
			commandNameList.add(s);
		}
	}

	private static void commandHelp()
	{
		Log.outputLog("--help/-h 获取帮助信息");
		Log.outputLog("--server/-s Host Port 设置监听服务，用来处理所接受的数据，将Socket对象传入对应的MessageConfig");
		Log.outputLog("--tutorial/-t 显示说明文档");
		Log.outputLog("--manipulate/-m Status 改变服务的运行状态。可选Status有start,pause,stop,restart,update,resume");
		Log.outputLog("--log/-l [enable|disable] Label... 打开或关闭某项Log。可选Label有output,info,warning,trace,debug,error,time,label。\n" +
				"--log/-l help 显示log指令的帮助信息");
		Log.outputLog("--key/-k Key 配置read和write的密钥。");
		Log.outputLog("--readkey/-r Key 配置read的密钥。客户端请将该密钥作为write密钥。");
		Log.outputLog("--writekey/-w Key 配置write的密钥。客户端请将该密钥作为read密钥。");
		Log.outputLog("--buffersize/-b Size 配置缓冲区大小，只作为参考值。");
		Log.outputLog("--cipher/-c BlockCipherType ByteCipherType 设置Block和Byte的加密类型，默认分别为AES-128和XOR加密。\n"
				+ "            BlockType: AES-128\n"
				+ "            ByteType : XOR\n");
	}

	private static void commandTutorial()
	{
		Log.outputLog("本系统主要的目的在于，在两台主机之间通过Socks5协议构建一个安全可靠、具有伪装机制的连接。\n" +
				"详情见https://github.com/qwesdfok/secretsocks\n");
	}

	private static void commandLog(List<String> argv)
	{
		if (argv.size() == 0)
		{
			Log.errorLog("参数输入错误");
		} else if (argv.size() == 1)
		{
			if (!"help".equals(argv.get(0)))
			{
				Log.errorLog("期望的--log参数为help");
				return;
			}
			Log.outputLog("output为正常的交互信息，不建议关闭\n" +
					"info为一些提示信息和进度信息\n" +
					"warning为警告信息，其代表有一些错误但不会影响整体的运行\n" +
					"error为错误信息，代表整个系统无法正常的运行\n" +
					"trace和debug为调试时所用\n" +
					"time为输出时是否附带时间，label为输出时是否附带日志级别，二者默认关闭\n");
		} else
		{
			boolean enable = "enable".equals(argv.get(0));
			HashMap<String, Integer> setting = new HashMap<>();
			setting.put("output", Log.OUTPUT_LOG);
			setting.put("info", Log.INFO_LOG);
			setting.put("warning", Log.WARNING_LOG);
			setting.put("error", Log.ERROR_LOG);
			setting.put("trace", Log.TRACE_LOG);
			setting.put("debug", Log.DEBUG_LOG);
			setting.put("time", Log.TIME_LOG);
			setting.put("label", Log.LABEL_LOG);
			for (String arg : argv)
			{
				if (setting.get(arg) != null)
				{
					if (enable)
						Log.enableLog(setting.get(arg));
					else
						Log.disableLog(setting.get(arg));
				}
			}
		}
	}

	private static void commandManipulate(List<String> argv, CommandResult result)
	{
		if (argv.size() != 1)
		{
			Log.errorLog("Manipulate的参数不正确");
			result.manipulate = null;
		} else
		{
			result.manipulate = argv.get(0);
		}
	}

	private static void commandServer(List<String> argv, CommandResult result)
	{
		if (argv.size() != 2)
		{
			Log.errorLog("未输入监听的Host和Port，或参数过多");
			return;
		}
		result.serverHost = argv.get(0);
		result.serverPort = Integer.parseInt(argv.get(1));
	}

	private static void commandDaemon(List<String> argv, CommandResult result)
	{
		if (argv.size() != 1)
		{
			Log.errorLog("输入的Daemon的参数不正确");
			return;
		}
		result.daemonPort = Integer.parseInt(argv.get(0));
	}

	private static void commandKey(List<String> argv, CommandResult result)
	{
		if (argv.size() != 1)
		{
			Log.errorLog("输入的Key的参数不正确");
			return;
		}
		result.readKey = argv.get(0);
		result.writeKey = argv.get(0);
	}

	private static void commandReadKey(List<String> argv, CommandResult result)
	{
		if (argv.size() != 1)
		{
			Log.errorLog("输入的ReadKey的参数不正确");
			return;
		}
		result.readKey = argv.get(0);
	}

	private static void commandWriteKey(List<String> argv, CommandResult result)
	{
		if (argv.size() != 1)
		{
			Log.errorLog("输入的WriteKey的参数不正确");
			return;
		}
		result.writeKey = argv.get(0);
	}

	private static void commandBufferSize(List<String> argv, CommandResult result)
	{
		if (argv.size() != 1)
		{
			Log.errorLog("输入的BufferSize的参数不正确");
			return;
		}
		try
		{
			result.bufferSize = Integer.parseInt(argv.get(0));
		} catch (Exception e)
		{
			result.bufferSize = -1;
		}
	}

	private static void commandCipher(List<String> argv, CommandResult result)
	{
		if (argv.size() != 2)
		{
			Log.errorLog("输入的CipherType的参数不正确");
			return;
		}
		result.blockCipherType = argv.get(0);
		result.byteCipherType = argv.get(1);
	}
}
