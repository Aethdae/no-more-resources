package com.resources.ResourcesNoMore;


import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;

import com.google.common.collect.Multimap;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ResourcesNoMoreOverlay extends Overlay
{

    private final Client client;
    private final ResourcesNoMorePlugin plugin;
    private final ResourcesNoMoreConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    ResourcesNoMoreOverlay(Client client, ResourcesNoMorePlugin plugin, ResourcesNoMoreConfig config, ModelOutlineRenderer modelOutlineRenderer) {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        final int maxDistance = config.maxDistance();
        final Multimap<WorldView, ResourceTile> depletedTrees = plugin.getDepletedTrees();
        final Multimap<WorldView, ResourceTile> depletedRocks = plugin.getDepletedRocks();
        final ArrayList<Tile> treeTiles = plugin.treeTiles;
        final ArrayList<Tile> rockTiles = plugin.rockTiles;
        final Player player = client.getLocalPlayer();
        if (player == null) {
            return null;
        }
        if (config.removeRocks()) {
            for (Tile tile : rockTiles) {
                for(WorldView wV : depletedRocks.keySet())
                {
                    if (wV.contains(tile.getWorldLocation()))
                    {
                        if (player.getWorldLocation().distanceTo(tile.getWorldLocation()) < maxDistance)
                        {
                            renderRocks(graphics, tile);
                        }
                    }
                }
            }
        }
        if (config.removeTrees()) {
            for (Tile tile : treeTiles) {
                for(WorldView wV : depletedTrees.keySet())
                {
                    if (wV.contains(tile.getWorldLocation()))
                    {
                        if (player.getWorldLocation().distanceTo(tile.getWorldLocation()) < maxDistance) {
                            renderTrees(graphics, tile);
                        }
                    }
                }
            }
        }

        return null;
    }

        /*
        Leaving this old abomination in as an example of what you don't do. I was tired and frustrated and settled on a
        working brute force method, but that is not the correct way.
         */
            /*
            for (WorldView worldView : depletedTrees.keySet()) {
                for (final ResourceTile rtile : depletedTrees.get(worldView)) {
                    Scene scene = worldView.getScene();
                    Tile[][][] tiles = scene.getTiles();
                    WorldPoint worldPoint = WorldPoint.fromRegion(rtile.regionId, rtile.regionX, rtile.regionY, rtile.z);
                    int z = worldView.getPlane();

                    if (worldPoint.getPlane() != worldView.getPlane())
                    {
                        continue;
                    }

                    for (int x = 0; x < tiles[z].length; ++x)
                    {
                        for (int y = 0; y < tiles[z][x].length; ++y)
                        {
                            Tile tile = tiles[z][x][y];
                            if (tile == null)
                            {
                                continue;
                            }
                            //for some reason just comparing the world points doesn't actually work? I am at a loss and
                            //now have this abomination:
                            if (worldPoint.getX() == tile.getWorldLocation().getX() && worldPoint.getY() == tile.getWorldLocation().getY() && worldPoint.getPlane() == tile.getPlane())
                            {
                                renderTrees(graphics, tile);
                            }
                        }
                    }
                }
            }
        }

             */

    private void renderRocks ( final Graphics2D graphics, Tile tile)
    {
        GameObject[] gameObjects = tile.getGameObjects();
        if (gameObjects != null) {
            for (GameObject gameObject : gameObjects) {
                if (gameObject != null && gameObject.getSceneMinLocation().equals(tile.getSceneLocation())) {
                    if (!config.removeTiles()) {
                        OverlayUtil.renderTileOverlay(graphics, gameObject, "", config.tileColor());
                    }
                    if (config.outline()) {
                        modelOutlineRenderer.drawOutline(gameObject, 3, config.modelOutlineColor(), 2);
                    }
                }
            }
        }
    }

    private void renderTrees ( final Graphics2D graphics, Tile tile)
    {
        GameObject[] gameObjects = tile.getGameObjects();
        if (gameObjects != null) {
            for (GameObject gameObject : gameObjects) {
                if (gameObject != null && gameObject.getSceneMinLocation().equals(tile.getSceneLocation())) {
                    if (!config.removeTiles()) {
                        OverlayUtil.renderTileOverlay(graphics, gameObject, "", config.tileColor());
                    }
                    if (config.outline()) {
                        modelOutlineRenderer.drawOutline(gameObject, 3, config.modelOutlineColor(), 2);
                    }
                }
            }
        }
    }
}
