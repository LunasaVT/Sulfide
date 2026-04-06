package graphics.sulfide.config;

import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public class SulfideConfigBuilder implements ConfigEntryPoint {
    private final SulfideOptionStorage storage = SulfideOptionStorage.getInstance();
    private final StorageEventHandler flushHandler = this.storage::flush;

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        builder.registerOwnModOptions()
                .setIcon(new Identifier("sulfide:sodium_icon.png"))
                .addPage(builder.createOptionPage()
                        .setName(new LiteralText("Features"))
                        .addOptionGroup(builder.createOptionGroup()
                                .setName(new LiteralText("Entity Rendering"))
                                .addOption(builder.createBooleanOption(new Identifier("sulfide:instanced_entities"))
                                        .setName(new LiteralText("Instanced Entity Rendering"))
                                        .setTooltip(new LiteralText("Uses GPU instancing to batch entity draw calls, significantly reducing CPU overhead."))
                                        .setStorageHandler(this.flushHandler)
                                        .setBinding(this.storage::setEnableInstancedEntities, this.storage::isEnableInstancedEntities)
                                        .setDefaultValue(true)
                                )
                        )
                        .addOptionGroup(builder.createOptionGroup()
                                .setName(new LiteralText("Text Rendering"))
                                .addOption(builder.createBooleanOption(new Identifier("sulfide:text_width_cache"))
                                        .setName(new LiteralText("Text Width Cache"))
                                        .setTooltip(new LiteralText("Caches text width calculations in an LRU cache to avoid redundant recomputation."))
                                        .setStorageHandler(this.flushHandler)
                                        .setBinding(this.storage::setEnableTextWidthCache, this.storage::isEnableTextWidthCache)
                                        .setDefaultValue(true)
                                )
                        )
                        .addOptionGroup(builder.createOptionGroup()
                                .setName(new LiteralText("World Rendering"))
                                .addOption(builder.createBooleanOption(new Identifier("sulfide:sky_rendering"))
                                        .setName(new LiteralText("Optimized Sky Rendering"))
                                        .setTooltip(new LiteralText("Replaces the vanilla sky renderer with a VBO-cached implementation."))
                                        .setStorageHandler(this.flushHandler)
                                        .setBinding(this.storage::setEnableSkyRendering, this.storage::isEnableSkyRendering)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(new Identifier("sulfide:cloud_rendering"))
                                        .setName(new LiteralText("Optimized Cloud Rendering"))
                                        .setTooltip(new LiteralText("Replaces the vanilla cloud renderer with a VBO-cached flat cloud mesh."))
                                        .setStorageHandler(this.flushHandler)
                                        .setBinding(this.storage::setEnableCloudRendering, this.storage::isEnableCloudRendering)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(new Identifier("sulfide:compute_lightmap"))
                                        .setName(new LiteralText("GPU Lightmap Computation"))
                                        .setTooltip(new LiteralText("Offloads lightmap texture updates to a compute shader instead of the CPU."))
                                        .setStorageHandler(this.flushHandler)
                                        .setBinding(this.storage::setEnableComputeLightmap, this.storage::isEnableComputeLightmap)
                                        .setDefaultValue(true)
                                )
                        )
                );
    }
}