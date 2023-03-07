package org.pipeman.bb.games.mine;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.util.BoundingBox;
import org.pipeman.bb.CoinManager;
import org.pipeman.bb.Main;
import org.pipeman.bb.games.Game;
import org.pipeman.bb.games.GameFlags;
import org.pipeman.bb.games.GameManager;
import org.pipeman.bb.utils.StructureStorage;
import org.pipeman.bb.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class MineGame implements Game {
    private static final int X_SIZE = 31;
    private static final int Y_SIZE = 29;
    private static final int Z_SIZE = 31;
    private static final Location LOCATION = new Location(Bukkit.getWorld("world"), 15, 33, -91);
    private static final BoundingBox BREAK_BOX = new BoundingBox(LOCATION.x(),
            LOCATION.y(),
            LOCATION.z(),
            LOCATION.x() + X_SIZE,
            LOCATION.y() + Y_SIZE,
            LOCATION.z() + Z_SIZE);
    private static final Material[] ores = {
            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.REDSTONE_ORE,
            Material.EMERALD_ORE,
            Material.LAPIS_ORE,
            Material.DIAMOND_ORE,
            Material.COPPER_ORE
    };

    private static final Location TP_LOCATION = new Location(Bukkit.getWorld("world"), 56.5, 65, -82.5, 90, 0);

    private static final BoundingBox BOX = new BoundingBox(46, 64, -60, 14, 32, -92);

    public MineGame() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.INSTANCE, this::resetMine, 1_728_000, 1_728_000);
    }

    public static void storeMine() throws IOException {
        OutputStream stream = Files.newOutputStream(Path.of("mine.wsf"), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        StructureStorage.saveStructure(stream, LOCATION, X_SIZE, Y_SIZE, Z_SIZE);
    }

    public void resetMine() {
        try {
            InputStream stream = Files.newInputStream(Path.of("mine.wsf"), StandardOpenOption.READ);
            StructureStorage.loadStructure(stream, LOCATION);
        } catch (IOException ignored) {
        }
    }

    @Override
    public boolean shouldPlayerLeave(Player player) {
        return !BOX.contains(player.getLocation().toVector());
    }

    @Override
    public boolean shouldPlayerJoin(Player player) {
        return BOX.contains(player.getLocation().toVector());
    }

    @Override
    public Location getTeleportLocation() {
        return TP_LOCATION;
    }

    @Override
    public void leavePlayer(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
    }

    @Override
    public void joinPlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
    }

    @Override
    public String id() {
        return "mine";
    }

    @EventHandler
    public void blockBroken(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!GameManager.isPlayingGame(player, id())) return;

        if (!BREAK_BOX.contains(event.getBlock().getLocation().toVector())) {
            event.setCancelled(true);
        } else {
            event.setDropItems(false);
            event.setExpToDrop(0);
            if (Utils.contains(ores, event.getBlock().getType())) {
                CoinManager.get().addCoins(player, 1, true);
            }
        }
    }

    @Override
    public List<GameFlags> getFlags() {
        return List.of(GameFlags.KEEP_INVENTORY);
    }
}
