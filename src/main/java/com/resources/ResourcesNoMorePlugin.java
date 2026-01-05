package com.resources;

import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;

import java.lang.reflect.Array;
import java.util.*;

import net.runelite.api.gameval.ObjectID;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.groundmarkers.GroundMarkerPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Resources No More"
)
public class ResourcesNoMorePlugin extends Plugin
{
    private static final String CONFIG_GROUP = "resourceGroundMarker";
    private static final String REGION_PREFIX = "resourceRegion_";

    boolean canAddTree = false;
    boolean canAddOre = false;

    private Collection<ResourceTile> depletedOreCollection = new Collection<>() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<ResourceTile> iterator() {
            return null;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(ResourceTile resourceTile) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends ResourceTile> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }
    };

    private Collection<ResourceTile> depletedTreeCollection = new Collection<>() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<ResourceTile> iterator() {
            return null;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(ResourceTile resourceTile) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends ResourceTile> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }
    };

    private Set<Tile> depletedTrees = new HashSet<>();

    private Set<Tile> depletedOres = new HashSet<>();

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

        int regionID = object.getWorldLocation().getRegionID();
        log.debug("Object size: " + object.sizeX() + " " + object.sizeY());
        Tile tile = event.getTile();

        log.debug("Object location: " + tile.getLocalLocation().toString());
        log.debug("Object ID: " + object.getId());

        if (canAddOre)
        {
            depletedOres.add(tile);
            canAddOre = false;
        }

        ArrayList<Tile> treeTiles = new ArrayList<>();
        treeTiles = getTiles(tile, object.sizeX(), object.sizeY());

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


        if (canAddTree)
        {
            if (treeTiles != null) {
                depletedTrees.addAll(treeTiles);
            }
            else
            {
                depletedTrees.add(tile);
            }
            canAddTree = false;
            //Text.toCSV(depletedTrees.toString());
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

    private ArrayList<Tile> getTiles(Tile tile, int sizeX, int sizeY)
    {
        ArrayList<Tile> tiles = new ArrayList<Tile>();
        tiles.add(tile);

        LocalPoint lP = tile.getLocalLocation();
        if (lP == null)
        {
            return null;
        }

        for (int x = 0; x < sizeX; x++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                int finalX = x;
                int finalY = y;

                Tile newTile = new Tile() {
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
                    return new WorldPoint(tile.getWorldLocation().getX() + finalX, tile.getWorldLocation().getY() + finalY, tile.getPlane());
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
                log.debug(newTile.getWorldLocation().toString());
                tiles.add(newTile);
            }
        }
        return tiles;
    }
}
