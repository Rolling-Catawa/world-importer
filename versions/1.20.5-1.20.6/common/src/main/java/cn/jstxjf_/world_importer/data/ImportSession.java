package cn.jstxjf_.world_importer.data;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImportSession {
    private static final Map<MinecraftServer, ImportSession> SESSIONS = new ConcurrentHashMap<>();

    private int originX = 0;
    private int originZ = 0;
    private boolean originSet = false;

    private volatile boolean importing = false;
    private volatile boolean cancelled = false;
    private volatile int totalChunks = 0;
    private volatile int completedChunks = 0;

    private final Map<String, byte[]> chunkBuffers = new ConcurrentHashMap<>();
    private final List<ChunkPos> placedChunks = new ArrayList<>();

    public static ImportSession getOrCreate(MinecraftServer server) {
        return SESSIONS.computeIfAbsent(server, k -> new ImportSession());
    }

    public static void remove(MinecraftServer server) {
        SESSIONS.remove(server);
    }

    public void setOrigin(int x, int z) {
        this.originX = x;
        this.originZ = z;
        this.originSet = true;
    }

    public boolean isOriginSet() {
        return originSet;
    }

    public int getOriginX() {
        return originX;
    }

    public int getOriginZ() {
        return originZ;
    }

    public boolean isImporting() {
        return importing;
    }

    public void setImporting(boolean importing) {
        this.importing = importing;
        if (importing) this.cancelled = false;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
        this.importing = false;
        this.chunkBuffers.clear();
    }

    public void setTotal(int total) {
        this.totalChunks = total;
        this.completedChunks = 0;
        this.placedChunks.clear();
    }

    public void incrementCompleted() {
        this.completedChunks++;
    }

    public void addPlacedChunk(ChunkPos pos) {
        placedChunks.add(pos);
    }

    public List<ChunkPos> getPlacedChunks() {
        return placedChunks;
    }

    public int getProgress() {
        if (totalChunks == 0) return 0;
        return (completedChunks * 100) / totalChunks;
    }

    public int getCompletedMCA() {
        return completedChunks;
    }

    public int getTotalMCA() {
        return totalChunks;
    }

    public void putMCABuffer(String key, byte[] data) {
        chunkBuffers.put(key, data);
    }

    public byte[] getMCABuffer(String key) {
        return chunkBuffers.get(key);
    }

    public void removeMCABuffer(String key) {
        chunkBuffers.remove(key);
    }

    public String getStatusDisplay() {
        if (!originSet) return "§7粘贴中心点: 未设置";
        return "§7粘贴中心点: (" + originX + ", " + originZ + ")";
    }

    public void reset() {
        importing = false;
        cancelled = false;
        totalChunks = 0;
        completedChunks = 0;
        chunkBuffers.clear();
        placedChunks.clear();
    }
}
