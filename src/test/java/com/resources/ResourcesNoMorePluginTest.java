package com.resources;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ResourcesNoMorePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ResourcesNoMorePlugin.class);
		RuneLite.main(args);
	}
}