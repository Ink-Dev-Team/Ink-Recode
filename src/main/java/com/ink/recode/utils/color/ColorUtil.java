package com.ink.recode.utils.color;

import java.awt.*;

public class ColorUtil {
    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.max(0.0f, Math.min(1.0f, opacity));
        int alpha = Math.round(color.getAlpha() * opacity);
        alpha = Math.max(0, Math.min(255, alpha));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static Color blendColors(Color color1, Color color2, float progress) {
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        float r = color1.getRed() + (color2.getRed() - color1.getRed()) * progress;
        float g = color1.getGreen() + (color2.getGreen() - color1.getGreen()) * progress;
        float b = color1.getBlue() + (color2.getBlue() - color1.getBlue()) * progress;
        float a = color1.getAlpha() + (color2.getAlpha() - color1.getAlpha()) * progress;

        int red = Math.max(0, Math.min(255, (int) Math.round(r)));
        int green = Math.max(0, Math.min(255, (int) Math.round(g)));
        int blue = Math.max(0, Math.min(255, (int) Math.round(b)));
        int alpha = Math.max(0, Math.min(255, (int) Math.round(a)));

        return new Color(red, green, blue, alpha);
    }
}
