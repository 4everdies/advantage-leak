/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

@ModuleInfo(label="Client Settings", category=ModuleCategory.CLIENT)
public final class ClientSettingsModule
extends Module {
    public static final ModeProperty<ClickInterface> clickInterface = new ModeProperty<ClickInterface>("Click Interface", ClickInterface.Normal);
    public static final ModeProperty<Font> font = new ModeProperty<Font>("Font", Font.MC);
    public static final ModeProperty<Color> color = new ModeProperty<Color>("Color", Color.Advantage);
    public static final ModeProperty<Anime> anime = new ModeProperty<Anime>("Anime", Anime.Onikata);
    public static final Property<String> customAnimeUrl = new Property<String>("Custom URL", "", () -> anime.getValue() == Anime.Custom);
    public static final Property<Boolean> showInGame = new Property<Boolean>("Show In-Game", false, () -> anime.getValue() != Anime.None);
    public static final Property<Boolean> showInInventory = new Property<Boolean>("Show In Inventory", true, () -> anime.getValue() != Anime.None);
    public static final Property<Boolean> blinkCancelsIncoming = new Property<Boolean>("Blink Cancels Incoming", true);
    private static final Map<String, ResourceLocation> cachedImages = new HashMap<String, ResourceLocation>();
    @EventLink
    public final Listener<Render2DEvent> render2DEventListener = event -> {
        if (showInGame.getValue().booleanValue() && Util.mc.thePlayer != null && Util.mc.currentScreen == null) {
            ScaledResolution sr = new ScaledResolution(Util.mc);
            ClientSettingsModule.renderAnimeImage(sr.getScaledWidth(), sr.getScaledHeight());
        }
    };

    public ClientSettingsModule() {
        this.toggle();
        this.setHidden(true);
    }

    public static ResourceLocation getAnimeImage(Anime animeType) {
        switch (animeType.ordinal()) {
            case 0: {
                return new ResourceLocation("advantage/images/onikata.png");
            }
            case 1: {
                return new ResourceLocation("advantage/images/takanashi.png");
            }
            case 2: {
                return new ResourceLocation("advantage/images/io.png");
            }
            case 3: {
                return new ResourceLocation("advantage/images/zerotwo.png");
            }
            case 4: {
                return new ResourceLocation("advantage/images/astolfo.png");
            }
            case 5: {
                return new ResourceLocation("advantage/images/felix.png");
            }
            case 6: {
                return new ResourceLocation("advantage/images/rem.png");
            }
            case 7: {
                return new ResourceLocation("advantage/images/ram.png");
            }
            case 8: {
                return ClientSettingsModule.loadCustomImage();
            }
        }
        return null;
    }

    public static void renderAnimeImage(int width, int height) {
        if (anime.getValue() == Anime.None) {
            return;
        }
        ResourceLocation imageResource = ClientSettingsModule.getAnimeImage((Anime)((Object)anime.getValue()));
        if (imageResource == null) {
            return;
        }
        int[] dimensions = RenderUtils.getImageDimensions(imageResource);
        RenderUtils.drawImage(imageResource, (float)width - (float)dimensions[0] / 3.0f, (float)height / 3.0f, (float)dimensions[0] / 3.0f, (float)dimensions[1] / 3.0f);
    }

    private static ResourceLocation loadCustomImage() {
        String url = customAnimeUrl.getValue();
        if (url == null || url.isEmpty()) {
            return null;
        }
        if (cachedImages.containsKey(url)) {
            return cachedImages.get(url);
        }
        new Thread(() -> {
            try {
                BufferedImage image = ImageIO.read(new URL(url));
                Util.mc.addScheduledTask(() -> {
                    try {
                        DynamicTexture texture = new DynamicTexture(image);
                        ResourceLocation location = Util.mc.getTextureManager().getDynamicTextureLocation("custom_anime", texture);
                        cachedImages.put(url, location);
                    }
                    catch (Exception e) {
                        System.err.println("Failed to create texture: " + e.getMessage());
                    }
                });
            }
            catch (Exception e) {
                System.err.println("Failed to load custom anime image: " + e.getMessage());
            }
        }).start();
        return null;
    }

    public static enum Anime {
        Onikata("Onikata"),
        Takanashi("Takanashi"),
        Io("Io"),
        ZeroTwo("Zero Two"),
        Astolfo("Astolfo"),
        Felix("Felix"),
        Rem("Rem"),
        Ram("Ram"),
        Custom("Custom"),
        None("None");

        public String name;

        private Anime(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    public static enum ClickInterface {
        Normal,
        Window;

    }

    public static enum Font {
        Advantage,
        Bold,
        Noto,
        Arial,
        Apple,
        Sans,
        Convection,
        Tahoma,
        SFUI,
        IntelliJ,
        Verdana,
        MC;

    }

    public static enum Color {
        Rainbow("Rainbow"),
        Exhibition("Exhibition"),
        Astolfo("Astolfo"),
        Advantage("Advantage"),
        Tenacity("Tenacity"),
        FDP("FDP"),
        Rise("Rise"),
        Vaporwave("Vaporwave"),
        Sunset("Sunset"),
        White("White"),
        Ruby("Ruby"),
        Red("Red"),
        Purple("Purple"),
        DarkPurple("Dark Purple"),
        Lavender("Lavender"),
        HotPink("Hot Pink"),
        Pink("Pink");

        public String name;

        private Color(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}

