package org.pipeman.bb.games.shooting_ranges;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public class DarkOakShooting extends ShootingGame {
    private static final BoundingBox BOW_BOX = new BoundingBox(307, 69, -56, 297, 73, -66);
    private static final Location TP_LOCATION = new Location(Bukkit.getWorld("world"), 301.3, 65, -49.2, -170, -12);
    private static final Location SPAWN = new Location(Bukkit.getWorld("world"), 330, 70, -83);
    private static final Location END = new Location(Bukkit.getWorld("world"), 273, 70, -3);

    @Override
    public BoundingBox getBowBox() {
        return BOW_BOX;
    }

    @Override
    public Location getMinecartSpawn() {
        return SPAWN;
    }

    @Override
    public Location getMinecartEnd() {
        return END;
    }

    @Override
    public Location getTpLocation() {
        return TP_LOCATION;
    }

    @Override
    public String getGameID() {
        return "dark_oak_shooting";
    }

    @Override
    public int getReward() {
        return 4;
    }
}
