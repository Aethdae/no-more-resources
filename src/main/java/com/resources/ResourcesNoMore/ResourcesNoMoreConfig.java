package com.resources.ResourcesNoMore;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("ResourcesNoMore")
public interface ResourcesNoMoreConfig extends Config
{
	@ConfigItem(
		keyName = "removeRocks",
		name = "Remove Rocks",
		description = "Whether to remove rocks"
	)
	default boolean removeRocks()
	{
		return true;
	}
    @ConfigItem(
            keyName = "removeTree",
            name = "Remove Trees",
            description = "Whether to remove trees"
    )
    default boolean removeTrees()
    {
        return true;
    }
    @ConfigItem(
            keyName = "tileColor",
            name = "Tile Color",
            description = "Color of the bottom tile"
    )
    default Color tileColor()
    {
        return Color.GRAY;
    }

    @ConfigItem(
            keyName = "removeTiles",
            name = "Remove Tile Outline",
            description = "Remove tile outline of depleted objects"
    )
    default boolean removeTiles()
    {
        return false;
    }

    @ConfigItem(
            keyName = "outline",
            name = "Show Object Outline",
            description = "Show outline of depleted objects"
    )
    default boolean outline()
    {
        return true;
    }

    @ConfigItem(
            keyName = "outlineColor",
            name = "Model Outline Color",
            description = "Color around depleted models"
    )
    default Color modelOutlineColor()
    {
        return Color.GRAY;
    }
}
