package org.pipeman.bb.games.shooting_ranges;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.pipeman.bb.games.shooting_ranges.ShootingGame;

public class JungleShooting extends ShootingGame {
    private static final BoundingBox BOW_BOX = new BoundingBox(-203, 74, 104, -187, 81, 83);
    private static final Location SPAWN = new Location(Bukkit.getWorld("world"), -166.5, 73, 60.5);
    private static final Location END = new Location(Bukkit.getWorld("world"), -161, 73, 101);
    private static final Location TP_LOCATION = new Location(Bukkit.getWorld("world"), -187.5, 65, 98, 139, -9);

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
        return "jungle_shooting";
    }

    @Override
    public int getReward() {
        return 2;
    }
}
