package com.ink.recode.render;

import com.ink.recode.render.nanovg.font.FontLoader;

public class FontManager {
    public static int getRegular(float size) {
        return FontLoader.regular(size);
    }

    public static int getMedium(float size) {
        return FontLoader.medium(size);
    }

    public static int getBold(float size) {
        return FontLoader.bold(size);
    }

    public static int getBorel(float size) {
        return FontLoader.borel(size);
    }
}