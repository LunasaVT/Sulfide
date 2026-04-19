package graphics.sulfide.mixin;

import graphics.sulfide.render.SulfideState;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.render.BlockBreakingInfo;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL45C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;
import java.util.Map;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public class MixinSodiumWorldRenderer {
    @Unique
    private static final FloatBuffer sulfide$projBuf = BufferUtils.createFloatBuffer(16);

    @Inject(method = "renderBlockEntities(Ljava/util/Map;F)V", at = @At("HEAD"))
    void sulfide$renderBlockEntities(Map<Integer, BlockBreakingInfo> blockBreakingProgressions, float tickDelta, CallbackInfo ci) {
        sulfide$projBuf.clear();
        GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, sulfide$projBuf);
        sulfide$projBuf.rewind();
        SulfideState.INSTANCE.endFrame(new Matrix4f(sulfide$projBuf));

        GL45C.glBindVertexArray(0);
        GL30C.glUseProgram(0);
        GL30C.glBindBuffer(GL30C.GL_ARRAY_BUFFER, 0);
        GL30C.glBindBuffer(GL30C.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
}
