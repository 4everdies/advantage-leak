/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.client;

import net.minecraft.util.ResourceLocation;

public class BgUtils {
    private static BgUtils instance;
    private final ResourceLocation[] backgroundImages = new ResourceLocation[]{new ResourceLocation("advantage/images/mainmenu.jpg"), new ResourceLocation("advantage/images/mainmenu2.png"), new ResourceLocation("advantage/images/mainmenu3.jpg"), new ResourceLocation("advantage/images/mainmenu4.png")};
    private int currentBackgroundIndex = 0;

    private BgUtils() {
    }

    public static BgUtils getInstance() {
        if (instance == null) {
            instance = new BgUtils();
        }
        return instance;
    }

    public ResourceLocation getCurrentBackground() {
        return this.backgroundImages[this.currentBackgroundIndex];
    }

    public void cycleBackground() {
        this.currentBackgroundIndex = (this.currentBackgroundIndex + 1) % this.backgroundImages.length;
    }

    public int getCurrentIndex() {
        return this.currentBackgroundIndex;
    }
}

