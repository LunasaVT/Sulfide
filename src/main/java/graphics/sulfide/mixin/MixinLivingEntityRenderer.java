package graphics.sulfide.mixin;

import graphics.sulfide.engine.MatrixTracker;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity> {
    @Inject(
            method = "method_10252",
            at = @At("RETURN")
    )
    private void sulfide$captureOverlay(T entity, float tickDelta, boolean bl,
                                      CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            MatrixTracker.clearOverlay();
            return;
        }

        boolean hurt = entity.hurtTime > 0 || entity.deathTime > 0;
        if (hurt) {
            MatrixTracker.setOverlay(1.0f, 0.0f, 0.0f, 0.3f);
        } else {
            try {
                MatrixTracker.setOverlay(1.0f, 1.0f, 1.0f, 0.15f);
            } catch (Exception ignored) {
                MatrixTracker.clearOverlay();
            }
        }
    }

    @Inject(
            method = "method_10260",
            at = @At("HEAD")
    )
    private void sulfide$clearOverlay(CallbackInfo ci) {
        MatrixTracker.clearOverlay();
    }

    @Inject(
            method = "render(Lnet/minecraft/entity/LivingEntity;DDDFF)V",
            at = @At("RETURN")
    )
    private void sulfide$renderReturn(T entity, double x, double y, double z,
                                    float yaw, float tickDelta, CallbackInfo ci) {
        MatrixTracker.clearOverlay();
    }
}
