package com.qwesdfok.clientmain;

import com.qwesdfok.utils.UITools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ConfigDialog
{
	public static final int ACTION_SAVE_AND_RESTART = 0x01;
	public static final int ACTION_SAVE = 0x02;
	public static final int ACTION_CANCEL = 0x03;
	private String oldHost;
	private int oldPort;
	private int oldLocalPort;
	private ConfigReader.ConfigServerData serverData;
	private Dialog dialog;
	private int actionType;
	private boolean create = false;
	private JTextField host, port, localPort, blockCipherType, byteCipherType, readKey, writeKey, bufferSize, comment;
	private JCheckBox autoEnable;
	private ActionListener cancelListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			actionType = ACTION_CANCEL;
			dialog.dispose();
		}
	};
	private ActionListener saveListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			actionType = ACTION_SAVE;
			saveData();
			dialog.dispose();
		}
	};
	private ActionListener saveAndRestartListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			actionType = ACTION_SAVE_AND_RESTART;
			saveData();
			dialog.dispose();
		}
	};

	public ConfigDialog(JFrame mainWindow, ConfigReader.ConfigServerData serverData)
	{
		if (serverData == null)
		{
			serverData = new ConfigReader.ConfigServerData();
			create = true;
		}
		this.serverData = serverData;
		oldHost = serverData.address;
		oldPort = serverData.port;
		oldLocalPort = serverData.localPort;
		dialog = new Dialog(mainWindow, true);
		dialog.setLayout(new GridBagLayout());
		dialog.setSize(450, 450);
		dialog.setLocationRelativeTo(mainWindow);
		dialog.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				actionType = ACTION_CANCEL;
				dialog.dispose();
			}
		});
		JPanel dataPanel = new JPanel();
		JPanel buttonPanel = new JPanel();

		dataPanel.setLayout(new GridLayout(0, 2, 10, 10));
		host = new JTextField(serverData.address);
		port = new JTextField(Integer.toString(serverData.port));
		localPort = new JTextField(Integer.toString(serverData.localPort));
		blockCipherType = new JTextField(serverData.blockCipherType);
		byteCipherType = new JTextField(serverData.byteCipherType);
		readKey = new JTextField(serverData.readKey);
		writeKey = new JTextField(serverData.writeKey);
		bufferSize = new JTextField(Integer.toString(serverData.bufferSize / 1024));
		comment = new JTextField(serverData.comment);
		autoEnable = new JCheckBox("启动", serverData.autoEnabled);
		dataPanel.add(new JLabel("注释:"));
		dataPanel.add(comment);
		dataPanel.add(new JLabel("远端Host:"));
		dataPanel.add(host);
		dataPanel.add(new JLabel("远端Port:"));
		dataPanel.add(port);
		dataPanel.add(new JLabel("本地监听Port"));
		dataPanel.add(localPort);
		dataPanel.add(new JLabel("BlockCipherType"));
		dataPanel.add(blockCipherType);
		dataPanel.add(new JLabel("ByteCipherType"));
		dataPanel.add(byteCipherType);
		dataPanel.add(new JLabel("ReadKey"));
		dataPanel.add(readKey);
		dataPanel.add(new JLabel("WriteKey"));
		dataPanel.add(writeKey);
		dataPanel.add(new JLabel("BufferSize"));
		JPanel bufferSizePanel = new JPanel(new GridLayout(0, 2, 10, 10));
		bufferSizePanel.add(bufferSize);
		bufferSizePanel.add(new JLabel("KB"));
		dataPanel.add(bufferSizePanel);
		dataPanel.add(new JLabel("自动启动"));
		dataPanel.add(autoEnable);

		JButton saveAndRestart = new JButton("保存并重启");
		JButton save = new JButton("保存");
		JButton cancel = new JButton("取消");
		saveAndRestart.addActionListener(saveAndRestartListener);
		save.addActionListener(saveListener);
		cancel.addActionListener(cancelListener);
		if (!create)
			buttonPanel.add(saveAndRestart);
		buttonPanel.add(save);
		buttonPanel.add(cancel);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(4, 8, 4, 8);
		constraints.fill = GridBagConstraints.BOTH;

		UITools tools = new UITools(constraints);
		dialog.add(dataPanel, tools.configConstraints(0, 0, 1, 1, 1, 1));
		dialog.add(buttonPanel, tools.configConstraints(0, 1, 1, 1, 1, 0));
	}

	public void setVisible(boolean visible)
	{
		dialog.setVisible(visible);
	}

	private void saveData()
	{
		serverData = new ConfigReader.ConfigServerData();
		serverData.address = host.getText();
		serverData.port = Integer.parseInt(port.getText());
		serverData.localPort = Integer.parseInt(localPort.getText());
		serverData.byteCipherType = byteCipherType.getText();
		serverData.blockCipherType = blockCipherType.getText();
		serverData.readKey = readKey.getText();
		serverData.writeKey = writeKey.getText();
		serverData.comment = comment.getText();
		serverData.bufferSize = Integer.parseInt(bufferSize.getText()) * 1024;
		serverData.autoEnabled = autoEnable.isSelected();
	}

	public int getActionType()
	{
		return actionType;
	}

	public ConfigReader.ConfigServerData getServerData()
	{
		return serverData;
	}

	public String getOldHost()
	{
		return oldHost;
	}

	public int getOldPort()
	{
		return oldPort;
	}

	public int getOldLocalPort()
	{
		return oldLocalPort;
	}
}
