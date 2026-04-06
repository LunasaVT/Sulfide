package graphics.sulfide.mixin;

import graphics.sulfide.render.SulfideState;
import net.minecraft.client.render.entity.feature.HeldItemRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class MixinHeldItemFeatureRenderer {
    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFFFFFF)V",
            at = @At("HEAD"))
    private void sulfide$beforeHeldItem(LivingEntity entity,
                                      float limbAngle, float limbDistance,
                                      float tickDelta, float age,
                                      float headYaw, float headPitch,
                                      float scale, CallbackInfo ci) {
        SulfideState.suppressRecording = true;
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFFFFFF)V",
            at = @At("RETURN"))
    private void sulfide$afterHeldItem(LivingEntity entity,
                                     float limbAngle, float limbDistance,
                                     float tickDelta, float age,
                                     float headYaw, float headPitch,
                                     float scale, CallbackInfo ci) {
        SulfideState.suppressRecording = false;
    }
}

