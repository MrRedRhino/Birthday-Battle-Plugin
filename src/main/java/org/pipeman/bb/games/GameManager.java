package org.pipeman.bb.games;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;
import org.pipeman.bb.utils.InventoryStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager implements Listener {
    private static final Map<String, Game> games = new HashMap<>();

    public static void registerGame(Game game) {
        games.put(game.id(), game);
    }

    public static void leaveGame(Player player) {
        Game game = getGame(player);
        if (game != null) {
            game.leavePlayer(player);
            if (!game.hasFlag(GameFlags.KEEP_INVENTORY)) {
                InventoryStorage.loadInventory(player);
            }
        }
        player.getScoreboardTags().removeIf(t -> t.startsWith("game="));
    }

    public static @Nullable Game getGame(Player player) {
        for (String tag : player.getScoreboardTags()) {
            if (tag.startsWith("game=")) {
                return games.get(tag.substring(5));
            }
        }
        return null;
    }

    private static @Nullable Game getLastPlayedGame(Player player) {
        for (String tag : player.getScoreboardTags()) {
            if (tag.startsWith("last_game=")) {
                return games.get(tag.substring(10));
            }
        }
        return null;
    }

    public static Game getGame(String id) {
        return games.get(id);
    }

    public static void joinGame(Player player, Game game) {
        leaveGame(player);
        player.addScoreboardTag("game=" + game.id());

        player.getScoreboardTags().removeIf(t -> t.startsWith("last_game="));
        player.addScoreboardTag("last_game=" + game.id());

        if (!game.hasFlag(GameFlags.KEEP_INVENTORY)) {
            InventoryStorage.saveInventory(player);
            player.getInventory().clear();
        }

        game.joinPlayer(player);
    }

    public static boolean isPlayingGame(Player player, String gameID) {
        Game g = getGame(player);
        return g != null && g.id().equals(gameID);
    }


    public static List<Player> getPlayers(String gameID) {
        ArrayList<Player> players = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isPlayingGame(player, gameID)) players.add(player);
        }
        return players;
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        leaveGame(player);

        Game lastPlayedGame = getLastPlayedGame(player);
        if (lastPlayedGame != null) player.teleport(lastPlayedGame.getTeleportLocation());
    }

    @EventHandler
    public void playerDisconnect(PlayerQuitEvent event) {
        leaveGame(event.getPlayer());
    }

    @EventHandler
    public void tick(ServerTickStartEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Game currentGame = getGame(player);
            if (currentGame != null && currentGame.shouldPlayerLeave(player)) leaveGame(player);

            for (Game game : games.values()) {
                if (!isPlayingGame(player, game.id()) && game.shouldPlayerJoin(player)) joinGame(player, game);
            }
        }
    }

    @EventHandler
    public void playerTakeDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Game game = getGame((Player) event.getEntity());
            if (game == null || !game.hasFlag(GameFlags.ALLOW_TAKING_DAMAGE)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void dropItem(PlayerDropItemEvent event) {
        Game game = getGame(event.getPlayer());
        if (game == null || !game.hasFlag(GameFlags.ALLOW_DROPPING_ITEMS)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDealDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Game game = getGame((Player) event.getDamager());
            if (game == null || !game.hasFlag(GameFlags.ALLOW_DEALING_DAMAGE)) {
                event.setCancelled(true);
            }
        }
    }
}
