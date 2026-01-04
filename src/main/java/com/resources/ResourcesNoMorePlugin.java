package com.resources;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.callback.Hooks;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Resources No More"
)
public class ResourcesNoMorePlugin extends Plugin
{

    boolean canAddTree = false;
    boolean canAddOre = false;

    @Getter
    private Set<Tile> depletedTrees = new HashSet<>();

    @Getter
    private Set<Tile> depletedOres = new HashSet<>();

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private Client client;

	@Inject
	private ResourcesNoMoreConfig config;

    @Inject ResourcesNoMoreOverlay overlay;


	@Override
	protected void startUp() throws Exception
	{
		log.debug("Loading at start of RNM...");
        overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("RNM is shutting down!");
        overlayManager.remove(overlay);
        depletedOres.clear();
        depletedTrees.clear();
	}

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.HOPPING)
        {
            depletedOres.clear();
            depletedTrees.clear();
        }
        if (event.getGameState() == GameState.LOADING)
        {
            depletedOres.clear();
            depletedTrees.clear();
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event)
    {
        var chatType = event.getType();
        if (chatType != ChatMessageType.GAMEMESSAGE && chatType != ChatMessageType.SPAM
        && chatType != ChatMessageType.MESBOX)
        {
            return;
        }

        final String message = event.getMessage();
        if (message.contains("You manage to mine"))
        {
            canAddOre = true;
        }

        if (message.contains("You get"))
        {
            canAddTree = true;
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event)
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        final GameObject object = event.getGameObject();

        log.debug(object.sizeX() + " " + object.sizeY());
        Tile tile = event.getTile();

        log.debug(tile.getLocalLocation().toString());

        if (canAddOre)
        {
            depletedOres.add(tile);
            canAddOre = false;
        }

        if (canAddTree)
        {
            Tile tile2 = new Tile() {
                @Override
                public DecorativeObject getDecorativeObject() {
                    return null;
                }

                @Override
                public GameObject[] getGameObjects() {
                    return new GameObject[0];
                }

                @Override
                public ItemLayer getItemLayer() {
                    return null;
                }

                @Override
                public GroundObject getGroundObject() {
                    return null;
                }

                @Override
                public void setGroundObject(GroundObject groundObject) {

                }

                @Override
                public WallObject getWallObject() {
                    return null;
                }

                @Override
                public SceneTilePaint getSceneTilePaint() {
                    return null;
                }

                @Override
                public void setSceneTilePaint(SceneTilePaint paint) {

                }

                @Override
                public SceneTileModel getSceneTileModel() {
                    return null;
                }

                @Override
                public void setSceneTileModel(SceneTileModel model) {

                }

                @Override
                public WorldPoint getWorldLocation() {
                    return new WorldPoint(tile.getWorldLocation().getX(), tile.getWorldLocation().getY() + 1, tile.getPlane());
                }

                @Override
                public Point getSceneLocation() {
                    return null;
                }

                @Override
                public LocalPoint getLocalLocation() {
                    return LocalPoint.fromWorld(client.getWorldView(-1), this.getWorldLocation());
                }

                @Override
                public int getPlane() {
                    return 0;
                }

                @Override
                public int getRenderLevel() {
                    return 0;
                }

                @Override
                public List<TileItem> getGroundItems() {
                    return List.of();
                }

                @Override
                public Tile getBridge() {
                    return null;
                }
            };
            depletedTrees.add(tile);
            depletedTrees.add(tile2);
            canAddTree = false;
        }

        for (Tile tiles : depletedTrees)
        {
            log.debug("I am a tree at: " + tiles.getLocalLocation());
        }
        //log.debug(depletedGroundObjects.size() + "Before");
        //depletedGroundObjects.add(tile.getGroundObject());
        //log.debug(depletedGroundObjects.size() + "After");
//        {
//            if (hey != null) {
//                log.debug("I've got: " + hey.getHash());
//            }
//        }

        //if (client.getLocalPlayer().getWorldView().isTopLevel() && tile.getGameObjects()[0] != null)
        {
            //client.getLocalPlayer().getWorldView().getScene().removeGameObject(tile.);
        }
        //manager.drawTile(client.getLocalPlayer().getWorldView().getScene(), tile);
        //manager.addEntity(object.getRenderable(), false);
        //client.getWorldView(-1).getScene().removeGameObject(object);
        //client.getLocalPlayer().getWorldView().getScene().removeGameObject(object);
    }
    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }
        /*

        final GameObject object = event.getGameObject();
        Tile tile = event.getTile();
        final WorldPoint location = object.getWorldLocation();
        if (depletedTiles.contains(tile))
        {
            switch (object.getId())
            {
                case ObjectID.OAKTREE:
                    log.debug("I'm a depleted oak at " + object.getWorldLocation().getX() + "  " + object.getWorldLocation().getY() + "  ");
                case ObjectID.TREE:
                    log.debug("I'm a depleted tree 1276 at " + object.getWorldLocation().getX() + "  " + object.getWorldLocation().getY() + "  ");
            }
            log.debug("I'm a depleted node");
        }
        else {
            return;
            //nada atm
        }

        */
    }

	@Provides
    ResourcesNoMoreConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ResourcesNoMoreConfig.class);
	}

    public Set<Tile> getDepletedTrees() {
        return depletedTrees;
    }

    public Set<Tile> getDepletedOres(){
        return depletedOres;
    }
}
