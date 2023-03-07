package org.pipeman.bb.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

public class InventoryStorage {
    private static final DiskMap inventories = new DiskMap(Path.of("inventories.db"));

    public static void saveInventory(Player player) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(out);

            iterateInventory(player, item -> {
                try {
                    os.writeObject(item == null ? new ItemStack(Material.AIR) : item);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return item;
            });

            inventories.put(player.getUniqueId().toString().getBytes(), out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadInventory(Player player) {
        try {
            byte[] bytes = inventories.get(player.getUniqueId().toString().getBytes());
            if (bytes == null) return;

            BukkitObjectInputStream is = new BukkitObjectInputStream(new ByteArrayInputStream(bytes));
            iterateInventory(player, item -> {
                try {
                    return (ItemStack) is.readObject();
                } catch (ClassNotFoundException | IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void iterateInventory(Player player, Function<ItemStack, ItemStack> action) {
        for (int i = 0; i <= 40; i++) {
            ItemStack oldItem = player.getInventory().getItem(i);
            ItemStack result = action.apply(oldItem);
            if (oldItem != result) player.getInventory().setItem(i, result);
        }
    }
}
