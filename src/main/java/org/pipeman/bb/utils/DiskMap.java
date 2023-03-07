package org.pipeman.bb.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class DiskMap implements Map<byte[], byte[]> {
    private final Path file;
    private Map<ByteArray, byte[]> data = new HashMap<>();

    public DiskMap(Path file) {
        this.file = file;
        readFile();
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    @Override
    public byte[] get(Object key) {
        if (!(key instanceof byte[])) return null;
        return data.get(new ByteArray((byte[]) key));
    }

    @Override
    public byte[] put(byte[] key, byte[] value) {
        byte[] returnValue = data.put(new ByteArray(key), value);
        writeFile();
        return returnValue;
    }

    @Override
    public byte[] remove(Object key) {
        byte[] returnValue = data.remove(key);
        writeFile();
        return returnValue;
    }

    @Override
    public void putAll(Map<? extends byte[], ? extends byte[]> m) {
        m.forEach((key, value) -> data.put(new ByteArray(key), value));
        writeFile();
    }

    @Override
    public void clear() {
        data = new HashMap<>();
        writeFile();
    }

    @Override
    public Set<byte[]> keySet() {
        Set<byte[]> out = new HashSet<>();
        data.keySet().forEach(key -> out.add(key.getBytes()));
        return out;
    }

    @Override
    public Collection<byte[]> values() {
        return data.values();
    }

    @Override
    public Set<Entry<byte[], byte[]>> entrySet() {
        Set<Entry<byte[], byte[]>> out = new HashSet<>();
        data.entrySet().forEach(entry -> out.add(new Entry<byte[], byte[]>() {
            @Override
            public byte[] getKey() {
                return entry.getKey().getBytes();
            }

            @Override
            public byte[] getValue() {
                return entry.getValue();
            }

            @Override
            public byte[] setValue(byte[] value) {
                return entry.setValue(value);
            }
        }));
        return out;
    }

    private void writeFile() {
        try {
            try (OutputStream output = Files.newOutputStream(file, StandardOpenOption.CREATE)) {
                writeInt(data.size(), output);

                for (Entry<ByteArray, byte[]> entry : data.entrySet()) {
                    writeBytes(entry.getKey().getBytes(), output);
                    writeBytes(entry.getValue(), output);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readFile() {
        try {
            if (!new File(file.toString()).exists()) return;
            try (InputStream input = Files.newInputStream(file)) {
                int length = readInt(input);
                Map<ByteArray, byte[]> newData = new HashMap<>(length);

                for (int i = 0; i < length; i++) {
                    byte[] key = readBytes(input);
                    byte[] value = readBytes(input);
                    newData.put(new ByteArray(key), value);
                }
                data = newData;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] readBytes(InputStream input) throws IOException {
        int length = readInt(input);
        byte[] bytes = new byte[length];
        input.read(bytes);
        return bytes;
    }

    private void writeBytes(byte[] bytes, OutputStream os) throws IOException {
        writeInt(bytes.length, os);
        os.write(bytes);
    }

    private int readInt(InputStream input) throws IOException {
        return (input.read() << 24) | (input.read() << 16) | (input.read() << 8) | input.read();
    }

    private void writeInt(int i, OutputStream os) throws IOException {
        os.write((i >> 24) & 0xFF);
        os.write((i >> 16) & 0xFF);
        os.write((i >> 8) & 0xFF);
        os.write(i & 0xFF);
    }

    public static class ByteArray {
        private final byte[] bytes;

        public ByteArray(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(bytes);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ByteArray && Arrays.equals(bytes, ((ByteArray) obj).getBytes());
        }

        public byte[] getBytes() {
            return bytes;
        }
    }
}
