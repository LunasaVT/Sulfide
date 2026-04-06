package graphics.sulfide.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import graphics.sulfide.config.SulfideOptionStorage;
import graphics.sulfide.render.CloudFlatVbo;
import graphics.sulfide.render.SkyVboCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexBuffer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private ClientWorld world;
    @Shadow
    @Final
    private TextureManager textureManager;
    @Shadow
    private int ticks;
    @Shadow
    private boolean vbo;
    @Shadow
    private VertexBuffer lightSkyBuffer;
    @Shadow
    private VertexBuffer darkSkyBuffer;
    @Shadow
    private VertexBuffer starsBuffer;
    @Shadow
    private int lightSkyList;
    @Shadow
    private int darkSkyList;
    @Shadow
    private int starsList;

    @Unique
    private static final Identifier zdraw$SUN = new Identifier("textures/environment/sun.png");
    @Unique
    private static final Identifier zdraw$MOON = new Identifier("textures/environment/moon_phases.png");
    @Unique
    private static final Identifier zdraw$CLOUDS = new Identifier("textures/environment/clouds.png");

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void zdraw$renderSky(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
        if (!SulfideOptionStorage.getInstance().isEnableSkyRendering()) return;

        if (this.world.dimension.getType() == 1) {
            if (!SkyVboCache.isUploaded()) SkyVboCache.upload();
            this.textureManager.bindTexture(new Identifier("textures/environment/end_sky.png"));
            SkyVboCache.drawEndSky();
            ci.cancel();
            return;
        }

        if (!this.world.dimension.canPlayersSleep()) return;

        if (!SkyVboCache.isUploaded()) SkyVboCache.upload();

        Vec3d skyVec = this.world.method_3631(this.client.getCameraEntity(), tickDelta);
        float sr = (float) skyVec.x, sg = (float) skyVec.y, sb = (float) skyVec.z;
        if (anaglyphFilter != 2) {
            float mono = (sr * 30f + sg * 59f + sb * 11f) / 100f;
            float redC = (sr * 30f + sg * 70f) / 100f;
            float bluC = (sr * 30f + sb * 70f) / 100f;
            sr = mono;
            sg = redC;
            sb = bluC;
        }

        GlStateManager.disableTexture();
        GlStateManager.color(sr, sg, sb);
        GlStateManager.depthMask(false);
        GlStateManager.enableFog();
        GlStateManager.color(sr, sg, sb);

        if (this.vbo) {
            this.lightSkyBuffer.bind();
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
            this.lightSkyBuffer.draw(7);
            this.lightSkyBuffer.unbind();
            GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        } else {
            GlStateManager.callList(this.lightSkyList);
        }

        GlStateManager.disableFog();
        GlStateManager.disableAlphaTest();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        DiffuseLighting.disable();

        float[] sunsetColours = this.world.dimension.getBackgroundColor(
                this.world.getSkyAngle(tickDelta), tickDelta);
        if (sunsetColours != null) {
            GlStateManager.disableTexture();
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.pushMatrix();
            GlStateManager.rotate(90f, 1f, 0f, 0f);
            GlStateManager.rotate(
                    MathHelper.sin(this.world.getSkyAngleRadians(tickDelta)) < 0f ? 180f : 0f,
                    0f, 0f, 1f);
            GlStateManager.rotate(90f, 0f, 0f, 1f);

            float l = sunsetColours[0], m = sunsetColours[1], n = sunsetColours[2];
            if (anaglyphFilter != 2) {
                float o = (l * 30f + m * 59f + n * 11f) / 100f;
                float p = (l * 30f + m * 70f) / 100f;
                float q = (l * 30f + n * 70f) / 100f;
                l = o;
                m = p;
                n = q;
            }

            GL11.glBegin(GL11.GL_TRIANGLE_FAN);
            GL11.glColor4f(l, m, n, sunsetColours[3]);
            GL11.glVertex3f(0f, 100f, 0f);
            GL11.glColor4f(sunsetColours[0], sunsetColours[1], sunsetColours[2], 0f);
            for (int s = 0; s <= 16; ++s) {
                float q = (float) s * (float) Math.PI * 2f / 16f;
                float tx = MathHelper.sin(q);
                float tz = MathHelper.cos(q);
                GL11.glVertex3f(tx * 120f, tz * 120f, -tz * 40f * sunsetColours[3]);
            }
            GL11.glEnd();

            GlStateManager.popMatrix();
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        GlStateManager.enableTexture();
        GlStateManager.blendFuncSeparate(770, 1, 1, 0);
        GlStateManager.pushMatrix();

        float rainFade = 1f - this.world.getRainGradient(tickDelta);
        GlStateManager.color(1f, 1f, 1f, rainFade);
        GlStateManager.rotate(-90f, 0f, 1f, 0f);
        GlStateManager.rotate(this.world.getSkyAngle(tickDelta) * 360f, 1f, 0f, 0f);

        SkyVboCache.bindSky();

        this.textureManager.bindTexture(zdraw$SUN);
        SkyVboCache.drawSun();

        this.textureManager.bindTexture(zdraw$MOON);
        int moonPhase = this.world.getMoonPhase();
        SkyVboCache.drawMoon(moonPhase);

        SkyVboCache.unbindSky();

        GlStateManager.disableTexture();
        float starBright = this.world.method_3707(tickDelta) * rainFade;
        if (starBright > 0f) {
            GlStateManager.color(starBright, starBright, starBright, starBright);
            if (this.vbo) {
                this.starsBuffer.bind();
                GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
                this.starsBuffer.draw(7);
                this.starsBuffer.unbind();
                GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            } else {
                GlStateManager.callList(this.starsList);
            }
        }

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableFog();
        GlStateManager.popMatrix();

        GlStateManager.disableTexture();
        GlStateManager.color(0f, 0f, 0f);
        double horizonDist = this.client.player.getCameraPosVec(tickDelta).y
                - this.world.getHorizonHeight();

        if (horizonDist < 0.0) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0f, 12f, 0f);
            if (this.vbo) {
                this.darkSkyBuffer.bind();
                GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                GL11.glVertexPointer(3, GL11.GL_FLOAT, 12, 0L);
                this.darkSkyBuffer.draw(7);
                this.darkSkyBuffer.unbind();
                GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            } else {
                GlStateManager.callList(this.darkSkyList);
            }
            GlStateManager.popMatrix();

            float oy = -((float) (horizonDist + 65.0));
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glColor4f(0f, 0f, 0f, 1f);
            GL11.glVertex3f(-1f, oy, 1f);
            GL11.glVertex3f(1f, oy, 1f);
            GL11.glVertex3f(1f, -1f, 1f);
            GL11.glVertex3f(-1f, -1f, 1f);
            GL11.glVertex3f(-1f, -1f, -1f);
            GL11.glVertex3f(1f, -1f, -1f);
            GL11.glVertex3f(1f, oy, -1f);
            GL11.glVertex3f(-1f, oy, -1f);
            GL11.glVertex3f(1f, -1f, -1f);
            GL11.glVertex3f(1f, -1f, 1f);
            GL11.glVertex3f(1f, oy, 1f);
            GL11.glVertex3f(1f, oy, -1f);
            GL11.glVertex3f(-1f, oy, -1f);
            GL11.glVertex3f(-1f, oy, 1f);
            GL11.glVertex3f(-1f, -1f, 1f);
            GL11.glVertex3f(-1f, -1f, -1f);
            GL11.glVertex3f(-1f, -1f, -1f);
            GL11.glVertex3f(-1f, -1f, 1f);
            GL11.glVertex3f(1f, -1f, 1f);
            GL11.glVertex3f(1f, -1f, -1f);
            GL11.glEnd();
        }

        if (this.world.dimension.hasGround()) {
            GlStateManager.color(sr * 0.2f + 0.04f, sg * 0.2f + 0.04f, sb * 0.6f + 0.1f);
        } else {
            GlStateManager.color(sr, sg, sb);
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(0f, -((float) (horizonDist - 16.0)), 0f);
        GlStateManager.callList(this.darkSkyList);
        GlStateManager.popMatrix();

        GlStateManager.enableTexture();
        GlStateManager.depthMask(true);
        ci.cancel();
    }


    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void zdraw$renderClouds(float tickDelta, int anaglyphFilter, CallbackInfo ci) {
        if (!SulfideOptionStorage.getInstance().isEnableCloudRendering()) return;
        if (this.client.options.getCloudMode() == 2) return;
        if (!this.world.dimension.canPlayersSleep()) return;
        if (this.client.options.getCloudMode() == 0) return;

        if (!CloudFlatVbo.INSTANCE.isUploaded()) CloudFlatVbo.INSTANCE.upload();

        float cameraY = (float) (
                this.client.getCameraEntity().prevTickY
                        + (this.client.getCameraEntity().y - this.client.getCameraEntity().prevTickY)
                        * (double) tickDelta);

        double camX = this.client.getCameraEntity().prevX
                + (this.client.getCameraEntity().x - this.client.getCameraEntity().prevX)
                * (double) tickDelta
                + (double) ((float) this.ticks + tickDelta) * SulfideOptionStorage.getInstance().getCloudSpeed();
        double camZ = this.client.getCameraEntity().prevZ
                + (this.client.getCameraEntity().z - this.client.getCameraEntity().prevZ)
                * (double) tickDelta;

        int tileX = MathHelper.floor(camX / 2048.0);
        int tileZ = MathHelper.floor(camZ / 2048.0);
        camX -= tileX * 2048;
        camZ -= tileZ * 2048;

        float cloudY = this.world.dimension.getCloudHeight() - cameraY + 0.33F;
        float scrollS = (float) (camX * 4.8828125E-4);
        float scrollT = (float) (camZ * 4.8828125E-4);

        Vec3d cloudColor = this.world.getCloudColor(tickDelta);
        float r = (float) cloudColor.x, g = (float) cloudColor.y, b = (float) cloudColor.z;
        if (anaglyphFilter != 2) {
            float mono = (r * 30f + g * 59f + b * 11f) / 100f;
            float red = (r * 30f + g * 70f) / 100f;
            float blue = (r * 30f + b * 70f) / 100f;
            r = mono;
            g = red;
            b = blue;
        }

        GlStateManager.disableCull();
        this.textureManager.bindTexture(zdraw$CLOUDS);
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);

        CloudFlatVbo.INSTANCE.draw(cloudY, scrollS, scrollT, r, g, b,
                SulfideOptionStorage.getInstance().getCloudOpacity());

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();

        ci.cancel();
    }
}

