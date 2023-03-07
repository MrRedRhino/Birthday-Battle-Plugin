package org.pipeman.bb.games.shooting_ranges;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.pipeman.bb.CoinManager;
import org.pipeman.bb.games.Game;
import org.pipeman.bb.games.GameManager;
import org.pipeman.bb.utils.Interval;
import org.pipeman.bb.utils.ItemBuilder;

public abstract class ShootingGame implements Game {
    private static final Interval interval = new Interval(70);

    public abstract BoundingBox getBowBox();

    public abstract Location getMinecartSpawn();

    public abstract Location getMinecartEnd();

    public abstract Location getTpLocation();

    public abstract String getGameID();

    public abstract int getReward();


    @EventHandler
    public void tick(ServerTickStartEvent event) {
        World world = getMinecartSpawn().getWorld();
        if (isAPlayerPlaying()) {
            if (interval.tick()) {
                Minecart minecart = world.spawn(getMinecartSpawn(), Minecart.class);
                minecart.addPassenger(world.spawn(getMinecartSpawn(), Zombie.class));
            }
        }

        for (Minecart cart : world.getEntitiesByClass(Minecart.class)) {
            if (cart.getLocation().distanceSquared(getMinecartEnd()) < 2) {
                cart.getPassengers().forEach(Entity::remove);
                cart.remove();
            }
        }
    }

    @EventHandler
    public void damageEntity(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;

        Player player = (Player) event.getEntity().getShooter();
        if (!GameManager.isPlayingGame(player, id())) return;

        Entity hitEntity = event.getHitEntity();
        if (hitEntity instanceof Minecart) {
            CoinManager.get().addCoins(player, getReward(), true);
            hitEntity.getPassengers().forEach(Entity::remove);
            hitEntity.remove();
        } else if (hitEntity instanceof Zombie) {
            CoinManager.get().addCoins(player, getReward(), true);
            Entity vehicle = hitEntity.getVehicle();
            if (vehicle != null) vehicle.remove();
            hitEntity.remove();
        }

        event.getEntity().remove();
    }

    private boolean isAPlayerPlaying() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getBowBox().contains(player.getLocation().toVector())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean shouldPlayerLeave(Player player) {
        return !getBowBox().contains(player.getLocation().toVector());
    }

    @Override
    public boolean shouldPlayerJoin(Player player) {
        return getBowBox().contains(player.getLocation().toVector());
    }

    @Override
    public Location getTeleportLocation() {
        return getTpLocation();
    }

    @Override
    public void leavePlayer(Player player) {
        player.getInventory().remove(Material.BOW);
        player.getInventory().remove(Material.ARROW);
    }

    @Override
    public void joinPlayer(Player player) {
        player.getInventory().setItem(0, new ItemBuilder(Material.BOW)
                .enchant(Enchantment.ARROW_INFINITE)
                .setUnbreakable(true)
                .build());
        player.getInventory().setItem(1, new ItemStack(Material.ARROW));
    }

    @Override
    public String id() {
        return getGameID();
    }
}
