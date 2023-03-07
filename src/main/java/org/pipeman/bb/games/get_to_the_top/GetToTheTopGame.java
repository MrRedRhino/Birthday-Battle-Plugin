package org.pipeman.bb.games.get_to_the_top;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.pipeman.bb.CoinManager;
import org.pipeman.bb.Main;
import org.pipeman.bb.games.Game;
import org.pipeman.bb.games.GameManager;
import org.pipeman.bb.utils.ItemBuilder;

public class GetToTheTopGame implements Game {
    private static final BoundingBox BOUNDING_BOX = new BoundingBox(-183, 63, 179, -318, 15, 107);
    private static final Location SPAWN = new Location(Bukkit.getWorld("world"), -211, 35, 158, 96, -4);
    private static final Location TP_LOC = new Location(Bukkit.getWorld("world"), -193.5, 65, 155.5, 87, 11);
    public static final String ID = "get_to_top";

    @Override
    public boolean shouldPlayerLeave(Player player) {
        return !BOUNDING_BOX.contains(player.getLocation().toVector());
    }

    @Override
    public boolean shouldPlayerJoin(Player player) {
        return false;
    }

    @Override
    public Location getTeleportLocation() {
        return TP_LOC;
    }

    @Override
    public void leavePlayer(Player player) {
        player.getInventory().remove(Material.OAK_DOOR);
        player.getInventory().remove(Material.GREEN_BED);
    }

    @Override
    public void joinPlayer(Player player) {
        Main.scheduleTask(10, () -> {
            if (!GameManager.isPlayingGame(player, id())) return;

            ItemStack leaveItem = new ItemBuilder(Material.OAK_DOOR)
                    .setName("Leave")
                    .build();

            ItemStack respawnItem = new ItemBuilder(Material.GREEN_BED)
                    .setName("Reset")
                    .build();

            player.getInventory().setItem(1, leaveItem);
            player.getInventory().setItem(0, respawnItem);
        });
    }

    @EventHandler
    public void useItem(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (item == null || !GameManager.isPlayingGame(player, id())) return;

        if (item.getType() == Material.OAK_DOOR) {
            GameManager.leaveGame(player);
            player.teleport(TP_LOC);
        } else if (item.getType() == Material.GREEN_BED) {
            player.teleport(SPAWN);
        }
    }

    @EventHandler
    public void move(PlayerMoveEvent event) {
        Location pos = event.getTo();
        Player player = event.getPlayer();
        if (!GameManager.isPlayingGame(player, id())) return;

        if (pos.getWorld().getBlockAt(pos.clone().subtract(0, 1, 0)).getType() == Material.GOLD_BLOCK) {
            GameManager.leaveGame(player);
            player.teleport(TP_LOC);
            CoinManager.get().addCoins(player, 6, true);
        }
    }

    @Override
    public String id() {
        return ID;
    }
}
