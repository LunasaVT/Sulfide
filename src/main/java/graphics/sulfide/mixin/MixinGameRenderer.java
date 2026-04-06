package graphics.sulfide.mixin;

import graphics.sulfide.config.SulfideOptionStorage;
import graphics.sulfide.render.SulfideState;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    @Unique
    private static final String RENDER_ENTITIES =
            "Lnet/minecraft/client/render/WorldRenderer;renderEntities(" +
                    "Lnet/minecraft/entity/Entity;" +
                    "Lnet/minecraft/client/render/CameraView;F)V";

    @Shadow
    private net.minecraft.client.MinecraftClient client;
    @Shadow
    @Mutable
    private boolean lightmapDirty;
    @Shadow
    private float lightmapFlicker;
    @Shadow
    private float skyDarkness;
    @Shadow
    private float lastSkyDarkness;
    @Final
    @Shadow
    private NativeImageBackedTexture lightmapTexture;
    @Final
    @Shadow
    private Identifier lightmapTextureId;

    @Shadow
    private float getNightVisionStrength(LivingEntity entity, float tickDelta) {
        throw new UnsupportedOperationException();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    void impl$__init__(CallbackInfo ci) {
        SulfideState.init();
    }

    @Inject(
            method = "renderWorld(IFJ)V",
            at = @At(value = "INVOKE", target = RENDER_ENTITIES)
    )
    private void zdraw$beforeEntities(int anaglyphFilter, float tickDelta, long limitTime,
                                      CallbackInfo ci) {
        if (SulfideOptionStorage.getInstance().isEnableInstancedEntities()) {
            SulfideState.INSTANCE.beginFrame();
        }
    }

    @Inject(method = "updateLightmap", at = @At("HEAD"), cancellable = true)
    private void zdraw$updateLightmap(float tickDelta, CallbackInfo ci) {
        if (!SulfideOptionStorage.getInstance().isEnableComputeLightmap()) return;
        ci.cancel();
        if (!this.lightmapDirty) return;

        World world = this.client.world;
        if (world == null) return;

        this.client.profiler.push("lightTex");

        float skyFactor = world.method_3649(1.0F);
        float flicker = this.lightmapFlicker * 0.1F + 1.5F;

        float skyDark = 0f;
        if (this.skyDarkness > 0f) {
            skyDark = this.lastSkyDarkness +
                    (this.skyDarkness - this.lastSkyDarkness) * tickDelta;
        }

        float nightVision = 0f;
        if (this.client.player != null &&
                this.client.player.hasStatusEffect(StatusEffect.NIGHTVISION)) {
            nightVision = getNightVisionStrength((PlayerEntity) this.client.player, tickDelta);
        }

        float gamma = this.client.options.gamma;
        int dimType = world.dimension.getType();
        boolean lightning = world.getLightningTicksLeft() > 0;
        float[] brightness = world.dimension.getLightLevelToBrightness();

        int glTex = this.client.getTextureManager()
                .getTexture(this.lightmapTextureId).getGlId();

        SulfideState.INSTANCE.getLightmapCompute().dispatch(
                glTex, brightness,
                skyFactor, flicker, skyDark,
                nightVision, gamma, dimType, lightning
        );

        this.lightmapDirty = false;
        this.client.profiler.pop();
    }
}