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
    private static void sulfide$matrixMode(int mode, CallbackInfo ci) {
        MatrixTracker.INSTANCE.setMode(mode);
    }

    @Inject(method = "pushMatrix", at = @At("HEAD"))
    private static void sulfide$pushMatrix(CallbackInfo ci) {
        MatrixTracker.INSTANCE.push();
    }

    @Inject(method = "popMatrix", at = @At("HEAD"))
    private static void sulfide$popMatrix(CallbackInfo ci) {
        MatrixTracker.INSTANCE.pop();
    }

    @Inject(method = "loadIdentity", at = @At("HEAD"))
    private static void sulfide$loadIdentity(CallbackInfo ci) {
        MatrixTracker.INSTANCE.loadIdentity();
    }

    @Inject(method = "translate(FFF)V", at = @At("HEAD"))
    private static void sulfide$translateF(float x, float y, float z, CallbackInfo ci) {
        MatrixTracker.INSTANCE.translate(x, y, z);
    }

    @Inject(method = "translate(DDD)V", at = @At("HEAD"))
    private static void sulfide$translateD(double x, double y, double z, CallbackInfo ci) {
        MatrixTracker.INSTANCE.translate((float) x, (float) y, (float) z);
    }

    @Inject(method = "rotate", at = @At("HEAD"))
    private static void sulfide$rotate(float angle, float x, float y, float z, CallbackInfo ci) {
        MatrixTracker.INSTANCE.rotate(angle, x, y, z);
    }

    @Inject(method = "scale(FFF)V", at = @At("HEAD"))
    private static void sulfide$scaleF(float x, float y, float z, CallbackInfo ci) {
        MatrixTracker.INSTANCE.scale(x, y, z);
    }

    @Inject(method = "scale(DDD)V", at = @At("HEAD"))
    private static void sulfide$scaleD(double x, double y, double z, CallbackInfo ci) {
        MatrixTracker.INSTANCE.scale((float) x, (float) y, (float) z);
    }

    @Inject(method = "multiMatrix", at = @At("HEAD"))
    private static void sulfide$multiMatrix(FloatBuffer buf, CallbackInfo ci) {
        MatrixTracker.INSTANCE.mulMatrix(buf);
    }

    @Inject(method = "bindTexture", at = @At("HEAD"))
    private static void sulfide$bindTexture(int texture, CallbackInfo ci) {
        MatrixTracker.INSTANCE.setBoundTexture(texture);
    }

    @Inject(method = "color(FFFF)V", at = @At("HEAD"))
    private static void sulfide$color(float r, float g, float b, float a, CallbackInfo ci) {
        MatrixTracker.setColor(r, g, b, a);
    }

    @Inject(method = "color(FFF)V", at = @At("HEAD"))
    private static void sulfide$colorRgb(float r, float g, float b, CallbackInfo ci) {
        MatrixTracker.setColor(r, g, b, MatrixTracker.INSTANCE.getColorA());
    }

    @Inject(method = "enableBlend", at = @At("HEAD"))
    private static void sulfide$enableBlend(CallbackInfo ci) {
        MatrixTracker.setBlend(true);
    }

    @Inject(method = "disableBlend", at = @At("HEAD"))
    private static void sulfide$disableBlend(CallbackInfo ci) {
        MatrixTracker.setBlend(false);
    }

    @Inject(method = "blendFunc", at = @At("HEAD"))
    private static void sulfide$blendFunc(int src, int dst, CallbackInfo ci) {
        MatrixTracker.setBlendFunc(src, dst);
    }

    @Inject(method = "enableTexture", at = @At("HEAD"))
    private static void sulfide$enableTexture(CallbackInfo ci) {
        MatrixTracker.setTextureEnabled(true);
    }

    @Inject(method = "disableTexture", at = @At("HEAD"))
    private static void sulfide$disableTexture(CallbackInfo ci) {
        MatrixTracker.setTextureEnabled(false);
    }

    @Inject(method = "alphaFunc", at = @At("HEAD"))
    private static void sulfide$alphaFunc(int func, float ref, CallbackInfo ci) {
        MatrixTracker.setAlphaFunc(func, ref);
    }


}

