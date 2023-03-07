package org.pipeman.bb.games.cake;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.pipeman.bb.CoinManager;
import org.pipeman.bb.utils.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class CakeGame implements Listener {
    private static final List<Cake> cakes = new ArrayList<>();
    private final Interval interval = new Interval(300);
    private static final Random RANDOM = new Random();

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock != null && clickedBlock.getType() == Material.CAKE) {
            Location location = clickedBlock.getLocation();
            if (!cakes.removeIf(cake -> cake.onEat(location))) {
                clickedBlock.setType(Material.AIR);
            }
            clickedBlock.getWorld().spawnParticle(Particle.BLOCK_CRACK, location.toCenterLocation(), 40, Material.CAKE.createBlockData());
            CoinManager.get().addCoins(event.getPlayer(), 1, true);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void tick(ServerTickStartEvent event) {
        if (Bukkit.getOnlinePlayers().isEmpty()) return;

        if (interval.tick()) {
            // -385, 58, 528 543 90 -400
            World world = Bukkit.getWorld("world");
            if (world == null) return;

            for (int i = 0; i < 5; i++) {
                int x = RANDOM.nextInt(-385, 543);
                int y = RANDOM.nextInt(58, 90);
                int z = RANDOM.nextInt(-400, 528);

                Optional<Cake> cake = tryToGenerateCake(world, x, y, z);
                if (cake.isPresent()) {
                    cakes.add(cake.get());
                    break;
                }
            }
        }

        List<Cake> toRemove = new ArrayList<>();
        for (Cake cake : cakes) {
            if (cake.tick()) {
                toRemove.add(cake);
            }
        }
        cakes.removeAll(toRemove);
    }

    private static Optional<Cake> tryToGenerateCake(World world, int x, int y, int z) {
//        if (!world.is(x, z)) return Optional.empty();

        for (int i = 0; i < 10; i++) {
            Optional<Cake> attempt1 = createCake(world, x, y + i, z);
            if (attempt1.isPresent()) return attempt1;

            Optional<Cake> attempt2 = createCake(world, x, y - i, z);
            if (attempt2.isPresent()) return attempt2;
        }
        return Optional.empty();
    }

    private static Optional<Cake> createCake(World world, int x, int y, int z) {
        if (world.getType(x, y, z) == Material.GRASS_BLOCK && canReplace(world.getType(x, y + 1, z))) {
            boolean isGrass = world.getType(x, y + 1, z) == Material.GRASS;
            return Optional.of(new Cake(new Location(world, x, y + 1, z), isGrass));
        }
        return Optional.empty();
    }

    private static boolean canReplace(Material material) {
        return material.isAir() || material == Material.GRASS;
    }

    private static class Cake {
        private final Location location;
        private final long despawnTime;
        private final boolean replaceWithGrass;

        public Cake(Location location, boolean replaceWithGrass) {
            this.location = location;
            this.replaceWithGrass = replaceWithGrass;
            this.despawnTime = System.currentTimeMillis() + 600_000;
            location.getWorld().setType(location, Material.CAKE);
        }

        public boolean onEat(Location loc) {
            if (loc.equals(location)) {
                remove();
                return true;
            }
            return false;
        }

        public boolean tick() {
            if (despawnTime < System.currentTimeMillis()) {
                remove();
                return true;
            }
            return false;
        }

        private void remove() {
            location.getWorld().setType(location, replaceWithGrass ? Material.GRASS : Material.AIR);
        }
    }
}
