package org.pipeman.bb.games.cactus;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.pipeman.bb.CoinManager;
import org.pipeman.bb.Main;
import org.pipeman.bb.Messages;
import org.pipeman.bb.games.Game;
import org.pipeman.bb.games.GameFlags;
import org.pipeman.bb.games.GameManager;
import org.pipeman.bb.utils.Countdown;
import org.pipeman.bb.utils.ItemBuilder;
import org.pipeman.bb.utils.SignTeleporter;
import org.pipeman.bb.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CactusTowerGame implements Game {
    private static final BoundingBox BOX = new BoundingBox(-79, 61, -57, -67, 317, -69);
    private static final Location TP = new Location(Main.getGameWorld(), -72.5, 62, -52.7, -180, 8);
    private static final Location INNER_TP = new Location(Main.getGameWorld(), -72.5, 62, -58.4, -180, 3);
    private final Countdown countdown = new Countdown(() -> GameManager.getPlayers(id()), this::startGame);
    private final Countdown gameTimer = new Countdown(Collections::emptyList, this::gameEnded);
    private List<Player> players;

    private void gameEnded() {
        int highest = 0;
        Player bestPlayer = null;

        for (int i = 0; i < players.size(); i++) {
            int height = 0;
            World world = Main.getGameWorld();
            Vector pos = TOWERS[i];
            while (world.getBlockAt(pos.getBlockX(), height + 63, pos.getBlockZ()).getType() == Material.CACTUS) {
                height++;
            }

            Player player = players.get(i);
            player.sendMessage("Dein Turm ist " + height + " Blöcke hoch");
            removeTower(height, pos);

            if (height > highest) {
                highest = height;
                bestPlayer = player;
            }
        }

        players.forEach(player -> player.teleport(TP));

        if (bestPlayer != null) {
            CoinManager.get().addCoins(bestPlayer, 4, true);
            Messages.WIN.sendTo(bestPlayer);

            ArrayList<Player> players1 = new ArrayList<>(players);
            players1.remove(bestPlayer);
            Messages.WINNER.sendTo(players1, bestPlayer.getName());
        }
    }

    private void removeTower(int height, Vector pos) {
        for (int i = height - 1; i >= 0; i--) {
            Main.getGameWorld().getBlockAt(pos.getBlockX(), pos.getBlockY() + i, pos.getBlockZ()).setType(Material.AIR);
        }
    }

    private static final Random RANDOM = new Random();

    private void startGame() {
        List<Player> players = GameManager.getPlayers(id());
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            player.teleport(TOWERS[i].toLocation(Main.getGameWorld()).toCenterLocation());
            player.getInventory().setItem(4, getCactusStack());
        }
        this.players = players;
        gameTimer.start(30);
        gameTimer.setCountdownConsumer(integer -> players.forEach(p -> p.sendMessage(integer.toString())));
    }

    private static final Vector[] TOWERS = {
            new Vector(-71, 63, -61),
            new Vector(-71, 63, -65),
            new Vector(-75, 63, -65),
            new Vector(-75, 63, -61)
    };

    public CactusTowerGame() {
        SignTeleporter.registerSign(Utils.loc(-73, 63, -57), this::tryToJoinPlayer);
    }

    private void tryToJoinPlayer(Player player) {
        if (GameManager.getPlayers(id()).size() == 4) {
            Messages.GAME_ALREADY_RUNNING.sendTo(player);
        } else {
            GameManager.joinGame(player, this);
        }
    }

    @Override
    public boolean shouldPlayerLeave(Player player) {
        return !BOX.contains(player.getLocation().toVector());
    }

    @Override
    public boolean shouldPlayerJoin(Player player) {
        return false;
    }

    @Override
    public Location getTeleportLocation() {
        return TP;
    }

    @Override
    public void leavePlayer(Player player) {

    }

    @Override
    public void joinPlayer(Player player) {
        player.teleport(INNER_TP);
        int playerCunt = GameManager.getPlayers(id()).size();
        if (playerCunt == 1) Messages.WAITING_FOR_PLAYER.sendTo(player);
        if (playerCunt > 1) countdown.start();
    }

    @EventHandler
    public void tick(ServerTickStartEvent event) {
        countdown.tick();
        gameTimer.tick();
    }

    @EventHandler
    public void onDamageTaken(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player && GameManager.isPlayingGame(player, id())) {
            event.setDamage(0);
        }
    }

    @EventHandler
    public void placeBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (GameManager.isPlayingGame(player, id())) {
            player.getInventory().setItem(RANDOM.nextInt(9), getCactusStack());
        }
    }

    @Override
    public String id() {
        return "cactus_tower";
    }

    @Override
    public List<GameFlags> getFlags() {
        return List.of(GameFlags.ALLOW_TAKING_DAMAGE);
    }

    private static ItemStack getCactusStack() {
        return new ItemBuilder(Material.CACTUS)
                .setCanPlaceOn(Material.RED_SAND.getKey(), Material.CACTUS.getKey())
                .build();
    }
}
