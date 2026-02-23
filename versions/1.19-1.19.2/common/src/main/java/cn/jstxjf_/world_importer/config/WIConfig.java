package cn.jstxjf_.world_importer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class WIConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "world_importer.json";
    private static WIConfig INSTANCE = new WIConfig();

    public int uploadIntervalTicks = 5;
    public int maxPacketSizeKB = 30;
    public boolean autoThrottle = true;
    public double tpsThresholdLow = 15.0;
    public double tpsThresholdHigh = 18.0;
    public int maxIntervalTicks = 40;
    public int relightChunksPerTick = 5;

    public static WIConfig get() {
        return INSTANCE;
    }

    public static void load() {
        Path path = Platform.getConfigFolder().resolve(FILE_NAME);
        if (Files.exists(path)) {
            try {
                String json = Files.readString(path);
                INSTANCE = GSON.fromJson(json, WIConfig.class);
            } catch (IOException e) {
                INSTANCE = new WIConfig();
            }
        }
        save();
    }

    public static void save() {
        Path path = Platform.getConfigFolder().resolve(FILE_NAME);
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
