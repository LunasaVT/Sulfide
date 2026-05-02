package graphics.sulfide.mixin;

import graphics.sulfide.engine.LightState;
import graphics.sulfide.engine.MatrixTracker;
import net.minecraft.client.render.DiffuseLighting;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.FloatBuffer;

@Mixin(DiffuseLighting.class)
public abstract class MixinDiffuseLighting {
    @Redirect(
            method = "enableNormally",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glLight(IILjava/nio/FloatBuffer;)V"
            )
    )
    private static void captureLightBuffers(int light, int pname, FloatBuffer params) {
        if (pname == GL11.GL_POSITION) {
            if (light == GL11.GL_LIGHT0) {
                copy(params, LightState.INSTANCE.getLight0());
            } else if (light == GL11.GL_LIGHT1) {
                copy(params, LightState.INSTANCE.getLight1());
            }
        }

        GL11.glLightfv(light, pname, params);
    }

    @Unique
    private static void copy(FloatBuffer src, FloatBuffer dst) {
        int oldPos = src.position();
        Matrix4f modelView = MatrixTracker.getModelViewCopy();
        Vector4f transformed = modelView.transform(new Vector4f(
                src.get(oldPos),
                src.get(oldPos + 1),
                src.get(oldPos + 2),
                src.get(oldPos + 3)
        ));

        dst.clear();
        dst.put(transformed.x);
        dst.put(transformed.y);
        dst.put(transformed.z);
        dst.put(transformed.w);
        dst.flip();
    }

}
