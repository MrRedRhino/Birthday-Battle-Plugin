package org.pipeman.bb;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.List;

import static org.bukkit.ChatColor.*;

public enum Messages {
    TELEPORT_CANCELLED(RED + BOLD.toString() + "Du hast dich bewegt! Dein Teleport wurde abgebrochen."),
    TELEPORT_SOON(GREEN + "Du wirst in 3 sekunden teleportiert. Bewege dich solange nicht!"),

    MLG_FAIL(RED + BOLD.toString() + "Leider nicht geschafft!"),

    COINS_DEPOSITED(GREEN + "{0} Münzen wurden eingezahlt."),
    COINS_PAYED_OFF(GREEN + "{0} Münzen wurden ausgezahlt."),
    COINS_EARNED(GREEN + String.valueOf(BOLD) + "Du hast {0} Münzen erhalten!"),
    COIN_EARNED(GREEN + String.valueOf(BOLD) + "Du hast 1 Münze erhalten!"),
    BANK_COMMAND_USAGE("Benutzung: /bank einzahlen/auszahlen <Menge>"),
    BANK_COMMAND_IN_GAME(RED + "Du kannst keine Münzen auszahlen, während du in einem Spiel bist."),

    ELYTRA_RING_MISSED(RED + String.valueOf(BOLD) + "Du hast den {0}. Ring verfehlt!"),
    ELYTRA_RING_PASSED(GREEN + String.valueOf(BOLD) + "Du hast den {0}. Ring durchflogen!"),

    GAME_ALREADY_RUNNING(RED + "Das Spiel läuft bereits. Warte, bis das Spiel zu ende ist"),

    ARENA_GAME_OPPONENT_LEFT(ChatColor.RED + "Dein Gegner hat das Spiel verlassen."),
    ARENA_GAME_WINNER("{0} hat das Match gewonnen!"),

    BOAT_RACE_WINNER(GREEN + "Du hast das Bootrennen gewonnen!"),
    BOAT_RACE_PLACE(GREEN + "Du bist {0}. Platz"),

    SPLEEF_PLAYER_DIED(RED + "{0} ist ins Wasser gefallen!"),

    WAITING_FOR_PLAYER(GREEN + "Das Spiel beginnt, sobald ein weiterer Mitspieler beitritt."),

    WIN(GREEN + "Du hast das Spiel gewonnen!"),
    WINNER(GREEN + "{0} hat das Spiel gewonnen!");

    private final String text;

    Messages(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }

    public void sendTo(Audience audience, Object... args) {
        audience.sendMessage(Component.text(MessageFormat.format(text(), args)));
    }

    public void sendTo(List<Player> players, Object... args) {
        for (Audience player : players) sendTo(player, args);
    }

    public void sendActionbar(Audience audience, Object... args) {
        audience.sendActionBar(Component.text(MessageFormat.format(text(), args)));
    }
}
