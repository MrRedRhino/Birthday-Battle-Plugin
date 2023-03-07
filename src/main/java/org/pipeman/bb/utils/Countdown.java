package org.pipeman.bb.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Supplier;

public class Countdown {
    private final Supplier<List<Player>> players;
    private final Runnable callback;
    private int ticksLeft = -1;

    public Countdown(Supplier<List<Player>> players, Runnable callback) {
        this.players = players;
        this.callback = callback;
    }

    public void tick() {
        if (ticksLeft >= 0) {
            if (ticksLeft == 0) callback.run();
            if (ticksLeft % 20 == 0) {
                showCountdownMessage(ticksLeft / 20);
            }
            ticksLeft--;
        }
    }

    private void showCountdownMessage(int seconds) {
        String message = getCountdownMessage(seconds);
        if (message != null) {
            Title title = Title.title(Component.empty(), Component.text(message), Title.DEFAULT_TIMES);
            players.get().forEach(p -> p.showTitle(title));
        }
    }

    private String getCountdownMessage(int seconds) {
        return switch (seconds) {
            case 0 -> ChatColor.GREEN + "GO!";
            case 1 -> ChatColor.RED + "1";
            case 2 -> ChatColor.RED + "2";
            case 3 -> ChatColor.RED + "3";
            case 4 -> ChatColor.RED + "4";
            case 5 -> ChatColor.RED + "5";
            case 10 -> ChatColor.RED + "10";
            default -> null;
        };
    }

    public void reset() {
        ticksLeft = -1;
    }

    public void start() {
        ticksLeft = 200;
    }

    public boolean isRunning() {
        return ticksLeft > 0;
    }
}
