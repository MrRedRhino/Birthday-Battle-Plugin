package org.pipeman.bb.utils;

import org.bukkit.util.Vector;

public class CenteredRectangle {
    private final float xCenter;
    private final float zCenter;
    private final float xSize;
    private final float zSize;

    public CenteredRectangle(float xCenter, float zCenter, float xSize, float zSize) {
        this.xCenter = xCenter;
        this.zCenter = zCenter;
        this.xSize = xSize;
        this.zSize = zSize;
    }

    public boolean contains(Vector pos) {
        return contains(pos.getX(), pos.getZ());
    }

    public boolean contains(double x, double z) {
        return Math.abs(xCenter - x) < xSize && Math.abs(zCenter - z) < zSize;
    }

    public float getxCenter() {
        return xCenter;
    }

    public float getzCenter() {
        return zCenter;
    }
}
