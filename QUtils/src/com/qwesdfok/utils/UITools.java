package com.qwesdfok.utils;

import java.awt.*;

/**
 * Created by qwesdfok on 2016/12/21.
 */
public class UITools
{
	private GridBagConstraints constraints;

	public UITools()
	{
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets(4, 8, 4, 8);
	}

	public UITools(GridBagConstraints constraints)
	{
		this.constraints = constraints;
	}

	public GridBagConstraints configConstraints(int x, int y)
	{
		return configConstraints(x, y, 1, 1, 1.0, 1.0);
	}

	public GridBagConstraints configConstraints(int x, int y, int width, int height)
	{
		return configConstraints(x, y, width, height, 1.0, 1.0);
	}

	public GridBagConstraints configConstraints(int x, int y, int width, int height, double dx, double dy)
	{
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.gridwidth = width;
		constraints.gridheight = height;
		constraints.weightx = dx;
		constraints.weighty = dy;
		return constraints;
	}

	public GridBagConstraints getConstraints()
	{
		return constraints;
	}

	public UITools setConstraints(GridBagConstraints constraints)
	{
		this.constraints = constraints;
		return this;
	}
}
