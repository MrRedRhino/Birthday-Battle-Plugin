package org.pipeman.bb.games.shoot_the_chicken;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.pipeman.bb.CoinManager;
import org.pipeman.bb.Main;
import org.pipeman.bb.games.Game;
import org.pipeman.bb.games.GameManager;
import org.pipeman.bb.utils.ItemBuilder;

public class ShootTheChickenGame implements Game {
    private static final Location TP_LOCATION = new Location(Main.getGameWorld(), 124.5, 69, 275, -90, 0);
    private static final BoundingBox BOX = new BoundingBox(128, 69, 269, 121, 75, 283);

    @Override
    public boolean shouldPlayerLeave(Player player) {
        return !BOX.contains(player.getLocation().toVector());
    }

    @Override
    public boolean shouldPlayerJoin(Player player) {
        return BOX.contains(player.getLocation().toVector());
    }

    @Override
    public Location getTeleportLocation() {
        return TP_LOCATION;
    }

    @Override
    public void leavePlayer(Player player) {

    }

    @Override
    public void joinPlayer(Player player) {
        ItemStack item = new ItemBuilder(Material.BOW)
                .setUnbreakable(true)
                .enchant(Enchantment.ARROW_INFINITE)
                .build();

        player.getInventory().setItem(0, item);
        player.getInventory().setItem(1, new ItemStack(Material.ARROW));
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        Entity entity = event.getHitEntity();
        if (entity instanceof Chicken && !entity.isInvulnerable()) {
            ((Chicken) entity).damage(42);
            if (event.getEntity().getShooter() instanceof Player shooter && GameManager.isPlayingGame(shooter, id())) {
                CoinManager.get().addCoins(shooter, 1, true);
            }
        }
    }

    @Override
    public String id() {
        return "shoot_the_chicken";
    }
}
