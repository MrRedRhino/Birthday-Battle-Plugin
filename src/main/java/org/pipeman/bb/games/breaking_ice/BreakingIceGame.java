package org.pipeman.bb.games.breaking_ice;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.pipeman.bb.CoinManager;
import org.pipeman.bb.Messages;
import org.pipeman.bb.games.Game;
import org.pipeman.bb.games.GameManager;
import org.pipeman.bb.utils.Countdown;
import org.pipeman.bb.utils.SignTeleporter;
import org.pipeman.bb.utils.Utils;

import java.util.List;

public class BreakingIceGame implements Game {
    private static final BoundingBox GAME_AREA = new BoundingBox(59, 68, -233, 99, 62, -198);
    private static final Location TP_LOCATION = new Location(Bukkit.getWorld("world"), 74, 65, -190.5, 180, 0);
    private static final Location CENTER = new Location(Bukkit.getWorld("world"), 77.5, 66, -216.5);
    private static final BoundingBox ICE_BOX = new BoundingBox(61, 64, -231, 97, 64, -200);
    private final Countdown countdown = new Countdown(() -> GameManager.getPlayers(id()), this::startGame);
    private boolean isRunning = false;

    public BreakingIceGame() {
        SignTeleporter.registerSign(
                new Location(Bukkit.getWorld("world"), 73, 66, -195),
                this::tryToJoinPlayer
        );
    }

    @Override
    public boolean shouldPlayerLeave(Player player) {
        return !GAME_AREA.contains(player.getLocation().toVector());
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
        if (GameManager.getPlayers(id()).size() <= 2) {
            countdown.reset();
            stopGame();
        }
    }

    @Override
    public void joinPlayer(Player player) {
        System.out.println(GameManager.getPlayers(id()).size());
        if (GameManager.getPlayers(id()).size() >= 2) {
            countdown.start();
        }
        fillIce();
        player.teleport(CENTER);
    }

    private void startGame() {
        isRunning = true;
    }

    private void stopGame() {
        isRunning = false;
    }

    private void fillIce() {
        int y = (int) ICE_BOX.getCenterY();
        World world = TP_LOCATION.getWorld();

        for (int x = (int) ICE_BOX.getMinX(); x <= ICE_BOX.getMaxX(); x++) {
            for (int z = (int) ICE_BOX.getMinZ(); z <= ICE_BOX.getMaxZ(); z++) {
                if (world.getType(x, y, z) == Material.WATER) {
                    world.setType(x, y, z, Material.ICE);
                }
            }
        }
    }

    @EventHandler
    public void tick(ServerTickStartEvent event) {
        countdown.tick();
    }

    @EventHandler
    public void onMelt(BlockFadeEvent event) {
        Vector position = event.getBlock().getLocation().toVector();
        if (Utils.containsIgnoringY(ICE_BOX, position.getBlockX(), position.getBlockZ()) && !isRunning) {
            event.setCancelled(true);
        }
    }

    public void tryToJoinPlayer(Player player) {
        if (!isRunning) GameManager.joinGame(player, this);
        else Messages.GAME_ALREADY_RUNNING.sendActionbar(player);
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getTo().getY() < 64.7 && GameManager.isPlayingGame(player, id())) {
            GameManager.leaveGame(player);
            player.sendMessage(Component.text(ChatColor.RED + "Du bist ins Wasser gefallen!"));
            player.teleport(TP_LOCATION);

            List<Player> players = GameManager.getPlayers(id());
            if (players.size() == 1) {
                Player winner = players.get(0);
                winner.sendMessage(Component.text(ChatColor.GREEN + "Du hast das Breaking-Ice gewonnen!"));
                CoinManager.get().addCoins(winner, 3, true);
                winner.teleport(TP_LOCATION);
                GameManager.leaveGame(winner);
            }
        }
    }

    @Override
    public String id() {
        return "breaking_ice";
    }
}
