package com.diabolickal.forestryplugin;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ForestryHelperTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ForestryHelperPlugin.class);
		RuneLite.main(args);
	}
}