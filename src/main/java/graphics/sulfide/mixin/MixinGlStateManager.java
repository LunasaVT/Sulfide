package graphics.sulfide.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import graphics.sulfide.engine.MatrixTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {

    @Inject(method = "matrixMode", at = @At("HEAD"))
    private static void zdraw$matrixMode(int mode, CallbackInfo ci) {
        MatrixTracker.INSTANCE.setMode(mode);
    }

    @Inject(method = "pushMatrix", at = @At("HEAD"))
    private static void zdraw$pushMatrix(CallbackInfo ci) {
        MatrixTracker.INSTANCE.push();
    }

    @Inject(method = "popMatrix", at = @At("HEAD"))
    private static void zdraw$popMatrix(CallbackInfo ci) {
        MatrixTracker.INSTANCE.pop();
    }

    @Inject(method = "loadIdentity", at = @At("HEAD"))
    private static void zdraw$loadIdentity(CallbackInfo ci) {
        MatrixTracker.INSTANCE.loadIdentity();
    }

    @Inject(method = "translate(FFF)V", at = @At("HEAD"))
    private static void zdraw$translateF(float x, float y, float z, CallbackInfo ci) {
        MatrixTracker.INSTANCE.translate(x, y, z);
    }

    @Inject(method = "translate(DDD)V", at = @At("HEAD"))
    private static void zdraw$translateD(double x, double y, double z, CallbackInfo ci) {
        MatrixTracker.INSTANCE.translate((float) x, (float) y, (float) z);
    }

    @Inject(method = "rotate", at = @At("HEAD"))
    private static void zdraw$rotate(float angle, float x, float y, float z, CallbackInfo ci) {
        MatrixTracker.INSTANCE.rotate(angle, x, y, z);
    }

    @Inject(method = "scale(FFF)V", at = @At("HEAD"))
    private static void zdraw$scaleF(float x, float y, float z, CallbackInfo ci) {
        MatrixTracker.INSTANCE.scale(x, y, z);
    }

    @Inject(method = "scale(DDD)V", at = @At("HEAD"))
    private static void zdraw$scaleD(double x, double y, double z, CallbackInfo ci) {
        MatrixTracker.INSTANCE.scale((float) x, (float) y, (float) z);
    }

    @Inject(method = "multiMatrix", at = @At("HEAD"))
    private static void zdraw$multiMatrix(FloatBuffer buf, CallbackInfo ci) {
        MatrixTracker.INSTANCE.mulMatrix(buf);
    }

    @Inject(method = "bindTexture", at = @At("HEAD"))
    private static void zdraw$bindTexture(int texture, CallbackInfo ci) {
        MatrixTracker.INSTANCE.setBoundTexture(texture);
    }

    @Inject(method = "color(FFFF)V", at = @At("HEAD"))
    private static void zdraw$color(float r, float g, float b, float a, CallbackInfo ci) {
        MatrixTracker.setColor(r, g, b, a);
    }

    @Inject(method = "color(FFF)V", at = @At("HEAD"))
    private static void zdraw$colorRgb(float r, float g, float b, CallbackInfo ci) {
        MatrixTracker.setColor(r, g, b, MatrixTracker.INSTANCE.getColorA());
    }


}

