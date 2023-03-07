package org.pipeman.bb.games;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.List;

public interface Game extends Listener {
    boolean shouldPlayerLeave(Player player);

    boolean shouldPlayerJoin(Player player);

    Location getTeleportLocation();

    void leavePlayer(Player player);

    void joinPlayer(Player player);

    String id();

    default List<GameFlags> getFlags() {
        return Collections.emptyList();
    }

    default boolean hasFlag(GameFlags flag) {
        return getFlags().contains(flag);
    }
}
