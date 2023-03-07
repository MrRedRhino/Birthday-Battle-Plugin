package org.pipeman.bb.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class StructureStorage {
    public static void loadStructure(InputStream input, Location loc) throws IOException {
        int xSize = readInt(input);
        int ySize = readInt(input);
        int zSize = readInt(input);

        iterateBlocks(loc, xSize, ySize, zSize, block -> {
            try {
                block.setBlockData(Bukkit.createBlockData(readString(input)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void saveStructure(OutputStream output, Location loc, int xSize, int ySize, int zSize) throws IOException {
        writeInt(xSize, output);
        writeInt(ySize, output);
        writeInt(zSize, output);

        iterateBlocks(loc, xSize, ySize, zSize, block -> {
            try {
                writeString(block.getBlockData().getAsString(), output);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void iterateBlocks(Location pos, int xSize, int ySize, int zSize, Consumer<Block> callback) {
        int startX = pos.getBlockX();
        int startY = pos.getBlockY();
        int startZ = pos.getBlockZ();
        World world = pos.getWorld();

        for (int y = startY; y < startY + ySize; y++) {
            for (int x = startX; x < startX + xSize; x++) {
                for (int z = startZ; z < startZ + zSize; z++) {
                    callback.accept(world.getBlockAt(x, y, z));
                }
            }
        }
    }

    private static int readInt(InputStream input) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(input.readNBytes(4));
        return bb.getInt();
    }

    private static void writeInt(int i, OutputStream output) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(4);
        output.write(bb.putInt(i).array());
    }

    private static String readString(InputStream input) throws IOException {
        int length = readInt(input);
        return new String(input.readNBytes(length));
    }

    private static void writeString(String s, OutputStream output) throws IOException {
        writeInt(s.length(), output);
        output.write(s.getBytes());
    }
}
