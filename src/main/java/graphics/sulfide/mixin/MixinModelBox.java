package graphics.sulfide.mixin;

import graphics.sulfide.engine.MatrixTracker;
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
    private void zdraw$captureUvParams(
            ModelPart part, int texU, int texV,
            float x, float y, float z,
            int szX, int szY, int szZ,
            float inflate, boolean mirror,
            CallbackInfo ci
    ) {
        MatrixTracker.INSTANCE.getBoxUVData().put(this, new int[]{
                texU, texV, szX, szY, szZ,
                Float.floatToRawIntBits(part.textureWidth),
                Float.floatToRawIntBits(part.textureHeight),
                Float.floatToRawIntBits(inflate)
        });
    }
}
