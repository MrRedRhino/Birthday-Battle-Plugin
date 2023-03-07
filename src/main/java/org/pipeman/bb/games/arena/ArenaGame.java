package org.pipeman.bb.games.arena;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.pipeman.bb.CoinManager;
import org.pipeman.bb.Main;
import org.pipeman.bb.Messages;
import org.pipeman.bb.games.Game;
import org.pipeman.bb.games.GameFlags;
import org.pipeman.bb.games.GameManager;
import org.pipeman.bb.utils.Countdown;
import org.pipeman.bb.utils.SignTeleporter;

import java.util.List;

public class ArenaGame implements Game {
    private static final Location TP_LOCATION = new Location(Main.getGameWorld(), 193.5, 65, 160.5, -90, -14);
    private static final Location[] SPAWNS = {
            new Location(Main.getGameWorld(), 297, 65, 220.5, -180, 0),
            new Location(Main.getGameWorld(), 297, 65, 112.5, 0, 0)
    };
    private final Countdown countdown = new Countdown(() -> GameManager.getPlayers(id()), this::startGame);
    private boolean isRunning = false;

    public ArenaGame() {
        SignTeleporter.registerSign(new Location(Main.getGameWorld(), 271, 72, 160), this::tryToJoinPlayer);
    }

    private void tryToJoinPlayer(Player player) {
        int players = GameManager.getPlayers(id()).size();
        if (players < 2) {
            GameManager.joinGame(player, this);
            player.teleport(SPAWNS[players]);

            System.out.println(players);
            if (players == 1) countdown.start();
        } else {
            Messages.GAME_ALREADY_RUNNING.sendActionbar(player);
        }
    }

    private void startGame() {
        List<Player> players = GameManager.getPlayers(id());
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            player.teleport(SPAWNS[i]);
        }
        isRunning = true;
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
        if (isRunning) {
            countdown.reset();
            for (Player player1 : GameManager.getPlayers(id())) {
                if (player1 != player) {
                    Messages.ARENA_GAME_OPPONENT_LEFT.sendTo(player1);
                }
            }
        }
    }

    @EventHandler
    public void tick(ServerTickStartEvent event) {
        countdown.tick();
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player && GameManager.isPlayingGame((Player) entity, id())) {
            List<Player> players = GameManager.getPlayers(id());
            Player winner = players.get(1 - players.indexOf(entity));
            Messages.ARENA_GAME_WINNER.sendTo(winner, winner.getName());
            Messages.ARENA_GAME_WINNER.sendTo(entity, winner.getName());

            for (Player player : players) {
                GameManager.leaveGame(player);
            }
            Main.scheduleTask(40, () -> {
                winner.teleport(TP_LOCATION);
                isRunning = false;
            });
            CoinManager.get().addCoins(winner, 4, true);
        }
    }

    @Override
    public void joinPlayer(Player player) {

    }

    @Override
    public String id() {
        return "arena";
    }

    @Override
    public List<GameFlags> getFlags() {
        return List.of(GameFlags.KEEP_INVENTORY, GameFlags.ALLOW_DEALING_DAMAGE, GameFlags.ALLOW_TAKING_DAMAGE);
    }
}
