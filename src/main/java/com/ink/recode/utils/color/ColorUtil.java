package com.ink.recode.utils.color;

import java.awt.*;

public class ColorUtil {
    public static Color applyOpacity(Color color, float opacity) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.round(color.getAlpha() * opacity));
    }

    public static Color blendColors(Color color1, Color color2, float progress) {
        float r = color1.getRed() + (color2.getRed() - color1.getRed()) * progress;
        float g = color1.getGreen() + (color2.getGreen() - color1.getGreen()) * progress;
        float b = color1.getBlue() + (color2.getBlue() - color1.getBlue()) * progress;
        float a = color1.getAlpha() + (color2.getAlpha() - color1.getAlpha()) * progress;

        return new Color((int) r, (int) g, (int) b, (int) a);
    }
}