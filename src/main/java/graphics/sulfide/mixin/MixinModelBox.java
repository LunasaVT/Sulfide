package graphics.sulfide.mixin;

import graphics.sulfide.engine.MatrixTracker;
import graphics.sulfide.render.entity.EntityModelCollector;
import net.minecraft.client.render.ModelBox;
import net.minecraft.client.render.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBox.class)
public class MixinModelBox {
    @Inject(
            method = "<init>(Lnet/minecraft/client/render/model/ModelPart;IIFFFIIIFZ)V",
            at = @At("RETURN")
    )
    private void sulfide$captureUvParams(
            ModelPart part, int texU, int texV,
            float x, float y, float z,
            int szX, int szY, int szZ,
            float inflate, boolean mirror,
            CallbackInfo ci
    ) {
        float textureWidth = part.textureWidth;
        float textureHeight = part.textureHeight;
        int i = texU;
        int j = texV;
        int k = szX;
        int l = szY;
        int m = szZ;

        MatrixTracker.INSTANCE.getBoxUVData().put(this, new int[]{
                EntityModelCollector.packUV((i + 2f * m + k) / textureWidth, (j + m) / textureHeight),
                EntityModelCollector.packUV((i + 2f * m + 2f * k) / textureWidth, (j + m + l) / textureHeight),

                EntityModelCollector.packUV((i + m) / textureWidth, (j + m) / textureHeight),
                EntityModelCollector.packUV((i + m + k) / textureWidth, (j + m + l) / textureHeight),

                EntityModelCollector.packUV(i / textureWidth, (j + m) / textureHeight),
                EntityModelCollector.packUV((i + m) / textureWidth, (j + m + l) / textureHeight),

                EntityModelCollector.packUV((i + m + k) / textureWidth, (j + m) / textureHeight),
                EntityModelCollector.packUV((i + 2f * m + k) / textureWidth, (j + m + l) / textureHeight),

                EntityModelCollector.packUV((i + m + k) / textureWidth, (j + m) / textureHeight),
                EntityModelCollector.packUV((i + m + 2f * k) / textureWidth, j / textureHeight),

                EntityModelCollector.packUV((i + m) / textureWidth, j / textureHeight),
                EntityModelCollector.packUV((i + m + k) / textureWidth, (j + m) / textureHeight),

                Float.floatToRawIntBits(inflate)
        });
    }
}
