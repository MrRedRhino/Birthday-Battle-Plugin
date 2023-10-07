package org.pipeman.bb.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.pipeman.bb.Messages;
import org.pipeman.bb.games.Game;
import org.pipeman.bb.games.GameManager;
import org.pipeman.bb.utils.DontMoveActionManager;

public class TeleportCommands implements Listener {
    private static final Location SPAWN = new Location(Bukkit.getWorld("world"), 52, 298, 136, 90, 0).toCenterLocation();
    private static final Location SHOP = new Location(Bukkit.getWorld("world"), 21, 66, 150, 141, 4).toCenterLocation();
    private static final int DELAY = 3_000;

    public static final CommandExecutor SHOP_EXECUTOR = (sender, command, label, args) -> scheduleTP(SHOP, sender);
    public static final CommandExecutor SPAWN_EXECUTOR = (sender, command, label, args) -> scheduleTP(SPAWN, sender);
    public static final CommandExecutor LEAVE_EXECUTOR = (sender, command, label, args) -> {
        if (sender instanceof Player player) {
            Game game = GameManager.getGame(player);
            if (game == null) {
                player.sendMessage(ChatColor.RED + "Du spielst gerade kein Spiel!");
                return true;
            }
            DontMoveActionManager.scheduleAction(player, DELAY, p -> {
                GameManager.leaveGame(p);
                p.teleport(game.getTeleportLocation());
            });
            player.sendMessage(ChatColor.GREEN + "Du wirst in 3 Sekunden aus dem Spiel geworfen. Bewege dich, um den Vorgang abzubrechen.");
        }
        return true;
    };

    private static boolean scheduleTP(Location loc, CommandSender sender) {
        if (!(sender instanceof Player player)) return false;
        DontMoveActionManager.scheduleAction(player, DELAY, p -> {
            p.teleport(loc);
            GameManager.leaveGame(player);
        });
        Messages.TELEPORT_SOON.sendTo(player);
        return true;
    }
}
