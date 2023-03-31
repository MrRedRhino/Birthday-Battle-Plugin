package org.pipeman.bb.games.boat_race;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.util.Vector;
import org.pipeman.bb.CoinManager;
import org.pipeman.bb.Main;
import org.pipeman.bb.Messages;
import org.pipeman.bb.games.Game;
import org.pipeman.bb.games.GameManager;
import org.pipeman.bb.utils.Countdown;

import java.util.ArrayList;
import java.util.List;

public class BoatRaceGame implements Game {
    private static final Location TP_LOCATION = new Location(Main.getGameWorld(), -223.5, 65.5, -239, 160, 0);
    private static final Vector CHECKPOINT = new Vector(79, 0, 234); // 79, 65, 234
    private static final Cell[] CELLS = {
            new Cell(new Location(Main.getGameWorld(), -232, 65, -255)),
            new Cell(new Location(Main.getGameWorld(), -232, 65, -261)),
            new Cell(new Location(Main.getGameWorld(), -232, 65, -267)),
            new Cell(new Location(Main.getGameWorld(), -232, 65, -249))
    };
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;
    private final Countdown countdown = new Countdown(() -> GameManager.getPlayers(id()), this::startGame);
    private static final String BOAT_TAG = "boat_race_boat";
    private static final String CHECKPOINT_TAG = "checkpoint";
    private static boolean isOn;
    private static int finishedPlayers = 0;

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
        if (!isOn && GameManager.getPlayers(id()).size() <= MIN_PLAYERS) {
            countdown.reset();
        }
    }

    @Override
    public void joinPlayer(Player player) {
        int size = GameManager.getPlayers(id()).size();
        System.out.println(size);
        if (size > MIN_PLAYERS - 1) {
            countdown.start();
        }
    }

    private void startGame() {
        for (Cell cell : CELLS) {
            cell.setOpen(true);
        }
        for (Boat boat : getBoats()) {
            if (boat.getPassengers().size() == 0) boat.remove();
        }
        isOn = true;
    }

    public static void resetGame() {
        isOn = false;
        finishedPlayers = 0;

        for (Boat boat : getBoats()) {
            boat.remove();
            for (Entity passenger : boat.getPassengers()) {
                passenger.teleport(TP_LOCATION);
            }
        }

        for (Cell cell : CELLS) {
            cell.spawnBoat(BOAT_TAG);
            cell.setOpen(false);
        }
    }

    @EventHandler
    public void enterBoat(VehicleEnterEvent event) {
        if (GameManager.getPlayers(id()).size() < MAX_PLAYERS && event.getVehicle().getScoreboardTags().contains(BOAT_TAG)) {
            if (event.getEntered() instanceof Player player) {
                GameManager.joinGame(player, this);
            }
        }
    }

    @EventHandler
    public void exitBoat(VehicleExitEvent event) {
        if (event.getExited() instanceof Player player) {
            boolean isInGameBoat = event.getVehicle().getScoreboardTags().contains(BOAT_TAG);
            if (isOn) {
                if (isInGameBoat && GameManager.isPlayingGame(player, id())) {
                    event.setCancelled(true);
                }
            } else {
                if (isInGameBoat) GameManager.leaveGame(player);
            }
        }
    }

    @EventHandler
    public void tick(ServerTickStartEvent event) {
        countdown.tick();
        List<Boat> boats = getBoats();

        for (Boat boat : boats) {
            if (isInCheckpoint(boat.getLocation())) {
                boat.addScoreboardTag(CHECKPOINT_TAG);
            }
        }

        for (Cell cell : CELLS) {
            for (Boat boat : cell.getBoatsInside(boats)) {
                if (!boat.getScoreboardTags().contains(CHECKPOINT_TAG)) continue;
                checkForFinish(boat);
            }
        }
    }

    private void checkForFinish(Boat boat) {
        for (Entity passenger : boat.getPassengers()) {
            if (!(passenger instanceof Player player)) continue;

            player.teleport(TP_LOCATION);
            boat.remove();
            GameManager.leaveGame(player);
            finishedPlayers++;

            if (finishedPlayers == 1) {
                CoinManager.get().addCoins(player, 4, true);
                Messages.BOAT_RACE_WINNER.sendTo(player);
            } else {
                Messages.BOAT_RACE_PLACE.sendTo(player, finishedPlayers);
            }

            if (getBoats().size() == 0) {
                resetGame();
            }
        }
    }

    private static List<Boat> getBoats() {
        List<Boat> boats = new ArrayList<>();
        for (Boat boat : TP_LOCATION.getWorld().getEntitiesByClass(Boat.class)) {
            if (boat.getScoreboardTags().contains(BOAT_TAG)) {
                boats.add(boat);
            }
        }
        return boats;
    }

    @Override
    public String id() {
        return "boat_race";
    }

    private static boolean isInCheckpoint(Location pos) {
        return pos.toVector().setY(0).distanceSquared(CHECKPOINT) < 50;
    }
}
