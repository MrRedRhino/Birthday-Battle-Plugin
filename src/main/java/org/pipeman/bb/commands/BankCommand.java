package org.pipeman.bb.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.pipeman.bb.CoinManager;
import org.pipeman.bb.Messages;
import org.pipeman.bb.games.GameManager;
import org.pipeman.bb.utils.ItemBuilder;
import org.pipeman.bb.utils.Utils;

import java.util.Collections;
import java.util.List;

public class BankCommand {
    public static CommandExecutor executor = (sender, command, label, args) -> {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if (args.length < 1) {
            Messages.BANK_COMMAND_USAGE.sendTo(player);
            return false;
        }

        PlayerInventory inventory = player.getInventory();
        if (args[0].equalsIgnoreCase("einzahlen")) {
            int removedAmount = 0;
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() == Material.SUNFLOWER) {
                    removedAmount += item.getAmount();
                    inventory.setItem(i, null);
                }
            }

            CoinManager.get().addCoins(player, removedAmount, false);
            Messages.COINS_DEPOSITED.sendTo(player, String.valueOf(removedAmount));
        } else if (args[0].equalsIgnoreCase("auszahlen")) {
            if (args.length < 2) {
                Messages.BANK_COMMAND_USAGE.sendTo(player);
                return false;
            }

            if (GameManager.getGame(player) != null) {
                Messages.BANK_COMMAND_IN_GAME.sendTo(player);
                return true;
            }

            int amount = Utils.parseInt(args[1]).orElse(-1);
            if (amount < 1) {
                player.sendMessage(ChatColor.RED + "Ungültige Menge");
                return false;
            }

            int toRemove = Math.min(64, Math.min(amount, CoinManager.get().getCoins(player)));

            CoinManager.get().removeCoins(player, toRemove);
            Messages.COINS_PAYED_OFF.sendTo(player, String.valueOf(toRemove));
            inventory.addItem(new ItemBuilder(Material.SUNFLOWER).setAmount(toRemove).build());

            return false;
        } else {
            Messages.BANK_COMMAND_USAGE.sendTo(player);
        }

        return false;
    };

    public static TabCompleter completer = (sender, command, label, args) -> {
        if (args.length == 1) {
            if (!args[0].isEmpty() && "einzahlen".startsWith(args[0])) {
                return List.of("einzahlen");
            }
            if (!args[0].isEmpty() && "auszahlen".startsWith(args[0])) {
                return List.of("auszahlen");
            }

            return List.of("einzahlen", "auszahlen");
        }
        return Collections.emptyList();
    };
}
