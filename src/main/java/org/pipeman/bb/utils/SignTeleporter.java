package org.pipeman.bb.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class SignTeleporter implements Listener {
    private static final Map<Location, Action> signs = new HashMap<>();

    @EventHandler
    public void rightClick(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        Action action = signs.get(block.getLocation());
        if (action != null) action.run(event.getPlayer());
    }

    public static void registerSign(Location signPos, Location teleportPos, Consumer<Player> afterTeleportAction) {
        signs.put(signPos, new Action(teleportPos, afterTeleportAction));
    }

    public static void registerSign(Location signPos, Location teleportPos) {
        signs.put(signPos, new Action(teleportPos, null));
    }

    public static void registerSign(Location signPos, Consumer<Player> afterTeleportAction) {
        signs.put(signPos, new Action(null, afterTeleportAction));
    }

    private record Action(Location teleportPos, Consumer<Player> afterTeleportAction) {
        private void run(Player player) {
            if (teleportPos != null) player.teleport(teleportPos);
            if (afterTeleportAction != null) afterTeleportAction.accept(player);
        }
    }
}
