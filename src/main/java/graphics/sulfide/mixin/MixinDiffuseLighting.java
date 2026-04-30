package graphics.sulfide.mixin;

import graphics.sulfide.engine.LightState;
import net.minecraft.client.render.DiffuseLighting;
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

        dst.clear();
        dst.put(src.get(oldPos));
        dst.put(src.get(oldPos + 1));
        dst.put(src.get(oldPos + 2));
        dst.put(src.get(oldPos + 3));
        dst.flip();
    }

}