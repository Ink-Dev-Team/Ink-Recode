package com.ink.recode.render;

import com.ink.recode.render.nanovg.NanoVGRenderer;
import com.ink.recode.render.nanovg.util.NanoVGHelper;
import java.awt.Color;
import java.util.function.Consumer;

public class Skia {
    public static void draw(Consumer<Long> drawingLogic) {
        NanoVGRenderer.INSTANCE.draw(drawingLogic);
    }

    public static void drawText(String text, float x, float y, Color color, int font) {
        NanoVGHelper.drawString(text, x, y, font, color);
    }

    public static void drawText(String text, float x, float y, Color color, int font, float size) {
        NanoVGHelper.drawString(text, x, y, font, size, color);
    }

    public static void drawRoundRect(float x, float y, float width, float height, float radius, Color color) {
        NanoVGHelper.drawRoundRect(x, y, width, height, radius, color);
    }

    public static void drawRect(float x, float y, float width, float height, Color color) {
        NanoVGHelper.drawRect(x, y, width, height, color);
    }

    public static void drawLine(float x1, float y1, float x2, float y2, float width, Color color) {
        NanoVGHelper.drawLine(x1, y1, x2, y2, width, color);
    }

    public static void drawCircle(float x, float y, float radius, Color color) {
        NanoVGHelper.drawCircle(x, y, radius, color);
    }

    public static float getTextWidth(String text, int font, float size) {
        return NanoVGHelper.getTextWidth(text, font, size);
    }

    public static float getTextHeight(int font, String text) {
        return NanoVGHelper.getTextHeight(font, text);
    }
}