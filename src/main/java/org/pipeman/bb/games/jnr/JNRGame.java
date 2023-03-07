package org.pipeman.bb.games.jnr;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.pipeman.bb.CoinManager;

public class JNRGame implements Listener {
    private static final Vector GOAL = new Vector(48, 107, 139);
    private static final Location TELEPORT_LOCATION = new Location(Bukkit.getWorld("world"), 55, 65, 125);

    @EventHandler
    public void move(PlayerMoveEvent event) {
        if (event.hasChangedBlock() && event.getTo().toBlockLocation().toVector().equals(GOAL)) {
            Player player = event.getPlayer();

            player.teleport(TELEPORT_LOCATION);
            CoinManager.get().addCoins(player, 5, true);
        }
    }
}
