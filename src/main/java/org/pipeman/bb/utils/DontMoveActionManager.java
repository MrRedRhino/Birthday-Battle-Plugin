package org.pipeman.bb.utils;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.pipeman.bb.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DontMoveActionManager implements Listener {
    private static final List<ScheduledDontMoveAction> actions = new ArrayList<>();

    public static void scheduleAction(Player player, long delay, Consumer<Player> action) {
        actions.add(new ScheduledDontMoveAction(player, System.currentTimeMillis() + delay, action));
    }

    @EventHandler
    public void tick(ServerTickStartEvent event) {
        List<ScheduledDontMoveAction> toRemove = new ArrayList<>();
        for (ScheduledDontMoveAction action : actions) {
            if (action.canRun()) {
                action.run();
                toRemove.add(action);
            }
        }
        actions.removeAll(toRemove);
    }

    @EventHandler
    public void playerMoved(PlayerMoveEvent event) {
        if (!event.hasChangedPosition()) return;

        List<ScheduledDontMoveAction> toRemove = new ArrayList<>();
        for (ScheduledDontMoveAction action : actions) {
            if (action.getPlayer().equals(event.getPlayer())) {
                toRemove.add(action);
                Messages.TELEPORT_CANCELLED.sendTo(event.getPlayer());
            }
        }
        actions.removeAll(toRemove);
    }

    public static class ScheduledDontMoveAction {
        private final Player player;
        private final long timestamp;
        private final Consumer<Player> action;

        public ScheduledDontMoveAction(Player player, long timestamp, Consumer<Player> action) {
            this.player = player;
            this.timestamp = timestamp;
            this.action = action;
        }

        public boolean canRun() {
            return System.currentTimeMillis() >= timestamp;
        }

        public void run() {
            action.accept(player);
        }

        public Player getPlayer() {
            return player;
        }
    }
}
