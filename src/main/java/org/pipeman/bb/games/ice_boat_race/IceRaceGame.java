package org.pipeman.bb.games.ice_boat_race;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.BoundingBox;
import org.pipeman.bb.CoinManager;
import org.pipeman.bb.Main;
import org.pipeman.bb.Messages;
import org.pipeman.bb.games.Game;
import org.pipeman.bb.games.GameManager;
import org.pipeman.bb.utils.Countdown;
import org.pipeman.bb.utils.SignTeleporter;
import org.pipeman.bb.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class IceRaceGame implements Game {
    private static final Location BOAT_1 = new Location(Main.getGameWorld(), -83.0, 65, -164.5, 180, 0);
    private static final Location BOAT_2 = new Location(Main.getGameWorld(), -88.0, 65, -164.5, 180, 0);
    private static final Location TP_LOCATION = new Location(Main.getGameWorld(), -88, 65, -155.7, -150, 8);
    private static final Location JOIN_TP = new Location(Main.getGameWorld(), -85.5, 65, -164.5, 180, 0);
    private static final BoundingBox BOX = new BoundingBox(268, 60, -157, -97, 85, -320);
    private final Countdown countdown = new Countdown(() -> GameManager.getPlayers(id()), this::startGame);
    private List<Boat> boats = new ArrayList<>();
    private boolean acceptWinners = false;

    public IceRaceGame() {
        SignTeleporter.registerSign(new Location(Main.getGameWorld(), -86, 66, -160), this::tryToJoinPlayer);
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
        return TP_LOCATION;
    }

    @Override
    public void leavePlayer(Player player) {
        countdown.reset();

        Entity vehicle = player.getVehicle();
        if (vehicle != null) vehicle.remove();
    }

    @Override
    public void joinPlayer(Player player) {
        player.teleport(JOIN_TP);
        if (GameManager.getPlayers(id()).size() == 2) {
            countdown.start();
            boats = new ArrayList<>();
            spawnBoats();
        } else {
            Messages.WAITING_FOR_PLAYER.sendTo(player);
        }
    }

    @EventHandler
    public void tick(ServerTickStartEvent event) {
        countdown.tick();

        for (Player player : GameManager.getPlayers(id())) {
            Location location = player.getLocation();
            if (location.z() > -161.6 && Utils.between(location.x(), 239, 256) && acceptWinners) {
                acceptWinners = false;
                gameEnded(player);
            }
        }
    }

    private void gameEnded(Player winner) {
        for (Boat boat : boats) {
            boat.remove();
        }

        Main.scheduleTask(100, () -> {
            for (Player player : GameManager.getPlayers(id())) {
                player.teleport(TP_LOCATION);
                GameManager.leaveGame(player);
            }
        });

        CoinManager.get().addCoins(winner, 3, true);
        Messages.BOAT_RACE_WINNER.sendTo(winner);
        for (Player player : GameManager.getPlayers(id())) {
            if (player != winner) {
                Messages.BOAT_RACE_PLACE.sendTo(player, 2);
                break;
            }
        }
    }

    private void tryToJoinPlayer(Player player) {
        if (GameManager.getPlayers(id()).size() != 2) {
            GameManager.joinGame(player, this);
        } else {
            Messages.GAME_ALREADY_RUNNING.sendTo(player);
        }
    }

    private void startGame() {
        for (Boat boat : boats) {
            Entity vehicle = boat.getVehicle();
            if (vehicle != null) vehicle.remove();
            boat.leaveVehicle();
        }
        acceptWinners = true;
    }

    private void spawnBoats() {
        List<Player> players = GameManager.getPlayers(id());
        spawnBoat(players.get(0), BOAT_1);
        spawnBoat(players.get(1), BOAT_2);
    }

    private void spawnBoat(Player player, Location location) {
        World world = Main.getGameWorld();
        ArmorStand stand = world.spawn(location.clone().subtract(0, 1.45, 0), ArmorStand.class);
        stand.setGravity(false);
        stand.setInvisible(true);

        Boat boat = world.spawn(location, Boat.class);
        boat.addPassenger(player);
        stand.addPassenger(boat);

        boats.add(boat);
    }

    @Override
    public String id() {
        return "ice_boat_race";
    }
}
