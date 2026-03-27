package com.ink.recode.render.nanovg.font;

public class FontLoader {
    public static int regular(float size) {
        return FontManager.fontWithCJK("OPPOSans-Regular.ttf", size);
    }

    public static int bold(float size) {
        return FontManager.fontWithCJK("OPPOSans-Bold.ttf", size);
    }

    public static int medium(float size) {
        return FontManager.fontWithCJK("OPPOSans-Medium.ttf", size);
    }

    public static int borel(float size) {
        return FontManager.fontWithCJK("Borel.ttf", size);
    }

    public static int icons(float size) {
        return FontManager.fontWithCJK("OPPOSans-Regular.ttf", size);
    }
}