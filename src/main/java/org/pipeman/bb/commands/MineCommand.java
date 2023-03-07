package org.pipeman.bb.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.pipeman.bb.games.mine.MineGame;

public class MineCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) return false;

        try {
            if (args[0].equals("save")) {
                MineGame.storeMine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
