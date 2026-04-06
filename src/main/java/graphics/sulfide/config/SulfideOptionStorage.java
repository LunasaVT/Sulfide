package graphics.sulfide.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SulfideOptionStorage {
    private static final Logger LOGGER = LogManager.getLogger("sulfide/config");

    private static final SulfideOptionStorage INSTANCE = new SulfideOptionStorage();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Paths.get("config", "sulfide.json");

    private boolean enableInstancedEntities = true;
    private boolean enableTextWidthCache = true;
    private boolean enableSkyRendering = true;
    private boolean enableCloudRendering = true;
    private boolean enableComputeLightmap = false;

    private int textWidthCacheSize = 4096;
    private int maxEntityInstances = 32384;
    private int atlasLayerSize = 1024;
    private int maxAtlasLayers = 16;

    private float cloudOpacity = 0.8f;
    private double cloudSpeed = 0.03;

    private SulfideOptionStorage() {
        load();
    }

    public static SulfideOptionStorage getInstance() {
        return INSTANCE;
    }

    public boolean isEnableInstancedEntities() {
        return enableInstancedEntities;
    }

    public void setEnableInstancedEntities(boolean v) {
        enableInstancedEntities = v;
    }

    public boolean isEnableTextWidthCache() {
        return enableTextWidthCache;
    }

    public void setEnableTextWidthCache(boolean v) {
        enableTextWidthCache = v;
    }

    public boolean isEnableSkyRendering() {
        return enableSkyRendering;
    }

    public void setEnableSkyRendering(boolean v) {
        enableSkyRendering = v;
    }

    public boolean isEnableCloudRendering() {
        return enableCloudRendering;
    }

    public void setEnableCloudRendering(boolean v) {
        enableCloudRendering = v;
    }

    public boolean isEnableComputeLightmap() {
        return enableComputeLightmap;
    }

    public void setEnableComputeLightmap(boolean v) {
        enableComputeLightmap = v;
    }

    public int getTextWidthCacheSize() {
        return textWidthCacheSize;
    }

    public void setTextWidthCacheSize(int v) {
        textWidthCacheSize = Math.max(128, Math.min(65536, v));
    }

    public int getMaxEntityInstances() {
        return maxEntityInstances;
    }

    public void setMaxEntityInstances(int v) {
        maxEntityInstances = Math.max(1024, Math.min(131072, v));
    }

    public int getAtlasLayerSize() {
        return atlasLayerSize;
    }

    public void setAtlasLayerSize(int v) {
        atlasLayerSize = Math.max(256, Math.min(4096, v));
    }

    public int getMaxAtlasLayers() {
        return maxAtlasLayers;
    }

    public void setMaxAtlasLayers(int v) {
        maxAtlasLayers = Math.max(4, Math.min(64, v));
    }

    public float getCloudOpacity() {
        return cloudOpacity;
    }

    public void setCloudOpacity(float v) {
        cloudOpacity = Math.max(0f, Math.min(1f, v));
    }

    public double getCloudSpeed() {
        return cloudSpeed;
    }

    public void setCloudSpeed(double v) {
        cloudSpeed = Math.max(0.0, Math.min(0.2, v));
    }

    public void flush() {
        save();
    }

    private void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                ConfigData data = GSON.fromJson(json, ConfigData.class);
                if (data != null) data.applyTo(this);
            } else {
                save();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load config", e);
        }
    }

    private void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer w = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(new ConfigData(this), w);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save config", e);
        }
    }

    @SuppressWarnings("unused") // fields read/written by Gson
    private static class ConfigData {
        boolean enableInstancedEntities = true;
        boolean enableTextWidthCache = true;
        boolean enableSkyRendering = true;
        boolean enableCloudRendering = true;
        boolean enableComputeLightmap = false;
        int textWidthCacheSize = 4096;
        int maxEntityInstances = 32384;
        int atlasLayerSize = 1024;
        int maxAtlasLayers = 16;
        float cloudOpacity = 0.8f;
        double cloudSpeed = 0.03;

        ConfigData() {
        }

        ConfigData(SulfideOptionStorage s) {
            enableInstancedEntities = s.enableInstancedEntities;
            enableTextWidthCache = s.enableTextWidthCache;
            enableSkyRendering = s.enableSkyRendering;
            enableCloudRendering = s.enableCloudRendering;
            enableComputeLightmap = s.enableComputeLightmap;
            textWidthCacheSize = s.textWidthCacheSize;
            maxEntityInstances = s.maxEntityInstances;
            atlasLayerSize = s.atlasLayerSize;
            maxAtlasLayers = s.maxAtlasLayers;
            cloudOpacity = s.cloudOpacity;
            cloudSpeed = s.cloudSpeed;
        }

        void applyTo(SulfideOptionStorage s) {
            s.setEnableInstancedEntities(enableInstancedEntities);
            s.setEnableTextWidthCache(enableTextWidthCache);
            s.setEnableSkyRendering(enableSkyRendering);
            s.setEnableCloudRendering(enableCloudRendering);
            s.setEnableComputeLightmap(enableComputeLightmap);
            s.setTextWidthCacheSize(textWidthCacheSize);
            s.setMaxEntityInstances(maxEntityInstances);
            s.setAtlasLayerSize(atlasLayerSize);
            s.setMaxAtlasLayers(maxAtlasLayers);
            s.setCloudOpacity(cloudOpacity);
            s.setCloudSpeed(cloudSpeed);
        }
    }
}
