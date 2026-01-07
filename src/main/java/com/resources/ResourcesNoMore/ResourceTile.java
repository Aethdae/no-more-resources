package com.resources.ResourcesNoMore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import net.runelite.api.coords.WorldPoint;


@Value
@EqualsAndHashCode
public class ResourceTile {
    @Getter
    @Setter
    public int regionId;
    @Getter
    @Setter
    public int regionX;
    @Getter
    @Setter
    public int regionY;
    @Getter
    @Setter
    public int z;

    public String toString()
    {
        return regionId + " region, at " + regionX + ", " + regionY + " on plane " + z + ".";
    }

    public ResourceTile(int id, int x, int y, int plane)
    {
        this.regionId = id;
        this.regionX = x;
        this.regionY = y;
        this.z = plane;
    }

}
