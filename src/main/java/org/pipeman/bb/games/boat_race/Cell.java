package org.pipeman.bb.games.boat_race;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Boat;
import org.pipeman.bb.utils.CenteredRectangle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Cell {
    private final Location pos;
    private final CenteredRectangle boundingBox;

    public Cell(Location pos) {
        this.pos = pos;
        int posX = (int) pos.getX();
        int posZ = (int) pos.getZ();
        boundingBox = new CenteredRectangle(posX + 2, posZ + 2.5f, 1, 2.5f);
    }

    public void setOpen(boolean open) {
        World world = pos.getWorld();

        for (int i = 0; i < 5; i++) {
            BlockState state = world.getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ() + i).getState();
            if (state.getBlockData() instanceof Openable openable) {
                openable.setOpen(open);
                state.setBlockData(openable);
                state.update();
            }
        }
    }

    public List<Boat> getBoatsInside(Collection<Boat> allBoats) {
        ArrayList<Boat> boats = new ArrayList<>();
        for (Boat boat : allBoats) {
            if (boundingBox.contains(boat.getLocation().toVector())) {
                boats.add(boat);
            }
        }
        return boats;
    }

    public void spawnBoat(String tag) {
        World world = pos.getWorld();
        Location location = new Location(world, boundingBox.getxCenter(), 65, boundingBox.getzCenter(), 90, 0);
        Boat boat = world.spawn(location, Boat.class);
        boat.setBoatType(Boat.Type.SPRUCE);
        boat.addScoreboardTag(tag);
    }
}
