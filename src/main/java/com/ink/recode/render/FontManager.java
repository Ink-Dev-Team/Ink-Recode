package com.ink.recode.render;

import com.ink.recode.render.font.Fonts;
import io.github.humbleui.skija.Font;
import net.minecraft.client.MinecraftClient;

public class FontManager {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void init() {
        Fonts.loadAll();
    }

    public static Font getRegular(float size) {
        return Fonts.getRegular(size);
    }

    public static Font getMedium(float size) {
        return Fonts.getMedium(size);
    }

    public static Font getBold(float size) {
        return Fonts.getBold(size);
    }

    public static Font getBorel(float size) {
        return Fonts.getBorel(size);
    }

    public static Font getCustomFont(String fontName, float size) {
        return Fonts.getCustomFont(fontName, size);
    }

    public static String[] getCustomFontNames() {
        return Fonts.getCustomFontNames();
    }
}
