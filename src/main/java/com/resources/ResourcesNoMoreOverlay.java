package com.resources;


import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.Set;

import com.sun.tools.jconsole.JConsoleContext;
import lombok.extern.java.Log;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.awt.*;

public class ResourcesNoMoreOverlay extends Overlay {

    private final Client client;
    private final ResourcesNoMorePlugin plugin;
    private final ResourcesNoMoreConfig config;

    @Inject
    ResourcesNoMoreOverlay(Client client, ResourcesNoMorePlugin plugin, ResourcesNoMoreConfig config)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }
    @Override
    public Dimension render(Graphics2D graphics)
    {
        final Player player = client.getLocalPlayer();
        if (player == null)
        {
            return null;
        }

        if (config.removeRocks())
        {
            Set<Tile> ores = plugin.getDepletedOres();
            if (!ores.isEmpty())
            {
                for (Tile tile : ores)
                {
                    renderTile(graphics, tile);
                }
            }
        }
        if (config.removeTrees())
        {
            Set<Tile> trees = plugin.getDepletedTrees();
            if (!trees.isEmpty())
            {
                for (Tile tile : trees)
                {
                    WorldPoint wP = tile.getWorldLocation();

                    for (GameObject gameObject : tile.getGameObjects())
                    {
                        if (gameObject != null)
                        {
                            gameObject.sizeX();
                        }
                    }

                    renderTile(graphics, tile);
                }
            }
        }
        return null;
    }


    private void renderTile(final Graphics2D graphics, final Tile tile)
    {
        if (tile == null)
        {
            return;
        }
        final Polygon poly = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());
        if (poly != null) {
            OverlayUtil.renderPolygon(graphics, poly, Color.GRAY, new BasicStroke(8.0f));
        }
    }
}
