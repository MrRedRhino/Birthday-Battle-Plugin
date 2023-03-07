package org.pipeman.bb;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.pipeman.bb.utils.Utils;

import static org.bukkit.ChatColor.GREEN;

public class CoinManager {
    private static final CoinManager INSTANCE = new CoinManager();

    public int getCoins(Player player) {
        return getOrCreateScore(player).getScore();
    }

    public void addCoins(Player player, int amount, boolean sendFeedback) {
        if (sendFeedback && amount > 0) {
            if (amount == 1) Messages.COIN_EARNED.sendTo(player);
            else Messages.COINS_EARNED.sendTo(player, amount);
            Utils.playSound(player, Sound.ENTITY_PLAYER_LEVELUP);
        }

        Score score = getOrCreateScore(player);
        score.setScore(score.getScore() + amount);
    }

    public void removeCoins(Player player, int amount) {
        Score score = getOrCreateScore(player);
        score.setScore(Math.max(score.getScore() - amount, 0));
    }

    private static Score getOrCreateScore(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective("coins");
        return (objective == null ? scoreboard.registerNewObjective("coins", Criteria.DUMMY, Component.text(GREEN + "Münzen")) : objective).getScore(player);
    }

    public static CoinManager get() {
        return INSTANCE;
    }
}
