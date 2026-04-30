package graphics.sulfide.mixin;

import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.FloatBuffer;

@Mixin(Camera.class)
public interface CameraAccess {
    @Accessor("PROJECTION_MATRIX")
    static FloatBuffer getProjectionMatrix() { throw new AssertionError(); };
}
