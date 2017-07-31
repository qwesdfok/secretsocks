package com.qwesdfok.clientmain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class MainWindow
{

	private JFrame mainWindow = new JFrame("SecretSock");
	private JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	private ServersPanel serversPanel;
	private ConfigReader configReader;
	private SystemTray systemTray;
	private TrayIcon trayIcon;

	public MainWindow()
	{
		mainWindow.setSize(700, 400);
		mainWindow.setLocationRelativeTo(null);

		try
		{
			configReader = new ConfigReader("./config.xml");
		} catch (IOException e)
		{
			mainWindow.add(new JTextArea("./config.xml配置文件读取出现错误：\n异常信息：" + e.getClass().getSimpleName() + ":" + e.getLocalizedMessage()));
			return;
		}
		serversPanel = new ServersPanel(mainWindow, configReader);
		JPanel settingPanel = new JPanel();


		tabbedPane.addTab("Servers", serversPanel.getServerPanel());

		tabbedPane.addTab("Setting", settingPanel);
		mainWindow.add(tabbedPane);

		if (!SystemTray.isSupported())
		{
			mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			return;
		}
		mainWindow.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		systemTray = SystemTray.getSystemTray();
		PopupMenu popupMenu = new PopupMenu();
		ImageIcon imageIcon = new ImageIcon(this.getClass().getResource("/picture/icon.jpg"));
		MenuItem showMainWindow = new MenuItem("Show");
		MenuItem exit = new MenuItem("Exit");
		popupMenu.add(showMainWindow);
		popupMenu.add(exit);
		trayIcon = new TrayIcon(imageIcon.getImage(), "Secretsocks", popupMenu);
		trayIcon.setImageAutoSize(true);
		trayIcon.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)showWindow();
			}
		});
		showMainWindow.addActionListener(e -> showWindow());
		exit.addActionListener(e -> closeWindow());
		mainWindow.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				hiddenWindow();
			}

			@Override
			public void windowClosed(WindowEvent e)
			{
				serversPanel.closeAll();
			}
		});
	}

	public void setVisible(boolean visible)
	{
		mainWindow.setVisible(visible);
	}

	private void hiddenWindow()
	{
		mainWindow.setVisible(false);
		try
		{
			systemTray.add(trayIcon);
		} catch (AWTException e)
		{
			e.printStackTrace();
			mainWindow.dispose();
		}
	}

	private void showWindow()
	{
		systemTray.remove(trayIcon);
		mainWindow.setVisible(true);
	}

	private void closeWindow()
	{
		systemTray.remove(trayIcon);
		mainWindow.dispose();
	}
}
