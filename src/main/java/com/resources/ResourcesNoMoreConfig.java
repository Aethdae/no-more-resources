package com.resources;

import net.runelite.api.gameval.SpriteID;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("example")
public interface ResourcesNoMoreConfig extends Config
{
	@ConfigItem(
		keyName = "removeRocks",
		name = "Remove rocks",
		description = "Whether to remove rocks"
	)
	default boolean removeRocks()
	{
		return true;
	}
    @ConfigItem(
            keyName = "removeTree",
            name = "Remove trees",
            description = "Whether to remove trees"
    )
    default boolean removeTrees()
    {
        return true;
    }
}
