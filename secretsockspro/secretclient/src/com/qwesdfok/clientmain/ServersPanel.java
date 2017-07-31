package com.qwesdfok.clientmain;

import com.qwesdfok.common.KeyInfo;
import com.qwesdfok.secretclient.ClientConfig;
import com.qwesdfok.utils.UITools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServersPanel
{
	private static final String CLOSE = "关闭";
	private static final String OPEN = "启动";
	private JFrame mainWindow;
	private ConfigReader configReader;
	private JPanel serverPanel = new JPanel();
	private JPanel gridPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();
	private List<ConfigReader.ConfigServerData> serverList;
	private HashMap<JButton, ConfigReader.ConfigServerData> buttonAttachmentMap = new HashMap<>();
	private final ArrayList<ListenerThread> listenerThreadList = new ArrayList<>();
	private boolean startAutoEnabledServers = false;
	private ActionListener deleteListener = new ActionListener()
	{
		ConfigReader.ConfigServerData serverData;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			JButton source = ((JButton) e.getSource());
			serverData = buttonAttachmentMap.get(source);
			if (serverData.autoEnabled)
				listenerThreadList.removeIf(thread -> {
					if (thread.equalsWithServerConfig(serverData.address, serverData.port, serverData.localPort))
					{
						thread.interrupt();
						return true;
					}
					return false;
				});
			serverList.removeIf(server -> server.address.equals(serverData.address) && server.port == serverData.port && server.localPort == serverData.localPort);
			configReader.updateServerConfig(serverList);
			renderGridPanel();
		}
	};
	private ActionListener startListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			JButton source = ((JButton) e.getSource());
			ConfigReader.ConfigServerData serverData = buttonAttachmentMap.get(source);
			if (serverData.enabled)
			{
				//当前enabled，因此变成disabled，选项变成enabled
				source.setText(OPEN);
				serverData.enabled = false;
				listenerThreadList.removeIf(thread -> {
					boolean same = thread.equalsWithServerConfig(serverData.address, serverData.port, serverData.localPort);
					if (same)
						thread.interrupt();
					return same;
				});
			} else
			{
				source.setText(CLOSE);
				serverData.enabled = true;
				ListenerThread thread = new ListenerThread(new KeyInfo(serverData.blockCipherType, serverData.byteCipherType, serverData.readKey, serverData.writeKey),
						new ClientConfig(serverData.localPort, serverData.address, serverData.port, serverData.bufferSize));
				thread.start();
				listenerThreadList.add(thread);
			}
		}
	};
	private ActionListener configListener = new ActionListener()
	{
		ConfigReader.ConfigServerData serverData;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			JButton source = ((JButton) e.getSource());
			serverData = buttonAttachmentMap.get(source);
			showConfigDialog(serverData);
		}
	};
	private ActionListener createListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			showConfigDialog(null);
		}
	};

	public ServersPanel(JFrame mainWindow, ConfigReader configReader)
	{
		this.mainWindow = mainWindow;
		this.configReader = configReader;
		gridPanel = new JPanel(new GridLayout(0, 7, 10, 10));
		buttonPanel = new JPanel();
		serverPanel.setLayout(new GridBagLayout());

		renderGridPanel();

		JButton createButton = new JButton("新建");
		JButton exitButton = new JButton("退出");
		createButton.addActionListener(createListener);
		exitButton.addActionListener(e -> mainWindow.dispose());
		buttonPanel.add(createButton);
		buttonPanel.add(exitButton);


		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(4, 8, 4, 8);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.NORTH;
		UITools tools = new UITools(constraints);
		serverPanel.add(gridPanel, tools.configConstraints(0, 0, 1, 1, 1, 1));
		serverPanel.add(buttonPanel, tools.configConstraints(0, 1, 1, 1, 1, 0));
	}

	public void closeAll()
	{
		for (ListenerThread thread : listenerThreadList)
		{
			if (thread.isAlive())
				thread.interrupt();
		}
		listenerThreadList.clear();
		serverList.clear();
	}

	public JPanel getServerPanel()
	{
		return serverPanel;
	}

	private void showConfigDialog(ConfigReader.ConfigServerData serverData)
	{
		ConfigDialog configDialog = new ConfigDialog(mainWindow, serverData);
		configDialog.setVisible(true);
		if (configDialog.getActionType() == ConfigDialog.ACTION_SAVE_AND_RESTART)
		{
			listenerThreadList.removeIf(thread -> {
				if (thread.equalsWithServerConfig(configDialog.getOldHost(), configDialog.getOldPort(), configDialog.getOldLocalPort()))
				{
					thread.interrupt();
					return true;
				}
				return false;
			});
			serverList.removeIf(server -> server.address.equals(configDialog.getOldHost()) && server.port == configDialog.getOldPort() && server.localPort == configDialog.getOldLocalPort());
			serverData = configDialog.getServerData();
			serverData.autoEnabled = true;
			serverList.add(serverData);
			ListenerThread thread = new ListenerThread(new KeyInfo(serverData.blockCipherType, serverData.byteCipherType, serverData.readKey, serverData.writeKey),
					new ClientConfig(serverData.localPort, serverData.address, serverData.port, serverData.bufferSize));
			thread.start();
			listenerThreadList.add(thread);
			configReader.updateServerConfig(serverList);
		} else if (configDialog.getActionType() == ConfigDialog.ACTION_SAVE)
		{
			serverList.removeIf(server -> server.address.equals(configDialog.getOldHost()) && server.port == configDialog.getOldPort() && server.localPort == configDialog.getOldLocalPort());
			serverData = configDialog.getServerData();
			serverList.add(serverData);
			configReader.updateServerConfig(serverList);
		}
		renderGridPanel();
	}

	private void renderGridPanel()
	{
		gridPanel.removeAll();
		gridPanel.add(new JLabel("删除"));
		gridPanel.add(new JLabel("服务器"));
		gridPanel.add(new JLabel("远程端口"));
		gridPanel.add(new JLabel("本地端口"));
		gridPanel.add(new JLabel("注释"));
		gridPanel.add(new JLabel("配置"));
		gridPanel.add(new JLabel("切换"));
		serverList = configReader.fetchServerData();
		for (ConfigReader.ConfigServerData data : serverList)
		{
			data.enabled = false;
			JButton deleteButton = new JButton("删除");
			deleteButton.addActionListener(deleteListener);
			buttonAttachmentMap.put(deleteButton, data);
			gridPanel.add(deleteButton);
			gridPanel.add(new JLabel(data.address));
			gridPanel.add(new JLabel(Integer.toString(data.port)));
			gridPanel.add(new JLabel(Integer.toString(data.localPort)));
			gridPanel.add(new JLabel(data.comment));
			JButton settingButton = new JButton("修改配置");
			settingButton.addActionListener(configListener);
			buttonAttachmentMap.put(settingButton, data);
			gridPanel.add(settingButton);
			JButton triggerButton = new JButton();
			if (!startAutoEnabledServers)
			{
				if (data.autoEnabled)
				{
					data.enabled = true;
					ListenerThread thread = new ListenerThread(new KeyInfo(data.blockCipherType, data.byteCipherType, data.readKey, data.writeKey),
							new ClientConfig(data.localPort, data.address, data.port, data.bufferSize));
					thread.start();
					listenerThreadList.add(thread);
				}
			}
			for (ListenerThread thread : listenerThreadList)
			{
				if (thread.equalsWithServerConfig(data.address, data.port, data.localPort))
					data.enabled = true;
			}
			if (data.enabled)
				triggerButton.setText(CLOSE);
			else
				triggerButton.setText(OPEN);
			buttonAttachmentMap.put(triggerButton, data);
			triggerButton.addActionListener(startListener);
			gridPanel.add(triggerButton);
		}
		gridPanel.invalidate();
		gridPanel.revalidate();
		startAutoEnabledServers = true;
	}
}
