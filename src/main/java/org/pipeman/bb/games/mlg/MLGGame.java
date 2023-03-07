package org.pipeman.bb.games.mlg;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.pipeman.bb.CoinManager;
import org.pipeman.bb.Messages;
import org.pipeman.bb.games.Game;
import org.pipeman.bb.games.GameManager;
import org.pipeman.bb.utils.ItemBuilder;
import org.pipeman.bb.utils.Utils;

public class MLGGame implements Game {
    private static final BoundingBox GAME_BOUNDARY = new BoundingBox(34, 65, 104, 45, 140, 100);
    private static final Vector GREEN_BUTTON = new Location(null, 36, 65, 100).toVector();
    private static final Vector YELLOW_BUTTON = new Location(null, 40, 65, 100).toVector();
    private static final Vector RED_BUTTON = new Location(null, 44, 65, 100).toVector();

    @EventHandler
    public void tick(ServerTickStartEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!GameManager.isPlayingGame(player, id())) continue;

            Vector position = player.getLocation().toVector();
            if (!GAME_BOUNDARY.contains(position) || position.getY() < 66.1f) {
                fail(player);
            }
        }
    }

    private void fail(Player player) {
        GameManager.leaveGame(player);
        Utils.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO);
        Messages.MLG_FAIL.sendTo(player);
    }

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        Vector pos = clickedBlock.getLocation().toVector();

        if (pos.equals(GREEN_BUTTON)) startGame(player, 36.5f, 97, 102.5f, Material.LIME_WOOL);
        if (pos.equals(YELLOW_BUTTON)) startGame(player, 40.5f, 113, 102.5f, Material.YELLOW_WOOL);
        if (pos.equals(RED_BUTTON)) startGame(player, 44.5f, 136, 102.5f, Material.RED_WOOL);
    }

    @EventHandler
    public void placeWater(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();

        if (GameManager.isPlayingGame(player, id())) {
            giveCoins(player, event.getBlockClicked().getType());
            event.setCancelled(true);
            GameManager.leaveGame(player);
        }
    }

    private void giveCoins(Player player, Material material) {
        int coins = switch (material) {
            case LIME_WOOL -> 1;
            case YELLOW_WOOL -> 2;
            case RED_WOOL -> 3;
            default -> 0;
        };
        if (coins > 0) CoinManager.get().addCoins(player, coins, true);
    }

    private void startGame(Player player, float x, float y, float z, Material color) {
        GameManager.joinGame(player, this);
        ItemStack item = new ItemBuilder(Material.WATER_BUCKET).setCanPlaceOn(color.getKey()).build();
        player.getInventory().setItem(0, item);
        player.teleport(new Location(player.getWorld(), x, y, z));
    }

    @Override
    public boolean shouldPlayerLeave(Player player) {
        return false;
    }

    @Override
    public boolean shouldPlayerJoin(Player player) {
        return false;
    }

    @Override
    public Location getTeleportLocation() {
        return new Location(Bukkit.getWorld("world"), 40.5, 65, 111.5, -180, 0);
    }

    @Override
    public void leavePlayer(Player player) {
        player.getInventory().remove(Material.WATER_BUCKET);
    }

    @Override
    public void joinPlayer(Player player) {

    }

    @Override
    public String id() {
        return "mlg";
    }
}
