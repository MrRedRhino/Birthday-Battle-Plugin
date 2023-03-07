package org.pipeman.bb.games.pull_ships;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.pipeman.bb.CoinManager;
import org.pipeman.bb.games.Game;
import org.pipeman.bb.games.GameFlags;
import org.pipeman.bb.games.GameManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PullShipsGame implements Game {
    private static final Location TP_LOCATION = new Location(Bukkit.getWorld("world"), -180, 69, -245.5, 136, 7);
    private static final Location SPAWN = new Location(Bukkit.getWorld("world"), -195, 69, -248).toCenterLocation();
    private static final Location SPAWN_BUTTON = new Location(Bukkit.getWorld("world"), -190, 69, -249);
    private static final BoundingBox BB = new BoundingBox(-167, 60, -273, -203, 82, -233);
    private static final Vector GOAL = new Vector(-175, 64, -260);

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && clickedBlock.getLocation().equals(SPAWN_BUTTON) && !isButtonPressed(clickedBlock)) {
            if (getBoats().size() < 10) {
                Boat boat = SPAWN.getWorld().spawn(SPAWN, Boat.class);
                boat.setBoatType(Boat.Type.OAK);
                boat.setGlowing(true);
                boat.addScoreboardTag(id());
                boat.addPassenger(SPAWN.getWorld().spawn(SPAWN, Marker.class));
                boat.addPassenger(SPAWN.getWorld().spawn(SPAWN, Marker.class));
            } else {
                event.getPlayer().sendActionBar(Component.text(ChatColor.RED + "Zu viele Boote im Spiel."));
            }
        }
    }

    private boolean isButtonPressed(@Nullable Block block) {
        return block instanceof Powerable && ((Powerable) block).isPowered();
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Entity caught = event.getCaught();
        if (!(caught instanceof Boat)) return;

        setLastPulledBy(caught, event.getPlayer());
    }

    @EventHandler
    public void entityMoved(VehicleMoveEvent event) {
        Vehicle boat = event.getVehicle();
        if (boat instanceof Boat && event.getTo().toVector().distanceSquared(GOAL) < 5) {
            Player player = getLastPulledBy(boat);
            if (player != null) CoinManager.get().addCoins(player, 1, true);
            boat.remove();
        }
    }

    private void setLastPulledBy(Entity entity, Player player) {
        entity.getScoreboardTags().removeIf(t -> t.startsWith("last_pulled_by="));
        entity.addScoreboardTag("last_pulled_by=" + player.getUniqueId());
    }

    private @Nullable Player getLastPulledBy(Entity entity) {
        for (String tag : entity.getScoreboardTags()) {
            if (tag.startsWith("last_pulled_by=")) {
                return Bukkit.getPlayer(UUID.fromString(tag.substring(15)));
            }
        }
        return null;
    }

    private List<Boat> getBoats() {
        List<Boat> boats = new ArrayList<>();
        for (Boat boat : SPAWN.getWorld().getEntitiesByClass(Boat.class)) {
            if (boat.getScoreboardTags().contains(id())) boats.add(boat);
        }
        return boats;
    }

    @Override
    public boolean shouldPlayerLeave(Player player) {
        return !BB.contains(player.getLocation().toVector());
    }

    @Override
    public boolean shouldPlayerJoin(Player player) {
        return BB.contains(player.getLocation().toVector());
    }

    @Override
    public Location getTeleportLocation() {
        return TP_LOCATION;
    }

    @Override
    public void leavePlayer(Player player) {
        if (GameManager.getPlayers(id()).size() <= 1) getBoats().forEach(Boat::remove);
    }

    @Override
    public void joinPlayer(Player player) {

    }

    @Override
    public String id() {
        return "pull_ships";
    }

    @Override
    public List<GameFlags> getFlags() {
        return List.of(GameFlags.KEEP_INVENTORY);
    }
}
