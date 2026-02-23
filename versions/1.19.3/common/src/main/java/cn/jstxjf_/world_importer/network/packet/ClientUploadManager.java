package cn.jstxjf_.world_importer.network.packet;

import cn.jstxjf_.world_importer.WorldImporter;
import cn.jstxjf_.world_importer.config.WIConfig;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientUploadManager {
    private static final Pattern MCA_PATTERN = Pattern.compile("r\\.(-?\\d+)\\.(-?\\d+)\\.mca");
    private static final List<ChunkEntry> chunkQueue = new ArrayList<>();
    private static int currentIndex = 0;
    private static boolean active = false;
    private static String selectedPath = "";
    private static int totalChunks = 0;
    private static int centerChunkX = 0;
    private static int centerChunkZ = 0;

    public static void setSelectedPath(String path) {
        selectedPath = path;
    }

    public static String getSelectedPath() {
        return selectedPath;
    }

    public static void startUpload(File regionDir) {
        chunkQueue.clear();
        currentIndex = 0;
        active = false;

        if (!regionDir.exists() || !regionDir.isDirectory()) {
            WorldImporter.LOGGER.error("Region directory not found: {}", regionDir);
            return;
        }

        File[] files = regionDir.listFiles((dir, name) -> name.endsWith(".mca"));
        if (files == null || files.length == 0) {
            WorldImporter.LOGGER.error("No MCA files found in: {}", regionDir);
            return;
        }

        for (File f : files) {
            Matcher m = MCA_PATTERN.matcher(f.getName());
            if (m.matches()) {
                int rx = Integer.parseInt(m.group(1));
                int rz = Integer.parseInt(m.group(2));
                try {
                    extractChunksFromMCA(f, rx, rz);
                } catch (IOException e) {
                    WorldImporter.LOGGER.error("Failed to parse MCA: {}", f.getName(), e);
                }
            }
        }

        if (chunkQueue.isEmpty()) return;

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (ChunkEntry e : chunkQueue) {
            if (e.chunkX < minX) minX = e.chunkX;
            if (e.chunkX > maxX) maxX = e.chunkX;
            if (e.chunkZ < minZ) minZ = e.chunkZ;
            if (e.chunkZ > maxZ) maxZ = e.chunkZ;
        }
        centerChunkX = (minX + maxX) / 2;
        centerChunkZ = (minZ + maxZ) / 2;

        totalChunks = chunkQueue.size();
        active = true;
        ClientImportState.reset();
        C2SUploadStartPacket.send(totalChunks);
    }

    private static void extractChunksFromMCA(File mcaFile, int regionX, int regionZ) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(mcaFile, "r")) {
            if (raf.length() < 8192) return;

            for (int localZ = 0; localZ < 32; localZ++) {
                for (int localX = 0; localX < 32; localX++) {
                    int headerOffset = 4 * (localX + localZ * 32);
                    raf.seek(headerOffset);
                    int locationEntry = raf.readInt();
                    if (locationEntry == 0) continue;

                    int sectorOffset = (locationEntry >> 8) & 0xFFFFFF;
                    int sectorCount = locationEntry & 0xFF;
                    if (sectorOffset < 2 || sectorCount == 0) continue;

                    long byteOffset = (long) sectorOffset * 4096;
                    if (byteOffset + 5 > raf.length()) continue;

                    raf.seek(byteOffset);
                    int dataLength = raf.readInt();
                    int compressionType = raf.readByte();

                    if (dataLength <= 1 || dataLength > sectorCount * 4096) continue;

                    byte[] compressedData = new byte[dataLength - 1];
                    raf.readFully(compressedData);

                    int chunkX = regionX * 32 + localX;
                    int chunkZ = regionZ * 32 + localZ;

                    chunkQueue.add(new ChunkEntry(chunkX, chunkZ, compressionType, compressedData));
                }
            }
        }
    }

    public static void sendNext() {
        if (!active || currentIndex >= chunkQueue.size()) {
            if (active) {
                active = false;
                C2SUploadFinishPacket.send();
            }
            return;
        }

        ChunkEntry entry = chunkQueue.get(currentIndex);
        currentIndex++;

        int relX = entry.chunkX - centerChunkX;
        int relZ = entry.chunkZ - centerChunkZ;

        int maxSize = WIConfig.get().maxPacketSizeKB * 1024;
        if (entry.data.length <= maxSize) {
            C2SUploadMCAChunkPacket.send(relX, relZ, entry.compressionType, 0, 1, entry.data);
        } else {
            int totalParts = (entry.data.length + maxSize - 1) / maxSize;
            for (int i = 0; i < totalParts; i++) {
                int offset = i * maxSize;
                int len = Math.min(maxSize, entry.data.length - offset);
                byte[] part = new byte[len];
                System.arraycopy(entry.data, offset, part, 0, len);
                C2SUploadMCAChunkPacket.send(relX, relZ, entry.compressionType, i, totalParts, part);
            }
        }
    }

    public static boolean isActive() {
        return active;
    }

    public static void cancel() {
        active = false;
        chunkQueue.clear();
        currentIndex = 0;
    }

    public static int getQueueSize() {
        return totalChunks;
    }

    public static int getCurrentIndex() {
        return currentIndex;
    }

    private record ChunkEntry(int chunkX, int chunkZ, int compressionType, byte[] data) {}
}
