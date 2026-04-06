package graphics.sulfide.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import graphics.sulfide.config.SulfideOptionStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

@Mixin(TextRenderer.class)
public class MixinTextRenderer {
    @Shadow
    @Final
    private static Identifier[] PAGES;
    @Shadow
    private int[] characterWidths;
    @Shadow
    private byte[] glyphWidths;
    @Shadow
    private boolean unicode;
    @Shadow
    @Final
    private TextureManager textureManager;
    @Shadow
    @Final
    private Identifier fontTexture;
    @Shadow
    private float x;
    @Shadow
    private float y;
    @Shadow
    private boolean obfuscated;
    @Shadow
    private boolean bold;
    @Shadow
    private boolean italic;
    @Shadow
    private boolean underline;
    @Shadow
    private boolean strikethrough;
    @Shadow
    private int[] colorCodes;
    @Shadow
    private float red;
    @Shadow
    private float green;
    @Shadow
    private float blue;
    @Shadow
    private float alpha;
    @Shadow
    private int color;
    @Shadow
    public Random random;
    @Shadow
    public int fontHeight;

    @Unique // for fast lookup
    private static final String zdraw$TABLE =
            "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5"
                    + "\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e"
                    + "\u015f\u0174\u0175\u017e\u0207"
                    + "\u0000\u0000\u0000\u0000\u0000\u0000\u0000"
                    + " !\"#$%&'()*+,-./0123456789:;<=>?"
                    + "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_"
                    + "`abcdefghijklmnopqrstuvwxyz{|}~\u0000"
                    + "\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb"
                    + "\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4"
                    + "\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8"
                    + "\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba"
                    + "\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb"
                    + "\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563"
                    + "\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c"
                    + "\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550"
                    + "\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b"
                    + "\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580"
                    + "\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398"
                    + "\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264"
                    + "\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2"
                    + "\u25a0\u0000";

    @Unique
    private static final int[] zdraw$CHAR_INDEX = new int[65536];

    static {
        Arrays.fill(zdraw$CHAR_INDEX, -1);
        for (int i = zdraw$TABLE.length() - 1; i >= 0; i--) {
            zdraw$CHAR_INDEX[zdraw$TABLE.charAt(i)] = i;
        }
    }

    @Unique
    private final Map<String, Integer> zdraw$widthCache =
            new LinkedHashMap<String, Integer>(512, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
                    return size() > SulfideOptionStorage.getInstance().getTextWidthCacheSize();
                }
            };

    @Unique
    private static final int zdraw$PAGE_NONE = Integer.MIN_VALUE;
    @Unique
    private static final int zdraw$PAGE_DEFAULT = -1;

    @Unique
    private int zdraw$currentPage;
    @Unique
    private boolean zdraw$batchActive;

    @Unique
    private static final int zdraw$DECO_STRIDE = 8;
    @Unique
    private static final int zdraw$MAX_DECOS = 256;
    @Unique
    private final float[] zdraw$decoData = new float[zdraw$MAX_DECOS * zdraw$DECO_STRIDE];

    @Inject(method = "reload", at = @At("HEAD"))
    private void zdraw$onReload(ResourceManager mgr, CallbackInfo ci) {
        zdraw$widthCache.clear();
    }

    @Inject(method = "setUnicode", at = @At("HEAD"))
    private void zdraw$onSetUnicode(boolean unicode, CallbackInfo ci) {
        zdraw$widthCache.clear();
    }

    /**
     * @reason Cached width computaton
     * @author Lunasa
     */
    @Overwrite
    public int getCharWidth(char character) {
        if (character == 167) return -1;
        if (character == ' ') return 4;

        int i = zdraw$CHAR_INDEX[character];
        if (character > 0 && i != -1 && !this.unicode) {
            return this.characterWidths[i];
        }
        if (this.glyphWidths[character] != 0) {
            int j = this.glyphWidths[character] >>> 4;
            int k = this.glyphWidths[character] & 15;
            if (k > 7) {
                k = 15;
                j = 0;
            }
            ++k;
            return (k - j) / 2 + 1;
        }
        return 0;
    }

    /**
     * @reason Cached width computaton
     * @author Lunasa
     */
    @Overwrite
    public int getStringWidth(String text) {
        if (text == null) return 0;

        boolean cacheEnabled = SulfideOptionStorage.getInstance().isEnableTextWidthCache();
        if (cacheEnabled) {
            Integer cached = zdraw$widthCache.get(text);
            if (cached != null) return cached;
        }

        int width = 0;
        boolean bl = false;

        for (int j = 0; j < text.length(); ++j) {
            char c = text.charAt(j);
            int k = this.getCharWidth(c);
            if (k < 0 && j < text.length() - 1) {
                ++j;
                c = text.charAt(j);
                if (c == 'l' || c == 'L') {
                    bl = true;
                } else if (c == 'r' || c == 'R') {
                    bl = false;
                }
                k = 0;
            }
            width += k;
            if (bl && k > 0) ++width;
        }

        if (cacheEnabled) {
            zdraw$widthCache.put(text, width);
        }
        return width;
    }

    /**
     * @reason Batched text rendering
     * @author Lunasa
     */
    @Overwrite
    private void draw(String text, boolean shadow) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        zdraw$currentPage = zdraw$PAGE_NONE;
        zdraw$batchActive = false;
        int zdraw$decoCount = 0;

        float curR = this.red, curG = this.green, curB = this.blue, curA = this.alpha;

        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);

            if (c == 167 && i + 1 < text.length()) {
                int j = zdraw$formattingIndex(text.charAt(i + 1));

                if (j < 16) {
                    this.obfuscated = false;
                    this.bold = false;
                    this.strikethrough = false;
                    this.underline = false;
                    this.italic = false;
                    if (j < 0) j = 15;
                    if (shadow) j += 16;
                    int k = this.colorCodes[j];
                    this.color = k;

                    zdraw$flushBatch(tess);
                    curR = (float) (k >> 16 & 255) / 255.0F;
                    curG = (float) (k >> 8 & 255) / 255.0F;
                    curB = (float) (k & 255) / 255.0F;
                    curA = this.alpha;
                    GlStateManager.color(curR, curG, curB, curA);
                } else if (j == 16) {
                    this.obfuscated = true;
                } else if (j == 17) {
                    this.bold = true;
                } else if (j == 18) {
                    this.strikethrough = true;
                } else if (j == 19) {
                    this.underline = true;
                } else if (j == 20) {
                    this.italic = true;
                } else if (j == 21) {
                    this.obfuscated = false;
                    this.bold = false;
                    this.strikethrough = false;
                    this.underline = false;
                    this.italic = false;
                    zdraw$flushBatch(tess);
                    curR = this.red;
                    curG = this.green;
                    curB = this.blue;
                    curA = this.alpha;
                    GlStateManager.color(curR, curG, curB, curA);
                }
                ++i;
                continue;
            }

            int j = zdraw$CHAR_INDEX[c];

            if (this.obfuscated && j != -1) {
                int charW = this.getCharWidth(c);
                char d;
                do {
                    d = zdraw$TABLE.charAt(this.random.nextInt(zdraw$TABLE.length()));
                } while (charW != this.getCharWidth(d));
                c = d;
                j = zdraw$CHAR_INDEX[c];
            }

            float f = this.unicode ? 0.5F : 1.0F;
            boolean bl = (c == 0 || j == -1 || this.unicode) && shadow;
            if (bl) {
                this.x -= f;
                this.y -= f;
            }

            float g = zdraw$renderGlyph(c, j, this.italic, tess, buf);

            if (bl) {
                this.x += f;
                this.y += f;
            }

            // bold: draw second copy offset by 1
            if (this.bold) {
                this.x += f;
                if (bl) {
                    this.x -= f;
                    this.y -= f;
                }
                zdraw$renderGlyph(c, j, this.italic, tess, buf);
                this.x -= f;
                if (bl) {
                    this.x += f;
                    this.y += f;
                }
                ++g;
            }

            if (this.strikethrough && zdraw$decoCount < zdraw$MAX_DECOS) {
                int di = zdraw$decoCount++ * zdraw$DECO_STRIDE;
                float yc = this.y + (float) (this.fontHeight / 2);
                zdraw$decoData[di] = this.x;
                zdraw$decoData[di + 1] = yc - 1.0F;
                zdraw$decoData[di + 2] = this.x + g;
                zdraw$decoData[di + 3] = yc;
                zdraw$decoData[di + 4] = curR;
                zdraw$decoData[di + 5] = curG;
                zdraw$decoData[di + 6] = curB;
                zdraw$decoData[di + 7] = curA;
            }
            if (this.underline && zdraw$decoCount < zdraw$MAX_DECOS) {
                int di = zdraw$decoCount++ * zdraw$DECO_STRIDE;
                float yb = this.y + (float) this.fontHeight;
                zdraw$decoData[di] = this.x - 1.0F;
                zdraw$decoData[di + 1] = yb - 1.0F;
                zdraw$decoData[di + 2] = this.x + g;
                zdraw$decoData[di + 3] = yb;
                zdraw$decoData[di + 4] = curR;
                zdraw$decoData[di + 5] = curG;
                zdraw$decoData[di + 6] = curB;
                zdraw$decoData[di + 7] = curA;
            }

            this.x += (float) ((int) g);
        }

        // flush remaining glyph batch
        zdraw$flushBatch(tess);

        // draw every decoration in a single batch
        if (zdraw$decoCount > 0) {
            GlStateManager.disableTexture();
            buf.begin(7, VertexFormats.POSITION);
            float prevR = curR, prevG = curG, prevB = curB, prevA = curA;
            for (int d = 0; d < zdraw$decoCount; d++) {
                int di = d * zdraw$DECO_STRIDE;
                float dr = zdraw$decoData[di + 4];
                float dg = zdraw$decoData[di + 5];
                float db = zdraw$decoData[di + 6];
                float da = zdraw$decoData[di + 7];
                // flush + restart if color changed
                if (dr != prevR || dg != prevG || db != prevB || da != prevA) {
                    tess.draw();
                    GlStateManager.color(dr, dg, db, da);
                    buf.begin(7, VertexFormats.POSITION);
                    prevR = dr;
                    prevG = dg;
                    prevB = db;
                    prevA = da;
                } else if (d == 0) {
                    GlStateManager.color(dr, dg, db, da);
                }
                float x1 = zdraw$decoData[di];
                float y1 = zdraw$decoData[di + 1];
                float x2 = zdraw$decoData[di + 2];
                float y2 = zdraw$decoData[di + 3];
                buf.vertex(x1, y2, 0.0).next();
                buf.vertex(x2, y2, 0.0).next();
                buf.vertex(x2, y1, 0.0).next();
                buf.vertex(x1, y1, 0.0).next();
            }
            tess.draw();
            GlStateManager.enableTexture();
        }
    }

    @Unique
    private float zdraw$renderGlyph(char c, int tableIdx, boolean italic,
                                    Tessellator tess, BufferBuilder buf) {
        if (c == ' ') return 4.0F;

        if (tableIdx != -1 && !this.unicode) {
            zdraw$ensurePage(zdraw$PAGE_DEFAULT, tess, buf);
            return zdraw$addNormalGlyph(tableIdx, italic, buf);
        } else {
            if (this.glyphWidths[c] == 0) return 0.0F;
            zdraw$ensurePage(c / 256, tess, buf);
            return zdraw$addUnicodeGlyph(c, italic, buf);
        }
    }

    @Unique
    private float zdraw$addNormalGlyph(int idx, boolean italic, BufferBuilder buf) {
        int texU = (idx % 16) * 8;
        int texV = (idx / 16) * 8;
        int slant = italic ? 1 : 0;
        int w = this.characterWidths[idx];
        float fw = (float) w - 0.01F;

        float u0 = (float) texU / 128.0F;
        float v0 = (float) texV / 128.0F;
        float u1 = ((float) texU + fw - 1.0F) / 128.0F;
        float v1 = ((float) texV + 7.99F) / 128.0F;

        buf.vertex(this.x + (float) slant, this.y, 0.0).texture(u0, v0).next();
        buf.vertex(this.x - (float) slant, this.y + 7.99, 0.0).texture(u0, v1).next();
        buf.vertex(this.x + fw - 1.0F - (float) slant, this.y + 7.99, 0.0).texture(u1, v1).next();
        buf.vertex(this.x + fw - 1.0F + (float) slant, this.y, 0.0).texture(u1, v0).next();

        return (float) w;
    }

    @Unique
    private float zdraw$addUnicodeGlyph(char c, boolean italic, BufferBuilder buf) {
        int rawStart = this.glyphWidths[c] >>> 4;
        int rawEnd = this.glyphWidths[c] & 15;
        float gStart = (float) rawStart;
        float gEnd = (float) (rawEnd + 1);
        float texX = (float) (c % 16 * 16) + gStart;
        float texY = (float) ((c & 255) / 16 * 16);
        float glyphW = gEnd - gStart - 0.02F;
        float slant = italic ? 1.0F : 0.0F;

        float u0 = texX / 256.0F;
        float v0 = texY / 256.0F;
        float u1 = (texX + glyphW) / 256.0F;
        float v1 = (texY + 15.98F) / 256.0F;

        buf.vertex(this.x + slant, this.y, 0.0).texture(u0, v0).next();
        buf.vertex(this.x - slant, this.y + 7.99, 0.0).texture(u0, v1).next();
        buf.vertex(this.x + glyphW / 2.0F - slant, this.y + 7.99, 0.0).texture(u1, v1).next();
        buf.vertex(this.x + glyphW / 2.0F + slant, this.y, 0.0).texture(u1, v0).next();

        return (gEnd - gStart) / 2.0F + 1.0F;
    }

    @Unique
    private void zdraw$ensurePage(int page, Tessellator tess, BufferBuilder buf) {
        if (zdraw$currentPage != page) {
            zdraw$flushBatch(tess);
            if (page == zdraw$PAGE_DEFAULT) {
                this.textureManager.bindTexture(this.fontTexture);
            } else {
                this.textureManager.bindTexture(zdraw$getFontPage(page));
            }
            zdraw$currentPage = page;
        }
        if (!zdraw$batchActive) {
            buf.begin(7, VertexFormats.POSITION_TEXTURE);
            zdraw$batchActive = true;
        }
    }

    @Unique
    private void zdraw$flushBatch(Tessellator tess) {
        if (zdraw$batchActive) {
            tess.draw();
            zdraw$batchActive = false;
        }
    }

    @Unique
    private static Identifier zdraw$getFontPage(int page) {
        if (PAGES[page] == null) {
            PAGES[page] = new Identifier(String.format("textures/font/unicode_page_%02x.png", page));
        }
        return PAGES[page];
    }

    @Unique
    private static int zdraw$formattingIndex(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        if (c >= 'A' && c <= 'F') return c - 'A' + 10;
        return switch (c) {
            case 'k', 'K' -> 16;
            case 'l', 'L' -> 17;
            case 'm', 'M' -> 18;
            case 'n', 'N' -> 19;
            case 'o', 'O' -> 20;
            case 'r', 'R' -> 21;
            default -> -1;
        };
    }
}