package com.qwesdfok.clientmain;

public class MainClient
{
	public static void main(String[]argv)
	{
		try
		{
			MainWindow mainWindow = new MainWindow();
			mainWindow.setVisible(true);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
