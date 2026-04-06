package graphics.sulfide.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import graphics.sulfide.engine.MatrixTracker;
import graphics.sulfide.engine.texture.TextureRegion;
import graphics.sulfide.render.SulfideState;
import graphics.sulfide.render.entity.EntityModelCollector;
import net.minecraft.client.render.ModelBox;
import net.minecraft.client.render.model.ModelPart;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;
import java.util.List;

@Mixin(ModelPart.class)
public class MixinModelPart {
    @Shadow
    public float offsetX;
    @Shadow
    public float offsetY;
    @Shadow
    public float offsetZ;

    @Shadow
    public float posX;
    @Shadow
    public float posY;
    @Shadow
    public float posZ;

    @Shadow
    public float pivotX;
    @Shadow
    public float pivotY;
    @Shadow
    public float pivotZ;

    @Shadow
    public boolean hide;
    @Shadow
    public boolean visible;

    @Shadow
    public List<ModelBox> cuboids;
    @Shadow
    public List<ModelPart> modelList;

    @Unique
    private static final float RAD_TO_DEG = 180.0F / (float) Math.PI;

    @Unique
    private static final FloatBuffer sulfide$mvBuf = BufferUtils.createFloatBuffer(16);
    @Unique
    private static final FloatBuffer sulfide$lightBuf = BufferUtils.createFloatBuffer(4);
    @Unique
    private static final FloatBuffer sulfide$texMatBuf = BufferUtils.createFloatBuffer(16);


    @Inject(method = "render(F)V", at = @At("HEAD"), cancellable = true)
    private void sulfide$render(float scale, CallbackInfo ci) {
        if (!SulfideState.INSTANCE.getRecording()) return;

        if (this.hide || !this.visible) {
            ci.cancel();
            return;
        }

        GlStateManager.translate(this.offsetX, this.offsetY, this.offsetZ);

        boolean hasRotation = this.posX != 0.0F || this.posY != 0.0F || this.posZ != 0.0F;
        boolean hasPivot = this.pivotX != 0.0F || this.pivotY != 0.0F || this.pivotZ != 0.0F;

        if (hasRotation) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(
                    this.pivotX * scale, this.pivotY * scale, this.pivotZ * scale);
            if (this.posZ != 0.0F)
                GlStateManager.rotate(this.posZ * RAD_TO_DEG, 0.0F, 0.0F, 1.0F);
            if (this.posY != 0.0F)
                GlStateManager.rotate(this.posY * RAD_TO_DEG, 0.0F, 1.0F, 0.0F);
            if (this.posX != 0.0F)
                GlStateManager.rotate(this.posX * RAD_TO_DEG, 1.0F, 0.0F, 0.0F);
        } else if (hasPivot) {
            GlStateManager.translate(
                    this.pivotX * scale, this.pivotY * scale, this.pivotZ * scale);
        }

        sulfide$recordCuboids(scale);

        if (this.modelList != null) {
            for (ModelPart modelPart : this.modelList) {
                modelPart.render(scale);
            }
        }

        if (hasRotation) {
            GlStateManager.popMatrix();
        } else if (hasPivot) {
            GlStateManager.translate(
                    -this.pivotX * scale, -this.pivotY * scale, -this.pivotZ * scale);
        }

        GlStateManager.translate(-this.offsetX, -this.offsetY, -this.offsetZ);

        ci.cancel();
    }

    @Inject(method = "rotateAndRender(F)V", at = @At("HEAD"), cancellable = true)
    private void sulfide$rotateAndRender(float scale, CallbackInfo ci) {
        if (!SulfideState.INSTANCE.getRecording() || SulfideState.suppressRecording) return;

        if (this.hide || !this.visible) {
            ci.cancel();
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(
                this.pivotX * scale, this.pivotY * scale, this.pivotZ * scale);
        if (this.posZ != 0.0F)
            GlStateManager.rotate(this.posZ * RAD_TO_DEG, 0.0F, 0.0F, 1.0F);
        if (this.posY != 0.0F)
            GlStateManager.rotate(this.posY * RAD_TO_DEG, 0.0F, 1.0F, 0.0F);
        if (this.posX != 0.0F)
            GlStateManager.rotate(this.posX * RAD_TO_DEG, 1.0F, 0.0F, 0.0F);

        sulfide$recordCuboids(scale);

        if (this.modelList != null) {
            for (ModelPart modelPart : this.modelList) {
                modelPart.render(scale);
            }
        }

        GlStateManager.popMatrix();

        ci.cancel();
    }

    @Unique
    private void sulfide$recordCuboids(float scale) {
        TextureRegion region = SulfideState.INSTANCE.getOrUploadTexture(
                MatrixTracker.INSTANCE.getBoundTexture());
        if (region == null) return;

        sulfide$mvBuf.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, sulfide$mvBuf);
        sulfide$mvBuf.rewind();
        Matrix4f modelview = new Matrix4f(sulfide$mvBuf);

        boolean isGlint = GL11.glIsEnabled(GL11.GL_BLEND)
                && GL11.glGetInteger(GL11.GL_BLEND_SRC) == GL11.GL_SRC_COLOR;

        float uvM00 = 1f, uvM01 = 0f;
        float uvM10 = 0f, uvM11 = 1f;
        float uvM30 = 0f, uvM31 = 0f;
        if (isGlint) {
            sulfide$texMatBuf.clear();
            GL11.glGetFloat(GL11.GL_TEXTURE_MATRIX, sulfide$texMatBuf);
            sulfide$texMatBuf.rewind();
            Matrix4f texMat = new Matrix4f(sulfide$texMatBuf);
            uvM00 = texMat.m00();
            uvM01 = texMat.m10();
            uvM10 = texMat.m01();
            uvM11 = texMat.m11();
            uvM30 = texMat.m30();
            uvM31 = texMat.m31();
        }

        int prevTex = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        int prevClientTex = GL11.glGetInteger(GL13.GL_CLIENT_ACTIVE_TEXTURE);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
        sulfide$lightBuf.clear();
        GL11.glGetFloat(GL11.GL_CURRENT_TEXTURE_COORDS, sulfide$lightBuf);
        GL13.glActiveTexture(prevTex);
        GL13.glClientActiveTexture(prevClientTex);

        int lightU = Math.min(15, Math.max(0, (int) (sulfide$lightBuf.get(0) / 16.0f)));
        int lightV = Math.min(15, Math.max(0, (int) (sulfide$lightBuf.get(1) / 16.0f)));

        int packedColor = EntityModelCollector.packRGBA(
                (int) (MatrixTracker.INSTANCE.getColorR() * 255.0f),
                (int) (MatrixTracker.INSTANCE.getColorG() * 255.0f),
                (int) (MatrixTracker.INSTANCE.getColorB() * 255.0f),
                (int) (MatrixTracker.INSTANCE.getColorA() * 255.0f)
        );

        int overlayColor = EntityModelCollector.packRGBA(
                (int) (MatrixTracker.INSTANCE.getOverlayR() * 255.0f),
                (int) (MatrixTracker.INSTANCE.getOverlayG() * 255.0f),
                (int) (MatrixTracker.INSTANCE.getOverlayB() * 255.0f),
                (int) (MatrixTracker.INSTANCE.getOverlayA() * 255.0f)
        );

        for (ModelBox box : this.cuboids) {
            int[] uvData = MatrixTracker.INSTANCE.getBoxUVData().get(box);
            if (uvData == null) continue;

            float inflate = (uvData.length > 7) ? Float.intBitsToFloat(uvData[7]) : 0.0f;

            float cx = (box.minX + box.maxX) * 0.5f * scale;
            float cy = (box.minY + box.maxY) * 0.5f * scale;
            float cz = (box.minZ + box.maxZ) * 0.5f * scale;
            float sx = ((box.maxX - box.minX) + inflate * 2.0f) * scale;
            float sy = ((box.maxY - box.minY) + inflate * 2.0f) * scale;
            float sz = ((box.maxZ - box.minZ) + inflate * 2.0f) * scale;

            Matrix4f model = new Matrix4f(modelview)
                    .translate(cx, cy, cz)
                    .scale(sx, sy, sz);

            EntityModelCollector.record(
                    model,
                    sulfide$buildUvRects(uvData),
                    region,
                    lightU,
                    lightV,
                    packedColor,
                    overlayColor,
                    uvM00, uvM01,
                    uvM10, uvM11,
                    uvM30, uvM31,
                    isGlint
            );
        }
    }

    @Unique
    private static int[] sulfide$buildUvRects(int[] uv) {
        int i = uv[0], j = uv[1];
        int k = uv[2], l = uv[3], m = uv[4];
        float tw = Float.intBitsToFloat(uv[5]);
        float th = Float.intBitsToFloat(uv[6]);

        return new int[]{
                EntityModelCollector.packUV((i + 2 * m + k) / tw, (j + m) / th),
                EntityModelCollector.packUV((i + 2 * m + 2 * k) / tw, (j + m + l) / th),

                EntityModelCollector.packUV((i + m) / tw, (j + m) / th),
                EntityModelCollector.packUV((i + m + k) / tw, (j + m + l) / th),

                EntityModelCollector.packUV(i / tw, (j + m) / th),
                EntityModelCollector.packUV((i + m) / tw, (j + m + l) / th),

                EntityModelCollector.packUV((i + m + k) / tw, (j + m) / th),
                EntityModelCollector.packUV((i + 2 * m + k) / tw, (j + m + l) / th),

                EntityModelCollector.packUV((i + m + k) / tw, (j + m) / th),
                EntityModelCollector.packUV((i + m + 2 * k) / tw, j / th),

                EntityModelCollector.packUV((i + m) / tw, j / th),
                EntityModelCollector.packUV((i + m + k) / tw, (j + m) / th),
        };
    }
}
