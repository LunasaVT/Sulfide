package graphics.sulfide.mixin;

import com.google.common.primitives.Floats;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sun.misc.Unsafe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.BitSet;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder {
    @Shadow
    private ByteBuffer buffer;
    @Shadow
    private IntBuffer intBuffer;
    @Shadow
    private int vertexCount;
    @Shadow
    private VertexFormatElement currentElement;
    @Shadow
    private int currentElementId;
    @Shadow
    private boolean textured;
    @Shadow
    private double offsetX;
    @Shadow
    private double offsetY;
    @Shadow
    private double offsetZ;
    @Shadow
    private VertexFormat format;

    @Shadow
    private void grow(int size) {
    }

    @Shadow
    private void nextElement() {
    }

    @Unique
    private static final Unsafe sulfide$U;
    @Unique
    private static final long sulfide$BUF_ADDR_OFF;

    static {
        try {
            var f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            sulfide$U = (Unsafe) f.get(null);
            sulfide$BUF_ADDR_OFF = sulfide$U.objectFieldOffset(
                    java.nio.Buffer.class.getDeclaredField("address")
            );
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Unique
    private long sulfide$addr;

    @Unique
    private void sulfide$refreshAddr() {
        sulfide$addr = sulfide$U.getLong(buffer, sulfide$BUF_ADDR_OFF);
    }

    @Inject(method = "begin", at = @At("RETURN"))
    private void sulfide$afterBegin(int drawMode, VertexFormat fmt, CallbackInfo ci) {
        sulfide$refreshAddr();
    }

    @Inject(method = "grow", at = @At("RETURN"))
    private void sulfide$afterGrow(int size, CallbackInfo ci) {
        sulfide$refreshAddr();
    }

    @Unique
    private long sulfide$elementPtr() {
        return sulfide$addr
                + (long) vertexCount * format.getVertexSize()
                + format.getIndex(currentElementId);
    }

    /**
     * @reason DMA
     * @author Lunasa
     */
    @Overwrite
    public BufferBuilder vertex(double x, double y, double z) {
        long p = sulfide$elementPtr();
        switch (currentElement.getFormat()) {
            case FLOAT:
                sulfide$U.putFloat(p, (float) (x + offsetX));
                sulfide$U.putFloat(p + 4, (float) (y + offsetY));
                sulfide$U.putFloat(p + 8, (float) (z + offsetZ));
                break;
            case UNSIGNED_INT:
            case INT:
                sulfide$U.putInt(p, Float.floatToRawIntBits((float) (x + offsetX)));
                sulfide$U.putInt(p + 4, Float.floatToRawIntBits((float) (y + offsetY)));
                sulfide$U.putInt(p + 8, Float.floatToRawIntBits((float) (z + offsetZ)));
                break;
            case UNSIGNED_SHORT:
            case SHORT:
                sulfide$U.putShort(p, (short) (int) (x + offsetX));
                sulfide$U.putShort(p + 2, (short) (int) (y + offsetY));
                sulfide$U.putShort(p + 4, (short) (int) (z + offsetZ));
                break;
            case UNSIGNED_BYTE:
            case BYTE:
                sulfide$U.putByte(p, (byte) (int) (x + offsetX));
                sulfide$U.putByte(p + 1, (byte) (int) (y + offsetY));
                sulfide$U.putByte(p + 2, (byte) (int) (z + offsetZ));
                break;
        }
        nextElement();
        return (BufferBuilder) (Object) this;
    }

    /**
     * @reason DMA
     * @author Lunasa
     */
    @Overwrite
    public BufferBuilder color(int red, int green, int blue, int alpha) {
        if (textured) return (BufferBuilder) (Object) this;
        long p = sulfide$elementPtr();
        switch (currentElement.getFormat()) {
            case FLOAT:
                sulfide$U.putFloat(p, red / 255.0F);
                sulfide$U.putFloat(p + 4, green / 255.0F);
                sulfide$U.putFloat(p + 8, blue / 255.0F);
                sulfide$U.putFloat(p + 12, alpha / 255.0F);
                break;
            case UNSIGNED_INT:
            case INT:
                sulfide$U.putFloat(p, (float) red);
                sulfide$U.putFloat(p + 4, (float) green);
                sulfide$U.putFloat(p + 8, (float) blue);
                sulfide$U.putFloat(p + 12, (float) alpha);
                break;
            case UNSIGNED_SHORT:
            case SHORT:
                sulfide$U.putShort(p, (short) red);
                sulfide$U.putShort(p + 2, (short) green);
                sulfide$U.putShort(p + 4, (short) blue);
                sulfide$U.putShort(p + 6, (short) alpha);
                break;
            case UNSIGNED_BYTE:
            case BYTE:
                if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                    sulfide$U.putByte(p, (byte) red);
                    sulfide$U.putByte(p + 1, (byte) green);
                    sulfide$U.putByte(p + 2, (byte) blue);
                    sulfide$U.putByte(p + 3, (byte) alpha);
                } else {
                    sulfide$U.putByte(p, (byte) alpha);
                    sulfide$U.putByte(p + 1, (byte) blue);
                    sulfide$U.putByte(p + 2, (byte) green);
                    sulfide$U.putByte(p + 3, (byte) red);
                }
                break;
        }
        nextElement();
        return (BufferBuilder) (Object) this;
    }

    /**
     * @reason DMA
     * @author Lunasa
     */
    @Overwrite
    public BufferBuilder texture(double u, double v) {
        long p = sulfide$elementPtr();
        switch (currentElement.getFormat()) {
            case FLOAT:
                sulfide$U.putFloat(p, (float) u);
                sulfide$U.putFloat(p + 4, (float) v);
                break;
            case UNSIGNED_INT:
            case INT:
                sulfide$U.putInt(p, (int) u);
                sulfide$U.putInt(p + 4, (int) v);
                break;
            case UNSIGNED_SHORT:
            case SHORT:
                sulfide$U.putShort(p, (short) (int) v);
                sulfide$U.putShort(p + 2, (short) (int) u);
                break;
            case UNSIGNED_BYTE:
            case BYTE:
                sulfide$U.putByte(p, (byte) (int) v);
                sulfide$U.putByte(p + 1, (byte) (int) u);
                break;
        }
        nextElement();
        return (BufferBuilder) (Object) this;
    }

    /**
     * @reason DMA
     * @author Lunasa
     */
    @Overwrite
    public BufferBuilder texture2(int u, int v) {
        long p = sulfide$elementPtr();
        switch (currentElement.getFormat()) {
            case FLOAT:
                sulfide$U.putFloat(p, (float) u);
                sulfide$U.putFloat(p + 4, (float) v);
                break;
            case UNSIGNED_INT:
            case INT:
                sulfide$U.putInt(p, u);
                sulfide$U.putInt(p + 4, v);
                break;
            case UNSIGNED_SHORT:
            case SHORT:
                sulfide$U.putShort(p, (short) v);
                sulfide$U.putShort(p + 2, (short) u);
                break;
            case UNSIGNED_BYTE:
            case BYTE:
                sulfide$U.putByte(p, (byte) v);
                sulfide$U.putByte(p + 1, (byte) u);
                break;
        }
        nextElement();
        return (BufferBuilder) (Object) this;
    }

    /**
     * @reason DMA
     * @author Lunasa
     */
    @Overwrite
    public BufferBuilder normal(float x, float y, float z) {
        long p = sulfide$elementPtr();
        switch (currentElement.getFormat()) {
            case FLOAT:
                sulfide$U.putFloat(p, x);
                sulfide$U.putFloat(p + 4, y);
                sulfide$U.putFloat(p + 8, z);
                break;
            case UNSIGNED_INT:
            case INT:
                sulfide$U.putInt(p, (int) x);
                sulfide$U.putInt(p + 4, (int) y);
                sulfide$U.putInt(p + 8, (int) z);
                break;
            case UNSIGNED_SHORT:
            case SHORT:
                sulfide$U.putShort(p, (short) ((int) x * 32767 & 0xFFFF));
                sulfide$U.putShort(p + 2, (short) ((int) y * 32767 & 0xFFFF));
                sulfide$U.putShort(p + 4, (short) ((int) z * 32767 & 0xFFFF));
                break;
            case UNSIGNED_BYTE:
            case BYTE:
                sulfide$U.putByte(p, (byte) ((int) x * 127 & 0xFF));
                sulfide$U.putByte(p + 1, (byte) ((int) y * 127 & 0xFF));
                sulfide$U.putByte(p + 2, (byte) ((int) z * 127 & 0xFF));
                break;
        }
        nextElement();
        return (BufferBuilder) (Object) this;
    }

    /**
     * @reason DMA
     * @author Lunasa
     */
    @Overwrite
    public void faceTexture2(int i, int j, int k, int l) {
        int m = (vertexCount - 4) * format.getVertexSizeInteger()
                + format.getUvIndex(1) / 4;
        int n = format.getVertexSize() >> 2;
        long base = sulfide$addr + (long) m * 4;
        long stride = (long) n * 4;
        sulfide$U.putInt(base, i);
        sulfide$U.putInt(base + stride, j);
        sulfide$U.putInt(base + stride * 2, k);
        sulfide$U.putInt(base + stride * 3, l);
    }

    /**
     * @reason DMA
     * @author Lunasa
     */
    @Overwrite
    public void postProcessFacePosition(double d, double e, double f) {
        int stride = format.getVertexSizeInteger();
        int base = (vertexCount - 4) * stride;
        for (int v = 0; v < 4; v++) {
            long p = sulfide$addr + ((long) base + (long) v * stride) * 4;
            sulfide$U.putInt(p, Float.floatToRawIntBits(
                    (float) (d + offsetX) + Float.intBitsToFloat(sulfide$U.getInt(p))));
            sulfide$U.putInt(p + 4, Float.floatToRawIntBits(
                    (float) (e + offsetY) + Float.intBitsToFloat(sulfide$U.getInt(p + 4))));
            sulfide$U.putInt(p + 8, Float.floatToRawIntBits(
                    (float) (f + offsetZ) + Float.intBitsToFloat(sulfide$U.getInt(p + 8))));
        }
    }

    /**
     * @reason DMA
     * @author Lunasa
     */
    @Overwrite
    public void faceTint(float r, float g, float b, int i) {
        int j = ((vertexCount - i) * format.getVertexSize()
                + format.getColorIndex()) / 4;
        long p = sulfide$addr + (long) j * 4;
        int k = -1;
        if (!textured) {
            k = sulfide$U.getInt(p);
            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                int lr = (int) ((float) (k & 0xFF) * r);
                int lg = (int) ((float) ((k >> 8) & 0xFF) * g);
                int lb = (int) ((float) ((k >> 16) & 0xFF) * b);
                k = (k & 0xFF000000) | (lb << 16) | (lg << 8) | lr;
            } else {
                int lr = (int) ((float) ((k >> 24) & 0xFF) * r);
                int lg = (int) ((float) ((k >> 16) & 0xFF) * g);
                int lb = (int) ((float) ((k >> 8) & 0xFF) * b);
                k = (k & 0xFF) | (lr << 24) | (lg << 16) | (lb << 8);
            }
        }
        sulfide$U.putInt(p, k);
    }

    /**
     * @reason DMA
     * @author Lunasa
     */
    @Overwrite
    private void putColor(int index, int red, int green, int blue, int alpha) {
        long p = sulfide$addr + (long) index * 4;
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            sulfide$U.putInt(p, (alpha << 24) | (blue << 16) | (green << 8) | red);
        } else {
            sulfide$U.putInt(p, (red << 24) | (green << 16) | (blue << 8) | alpha);
        }
    }

    /**
     * @reason DMA
     * @author Lunasa
     */
    @Overwrite
    public void putNormal(float x, float y, float z) {
        int nx = (byte) ((int) (x * 127.0F)) & 0xFF;
        int ny = (byte) ((int) (y * 127.0F)) & 0xFF;
        int nz = (byte) ((int) (z * 127.0F)) & 0xFF;
        int packed = nx | (ny << 8) | (nz << 16);
        int stride = format.getVertexSize() >> 2;
        int n = (vertexCount - 4) * stride + format.getNormalIndex() / 4;
        long base = sulfide$addr + (long) n * 4;
        long s = (long) stride * 4;
        sulfide$U.putInt(base, packed);
        sulfide$U.putInt(base + s, packed);
        sulfide$U.putInt(base + s * 2, packed);
        sulfide$U.putInt(base + s * 3, packed);
    }

    /**
     * @reason DMA
     * @author Lunasa
     */
    @Overwrite
    public void putArray(int[] data) {
        grow(data.length);
        int pos = vertexCount * format.getVertexSizeInteger();
        sulfide$U.copyMemory(
                data, Unsafe.ARRAY_INT_BASE_OFFSET,
                null, sulfide$addr + (long) pos * 4,
                (long) data.length * 4
        );
        vertexCount += data.length / format.getVertexSizeInteger();
        // keep intBuffer.position in sync for grow()'s remaining() check
        intBuffer.position(vertexCount * format.getVertexSizeInteger());
    }

    /**
     * @reason DMA
     * @author Lunasa
     */
    @Overwrite
    public void sortQuads(float cameraX, float cameraY, float cameraZ) {
        int quadCount = vertexCount / 4;
        float[] distances = new float[quadCount];

        int vertInts = format.getVertexSizeInteger();
        int vertBytes = format.getVertexSize();
        long addr = sulfide$addr;
        long vs = (long) vertInts * 4;

        float adjX = (float) ((double) cameraX + offsetX);
        float adjY = (float) ((double) cameraY + offsetY);
        float adjZ = (float) ((double) cameraZ + offsetZ);

        for (int q = 0; q < quadCount; q++) {
            long qb = addr + (long) q * vertBytes * 4L;

            float x0 = sulfide$U.getFloat(qb);
            float y0 = sulfide$U.getFloat(qb + 4);
            float z0 = sulfide$U.getFloat(qb + 8);
            float x1 = sulfide$U.getFloat(qb + vs);
            float y1 = sulfide$U.getFloat(qb + vs + 4);
            float z1 = sulfide$U.getFloat(qb + vs + 8);
            float x2 = sulfide$U.getFloat(qb + vs * 2);
            float y2 = sulfide$U.getFloat(qb + vs * 2 + 4);
            float z2 = sulfide$U.getFloat(qb + vs * 2 + 8);
            float x3 = sulfide$U.getFloat(qb + vs * 3);
            float y3 = sulfide$U.getFloat(qb + vs * 3 + 4);
            float z3 = sulfide$U.getFloat(qb + vs * 3 + 8);

            float dx = (x0 + x1 + x2 + x3) * 0.25F - adjX;
            float dy = (y0 + y1 + y2 + y3) * 0.25F - adjY;
            float dz = (z0 + z1 + z2 + z3) * 0.25F - adjZ;
            distances[q] = dx * dx + dy * dy + dz * dz;
        }

        Integer[] indices = new Integer[quadCount];
        for (int i = 0; i < quadCount; i++) indices[i] = i;
        final float[] dists = distances;
        Arrays.sort(indices, (a, b) -> Floats.compare(dists[b], dists[a]));

        int l = vertBytes;
        long quadBytes = (long) l * 4;
        int[] temp = new int[l];
        BitSet done = new BitSet();

        for (int m = 0; (m = done.nextClearBit(m)) < indices.length; m++) {
            int target = indices[m];
            if (target != m) {
                sulfide$U.copyMemory(
                        null, addr + (long) target * quadBytes,
                        temp, Unsafe.ARRAY_INT_BASE_OFFSET,
                        quadBytes
                );

                int cur = target;
                for (int nxt = indices[target]; cur != m; nxt = indices[nxt]) {
                    sulfide$U.copyMemory(
                            addr + (long) nxt * quadBytes,
                            addr + (long) cur * quadBytes,
                            quadBytes
                    );
                    done.set(cur);
                    cur = nxt;
                }

                sulfide$U.copyMemory(
                        temp, Unsafe.ARRAY_INT_BASE_OFFSET,
                        null, addr + (long) m * quadBytes,
                        quadBytes
                );
            }
            done.set(m);
        }
    }
}