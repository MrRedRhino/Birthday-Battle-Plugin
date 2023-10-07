package org.pipeman.bb.utils;

import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.pipeman.bb.Main;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

public class Utils {
    private static final Random RANDOM = new Random();

    public static void playSound(Player player, org.bukkit.Sound sound) {
        player.playSound(Sound.sound(sound.key(), Sound.Source.MASTER, 1, 1));
    }

    public static Optional<Integer> parseInt(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static boolean contains(Object[] array, Object value) {
        for (Object o : array) {
            if (o.equals(value)) return true;
        }
        return false;
    }

    public static <T> T getRandomValue(T[] array) {
        return array[RANDOM.nextInt(array.length)];
    }

    public static <T> T getRandomValue(List<T> list) {
        return list.get(RANDOM.nextInt(list.size()));
    }

    public static void shootParticleLine(Location from, Location to, int amount, org.bukkit.Particle particle) {
        Vector fromVec = from.toVector();
        Vector diff = to.toVector().subtract(fromVec);
        Vector step = diff.multiply(1.0 / amount);

        for (int i = 0; i < amount; i++) {
            Vector pos = fromVec.add(step);
            from.getWorld().spawnParticle(particle, pos.getX(), pos.getY(), pos.getZ(), 10);
        }
    }

    public static boolean containsIgnoringY(BoundingBox box, int x, int z) {
        return x >= box.getMinX() && x <= box.getMaxX() && z >= box.getMinZ() && z <= box.getMaxZ();
    }

    public static Location toLocation(Vector vector) {
        return toLocation(vector, Bukkit.getWorld("world"));
    }

    public static Location toLocation(Vector vector, World world) {
        return new Location(world, vector.getX(), vector.getY(), vector.getZ());
    }

    public static void putAttribute(Player player, String key, String value) {
        player.addScoreboardTag(key + '=' + value);
    }

    public static @Nullable String getAttribute(Player player, String key) {
        for (String tag : player.getScoreboardTags()) {
            if (tag.startsWith(key + '=')) {
                return tag.substring(key.length() + 1);
            }
        }
        return null;
    }

    public static boolean between(double value, double bound1, double bound2) {
        return value >= Math.min(bound1, bound2) && value <= Math.max(bound1, bound2);
    }

    public static Location loc(int x, int y, int z) {
        return new Location(Main.getGameWorld(), x, y, z);
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> extends Consumer<T> {
        @Override
        default void accept(T t) {
            try {
                acceptAndCatch(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        void acceptAndCatch(T t) throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingRunnable extends Runnable {
        @Override
        default void run() {
            try {
                runAndCatch();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        void runAndCatch() throws Exception;
    }
}
