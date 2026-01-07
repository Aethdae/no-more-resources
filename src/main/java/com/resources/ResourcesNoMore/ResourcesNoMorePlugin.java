package com.resources.ResourcesNoMore;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;

import joptsimple.internal.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;

import java.util.*;

import net.runelite.api.events.*;
import net.runelite.api.coords.LocalPoint;
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
    @Getter(AccessLevel.PACKAGE)
    public final Multimap<WorldView, ResourceTile> depletedTrees = ArrayListMultimap.create();

    @Getter(AccessLevel.PACKAGE)
    public final Multimap<WorldView, ResourceTile> depletedRocks = ArrayListMultimap.create();

    private static final String CONFIG_GROUP = "resourceGroundMarker";
    private static final String ROCK_GROUP = "_Rock";
    private static final String TREE_GROUP = "_Tree";
    private static final String REGION_PREFIX = "resourceRegion_";

    boolean canAddTree = false;
    boolean canAddOre = false;

    @Inject
    private Gson gson;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ConfigManager configManager;

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
        loadTrees();
        loadRocks();
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("RNM is shutting down!");
        overlayManager.remove(overlay);
        depletedTrees.clear();
        depletedRocks.clear();
	}

    @Subscribe
    public void onWorldViewLoaded(WorldViewLoaded event)
    {
        loadTrees(event.getWorldView());
        loadRocks(event.getWorldView());
    }

    @Subscribe
    public void onWorldViewUnloaded(WorldViewUnloaded event)
    {
        depletedRocks.removeAll(event.getWorldView());
        depletedTrees.removeAll(event.getWorldView());
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
    public void onGameObjectDespawned(GameObjectDespawned event) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        final GameObject object = event.getGameObject();

        int regionID = object.getWorldLocation().getRegionID();
        Tile tile = event.getTile();

        if (canAddOre)
        {
            canAddOre = false;
            List<ResourceTile> depletedRockCollection = new ArrayList<>(getDepletedRocksInRegion(regionID));

            ResourceTile resourceTile = new ResourceTile(regionID, tile.getWorldLocation().getRegionX(), tile.getWorldLocation().getRegionY(), tile.getPlane());
            if (!depletedRockCollection.contains(resourceTile))
            {
                depletedRockCollection.add(resourceTile);
            }

            log.debug(resourceTile.toString());
            String json = gson.toJson(depletedRockCollection);
            configManager.setConfiguration(CONFIG_GROUP + ROCK_GROUP, REGION_PREFIX + regionID, json);
            loadRocks();
        }

        if (canAddTree)
        {
            canAddTree = false;
            List<ResourceTile> depletedTreeCollection = new ArrayList<>(getDepletedTreesInRegion(regionID));

            ResourceTile resourceTile = new ResourceTile(regionID, tile.getWorldLocation().getRegionX(), tile.getWorldLocation().getRegionY(), tile.getPlane());

            if (!depletedTreeCollection.contains(resourceTile)) {
                depletedTreeCollection.add(resourceTile);
            }
            log.debug(tile.toString());
            String json = gson.toJson(depletedTreeCollection);
            configManager.setConfiguration(CONFIG_GROUP + TREE_GROUP, REGION_PREFIX + regionID, json);
            loadTrees();
        }
    }

    TileObject getTileObject(WorldView worldView, int x, int y, LocalPoint localPoint)
    {
        int offset = worldView.getId() == WorldView.TOPLEVEL ? (Constants.EXTENDED_SCENE_SIZE - Constants.SCENE_SIZE) / 2 : 0;
        x += offset;
        y += offset;
        Scene scene = worldView.getScene();
        Tile[][][] tiles = scene.getTiles();
        //log.debug("localpoint: " + localPoint.getX() + ", " +  localPoint.getY());
        Tile tile = tiles[worldView.getPlane()][53][58];
        if (tile != null)
        {
            for (GameObject gameObject : tile.getGameObjects())
            {
                if (gameObject != null)
                {
                    return gameObject;
                }
            }

            GroundObject groundObject = tile.getGroundObject();
            if (groundObject != null)
            {
                return groundObject;
            }
        }
        return null;
    }

    void loadTrees()
    {
        depletedTrees.clear();

        WorldView wv = client.getTopLevelWorldView();
        if (wv == null)
        {
            return;
        }

        loadTrees(wv);

        for (WorldEntity we : wv.worldEntities())
        {
            loadTrees(we.getWorldView());
        }
    }

    void loadTrees(WorldView wv)
    {
        depletedTrees.removeAll(wv);

        int[] regions = wv.getMapRegions();
        if (regions == null)
        {
            return;
        }

        for (int regionId : regions)
        {
            // load points for region
            log.debug("Loading points for region {}", regionId);
            Collection<ResourceTile> trees = getDepletedTreesInRegion(regionId);
            depletedTrees.putAll(wv, trees);
        }
    }
    void loadRocks()
    {
        depletedRocks.clear();

        WorldView wv = client.getTopLevelWorldView();
        if (wv == null)
        {
            return;
        }

        loadRocks(wv);

        for (WorldEntity we : wv.worldEntities())
        {
            loadRocks(we.getWorldView());
        }
    }

    void loadRocks(WorldView wv)
    {
        depletedRocks.removeAll(wv);

        int[] regions = wv.getMapRegions();
        if (regions == null)
        {
            return;
        }

        for (int regionId : regions)
        {
            // load points for region
            log.debug("Loading points for region {}", regionId);
            Collection<ResourceTile> rocks = getDepletedRocksInRegion(regionId);
            depletedRocks.putAll(wv, rocks);
        }
    }

    Collection<ResourceTile> getDepletedTreesInRegion(int regionId)
    {
        String json = configManager.getConfiguration(CONFIG_GROUP + TREE_GROUP, REGION_PREFIX + regionId);
        if (Strings.isNullOrEmpty(json)) {
            return Collections.emptyList();
        }

        return gson.fromJson(json, new  TypeToken<List<ResourceTile>>(){}.getType());
    }
    Collection<ResourceTile> getDepletedRocksInRegion(int regionId)
    {
        String json = configManager.getConfiguration(CONFIG_GROUP + ROCK_GROUP, REGION_PREFIX + regionId);
        if (Strings.isNullOrEmpty(json)) {
            return Collections.emptyList();
        }
        return gson.fromJson(json, new TypeToken<List<ResourceTile>>(){}.getType());
    }

    @Provides
    ResourcesNoMoreConfig provideConfig (ConfigManager configManager)
    {
        return configManager.getConfig(ResourcesNoMoreConfig.class);
    }
//
//    private ArrayList<Tile> getTiles (Tile tile,int sizeX, int sizeY)
//    {
//        ArrayList<Tile> tiles = new ArrayList<Tile>();
//        tiles.add(tile);
//
//        LocalPoint lP = tile.getLocalLocation();
//        if (lP == null) {
//            return null;
//        }
//
//        for (int x = 0; x < sizeX; x++) {
//            for (int y = 0; y < sizeY; y++) {
//                int finalX = x;
//                int finalY = y;
//
//                Tile newTile = new Tile() {
//                    @Override
//                    public DecorativeObject getDecorativeObject() {
//                        return null;
//                    }
//
//                    @Override
//                    public GameObject[] getGameObjects() {
//                        return new GameObject[0];
//                    }
//
//                    @Override
//                    public ItemLayer getItemLayer() {
//                        return null;
//                    }
//
//                    @Override
//                    public GroundObject getGroundObject() {
//                        return null;
//                    }
//
//                    @Override
//                    public void setGroundObject(GroundObject groundObject) {
//
//                    }
//
//                    @Override
//                    public WallObject getWallObject() {
//                        return null;
//                    }
//
//                    @Override
//                    public SceneTilePaint getSceneTilePaint() {
//                        return null;
//                    }
//
//                    @Override
//                    public void setSceneTilePaint(SceneTilePaint paint) {
//
//                    }
//
//                    @Override
//                    public SceneTileModel getSceneTileModel() {
//                        return null;
//                    }
//
//                    @Override
//                    public void setSceneTileModel(SceneTileModel model) {
//
//                    }
//
//                    @Override
//                    public WorldPoint getWorldLocation() {
//                        return new WorldPoint(tile.getWorldLocation().getX() + finalX, tile.getWorldLocation().getY() + finalY, tile.getPlane());
//                    }
//
//                    @Override
//                    public Point getSceneLocation() {
//                        return null;
//                    }
//
//                    @Override
//                    public LocalPoint getLocalLocation() {
//                        return LocalPoint.fromWorld(client.getWorldView(-1), this.getWorldLocation());
//                    }
//
//                    @Override
//                    public int getPlane() {
//                        return 0;
//                    }
//
//                    @Override
//                    public int getRenderLevel() {
//                        return 0;
//                    }
//
//                    @Override
//                    public List<TileItem> getGroundItems() {
//                        return List.of();
//                    }
//
//                    @Override
//                    public Tile getBridge() {
//                        return null;
//                    }
//                };
//                log.debug(newTile.getWorldLocation().toString());
//                tiles.add(newTile);
//            }
//        }
//        return tiles;
//    }
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

//client.getLocalPlayer().getWorldView().getScene().removeGameObject(tile.);

//manager.drawTile(client.getLocalPlayer().getWorldView().getScene(), tile);
//manager.addEntity(object.getRenderable(), false);
//client.getWorldView(-1).getScene().removeGameObject(object);
//client.getLocalPlayer().getWorldView().getScene().removeGameObject(object);
        /*
        switch (object.sizeX())
        {
            case ObjectID.TREE:
            case ObjectID.TREE2:
            case ObjectID.TREE3:
            case ObjectID.TREE4:
            case ObjectID.TREE5:
                treeTiles = getTiles(tile, 2, 2);
                break;
            case ObjectID.OAKTREE:
            case ObjectID.OAK_TREE_1:
            case ObjectID.OAK_TREE_2:
            case ObjectID.OAK_TREE_3:
                treeTiles = getTiles(tile, 3, 3);
                break;
        }
        */