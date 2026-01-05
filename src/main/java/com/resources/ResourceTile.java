package com.resources;

import lombok.EqualsAndHashCode;
import lombok.Value;

import javax.annotation.Nullable;
import java.awt.*;

@Value
@EqualsAndHashCode
public class ResourceTile {
    private int regionId;
    private int regionX;
    private int regionY;
    private int z;
}
