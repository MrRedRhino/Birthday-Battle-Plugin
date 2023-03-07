package org.pipeman.bb.games.jungle_arena;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.pipeman.bb.CoinManager;
import org.pipeman.bb.Main;
import org.pipeman.bb.games.Game;
import org.pipeman.bb.games.GameFlags;
import org.pipeman.bb.games.GameManager;
import org.pipeman.bb.utils.Interval;
import org.pipeman.bb.utils.SignTeleporter;
import org.pipeman.bb.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class JungleArenaGame implements Game {
    private static final Location TP_LOCATION = new Location(Bukkit.getWorld("world"), -288.7, 65, 115, -96, 0);
    private static final Vector[] SPAWN_POINTS = {
            new Vector(-264.5, 63, 115.5),
            new Vector(-258.5, 65, 107.5),
            new Vector(-279.5, 65, 109.5),
            new Vector(-268.5, 63, 107.5),
            new Vector(-270.5, 65, 99.5),
            new Vector(-273.5, 65, 95.5),
            new Vector(-263.5, 65, 93.5),
            new Vector(-259.5, 65, 97.5),
            new Vector(-252.5, 66, 118.5),
            new Vector(-251.5, 70, 117.5),
            new Vector(-252.5, 73, 118.5)
    };
    private static final EntityType[] ENTITIES = {
            EntityType.SKELETON,
            EntityType.ZOMBIE,
            EntityType.SPIDER
    };
    private final Interval interval = new Interval(200);
    private final int MOB_CAP = 20;

    public JungleArenaGame() {
        SignTeleporter.registerSign(
                new Location(Bukkit.getWorld("world"), -285,66,114),
                player -> GameManager.joinGame(player, this)
        );
        SignTeleporter.registerSign(
                new Location(Bukkit.getWorld("world"), -265, 66, 119),
                player -> GameManager.joinGame(player, this)
        );
    }

    @Override
    public boolean shouldPlayerLeave(Player player) {
        return player.getLocation().toBlockLocation().toVector().setY(0)
                .distanceSquared(new Vector(-261.5, 0, 88.5)) < 1;
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
        player.teleport(TP_LOCATION);
        if (GameManager.getPlayers(id()).size() <= 1) {
            getGameEntities().forEach(Entity::remove);
        }
    }

    @Override
    public void joinPlayer(Player player) {
        player.teleport(new Location(Bukkit.getWorld("world"), -264.5, 63, 115.5, 165, -5));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 70, 2));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (((Entity) player).isOnGround()) {
            player.removePotionEffect(PotionEffectType.SLOW_FALLING);
        }
    }

    @EventHandler
    public void tick(ServerTickStartEvent event) {
        if (interval.tick() && GameManager.getPlayers(id()).size() > 0 && getGameEntities().size() < MOB_CAP) {
            Location location = Utils.getRandomValue(SPAWN_POINTS).toLocation(Main.getGameWorld());
            EntityType type = Utils.getRandomValue(ENTITIES);
            location.getWorld().spawnEntity(location, type)
                    .addScoreboardTag(id());
        }
    }

    @EventHandler
    public void onDamage(EntityDeathEvent event) {
        if (event.getEntity().getScoreboardTags().contains(id())) {
            Player killer = event.getEntity().getKiller();
            if (killer != null) {
                event.setDroppedExp(0);
                event.getDrops().clear();
                CoinManager.get().addCoins(killer, 1, true);
            }
        }
    }

    private List<Entity> getGameEntities() {
        List<Entity> entities = new ArrayList<>();
        for (Entity entity : Main.getGameWorld().getEntities()) {
            if (entity.getScoreboardTags().contains(id())) {
                entities.add(entity);
            }
        }
        return entities;
    }

    @Override
    public String id() {
        return "jungle_arena";
    }

    @Override
    public List<GameFlags> getFlags() {
        return List.of(GameFlags.KEEP_INVENTORY, GameFlags.ALLOW_TAKING_DAMAGE, GameFlags.ALLOW_DEALING_DAMAGE);
    }
}
