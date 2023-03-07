package org.pipeman.bb.games;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.pipeman.bb.CoinManager;
import org.pipeman.bb.Main;
import org.pipeman.bb.Messages;
import org.pipeman.bb.utils.Countdown;
import org.pipeman.bb.utils.ItemBuilder;
import org.pipeman.bb.utils.SignTeleporter;

import java.util.List;

public class SpleefGame implements Game {
    private static final Location PLATFORM_START = new Location(Main.getGameWorld(), -30, 76, -247);
    private static final int PLATFORM_SIZE = 49;
    private static final int PLATFORM_HEIGHT = 9;
    private static final Location TP_LOCATION = new Location(Main.getGameWorld(), 30.5, 65, -169, -180, -10);
    private final Countdown countdown = new Countdown(() -> GameManager.getPlayers(id()), this::startGame);

    public SpleefGame() {
        SignTeleporter.registerSign(new Location(
                        Main.getGameWorld(), 30, 67, -179),
                        p -> GameManager.joinGame(p, this)
        );
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
        return TP_LOCATION;
    }

    @Override
    public void leavePlayer(Player player) {
        int playerCount = GameManager.getPlayers(id()).size();
        if (playerCount <= 4) countdown.reset();
    }

    @Override
    public void joinPlayer(Player player) {
        int playerCount = GameManager.getPlayers(id()).size();
        if (playerCount == 1) resetPlatform();

        player.teleport(new Location(Main.getGameWorld(), -5.5, 97, -222.5));
        if (playerCount >= 4) countdown.start();
    }

    private void startGame() {
        ItemStack shovel = new ItemBuilder(Material.NETHERITE_SHOVEL)
                .enchant(Enchantment.DIG_SPEED, 4)
                .setUnbreakable(true)
                .setCanDestroy(Material.SNOW_BLOCK.getKey())
                .build();

        for (Player player : GameManager.getPlayers(id())) {
            player.getInventory().setItem(0, shovel);
        }
    }

    @EventHandler
    public void tick(ServerTickStartEvent event) {
        countdown.tick();

        for (Player player : GameManager.getPlayers(id())) {
            if (player.getLocation().y() < 69) { // NICE
                if (handleDeath(player)) break;
            }
        }
    }

    private boolean handleDeath(Player deceased) {
        GameManager.leaveGame(deceased);
        deceased.teleport(TP_LOCATION);
        List<Player> players = GameManager.getPlayers(id());
        Messages.SPLEEF_PLAYER_DIED.sendTo(players, deceased.getName());
        Messages.SPLEEF_PLAYER_DIED.sendTo(deceased, "Du");

        if (players.size() == 1) {
            Player winner = players.get(0);
            GameManager.leaveGame(winner);
            winner.teleport(TP_LOCATION);
            CoinManager.get().addCoins(winner, 5, true);
            return true;
        }
        return false;
    }

    private static void resetPlatform() {
        int startX = PLATFORM_START.getBlockX();
        int startY = PLATFORM_START.getBlockY();
        int startZ = PLATFORM_START.getBlockZ();
        for (int x = 0; x < PLATFORM_SIZE; x++) {
            for (int z = 0; z < PLATFORM_SIZE; z++) {
                for (int i = 0; i < 3; i++) {
                    PLATFORM_START.getWorld().setType(startX + x, startY + i * PLATFORM_HEIGHT, startZ + z, Material.SNOW_BLOCK);
                }
            }
        }
    }

    @Override
    public String id() {
        return "spleef";
    }
}
