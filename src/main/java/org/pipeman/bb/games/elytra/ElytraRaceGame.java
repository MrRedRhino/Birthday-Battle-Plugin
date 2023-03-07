package org.pipeman.bb.games.elytra;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.pipeman.bb.CoinManager;
import org.pipeman.bb.Messages;
import org.pipeman.bb.games.Game;
import org.pipeman.bb.games.GameManager;
import org.pipeman.bb.utils.ItemBuilder;

public class ElytraRaceGame implements Game {
    private static final BoundingBox START = new BoundingBox(492, 103, -328, 488, 105, -322);
    private static final Location TP_LOCATION = new Location(Bukkit.getWorld("world"), 490.5, 104, -330.5, 0, 0);
    private static final BoundingBox GOAL = new BoundingBox(413, 121, 419, 439, 155, 446);

    private static final Ring[] RINGS = {
            ring(490, 108, -290, 4),
            ring(474, 117, -250, 4),
            ring(460, 122, -221, 4),
            ring(442, 123, -180, 4),
            ring(432, 127, -131, 4),
            ring(429, 127, -93, 4),
            ring(422, 125, -57, 4),
            ring(418, 124, -33, 3),
            ring(406, 125, -2, 4),
            ring(391, 129, 29, 4),
            ring(377, 137, 57, 4),
            ring(366, 146, 83, 4),
            ring(352, 155, 116, 4),
            ring(344, 155, 152, 4),
            ring(341, 156, 184, 3),
            ring(334, 155, 207, 4),
            ring(343, 156, 245, 3),
            ring(347, 154, 274, 4),
            ring(351, 152, 305, 3),
            ring(357, 154, 327, 4),
            ring(379, 154, 362, 4),
            ring(396, 150, 386, 4),
            ring(413, 148, 407, 4)
    };

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!GameManager.isPlayingGame(player, id())) return;

        Location to = event.getTo();
        Location from = event.getFrom();

        if (GOAL.contains(to.toVector())) {
            GameManager.leaveGame(player);
            CoinManager.get().addCoins(player, 3, true);
            return;
        }

        for (int i = 0; i < RINGS.length; i++) {
            Ring ring = RINGS[i];
            if (ring.isBetween(from, to))
                if (ring.hasMissed(from, to)) {
                    Messages.ELYTRA_RING_MISSED.sendTo(player, i + 1);
                    GameManager.leaveGame(player);
                    player.teleport(getTeleportLocation());
                } else {
                    Messages.ELYTRA_RING_PASSED.sendTo(player, i + 1);
                }
        }
    }

    @Override
    public boolean shouldPlayerLeave(Player player) {
        Vector pVev = player.getLocation().toVector();
        return ((Entity) player).isOnGround() && !START.contains(pVev) && !GOAL.contains(pVev);
    }

    @Override
    public boolean shouldPlayerJoin(Player player) {
        return START.contains(player.getLocation().toVector());
    }

    @Override
    public Location getTeleportLocation() {
        return TP_LOCATION;
    }

    @Override
    public void leavePlayer(Player player) {
        player.getInventory().remove(Material.ELYTRA);
        player.getInventory().setChestplate(null);
        player.getInventory().remove(Material.FIREWORK_ROCKET);
    }

    @Override
    public void joinPlayer(Player player) {
        player.getInventory().setItem(EquipmentSlot.CHEST, new ItemStack(Material.ELYTRA));
        player.getInventory().setItem(0, new ItemBuilder(Material.FIREWORK_ROCKET).setAmount(10).build());
    }

    @Override
    public String id() {
        return "elytra";
    }

    private static Ring ring(int x, int y, int z, int radius) {
        Vector center = new Location(null, x, y, z).toCenterLocation().toVector();
        center.setZ(center.getZ() - 0.5);
        return new Ring(center, radius);
    }

    private static class Ring {
        private final Vector center;
        private final int radius;

        private Ring(Vector center, int radius) {
            this.center = center;
            this.radius = radius;
        }

        private boolean isBetween(Location from, Location to) {
            return from.z() <= center.getZ() && center.getZ() <= to.z();
        }

        private Vector getIntersection(Location from, Location to) {
            double multiplier = (center.getZ() - from.z()) / (to.z() - from.z());
            Vector movement = to.toVector().subtract(from.toVector());
            movement.multiply(multiplier);
            return from.toVector().add(movement);
        }

        private boolean hasMissed(Location from, Location to) {
            Vector intersection = getIntersection(from, to);
            double distX = Math.abs(intersection.getX() - center.getX());
            double distY = Math.abs(intersection.getY() - center.getY());
            return distX > radius || distY > radius;
        }
    }
}
