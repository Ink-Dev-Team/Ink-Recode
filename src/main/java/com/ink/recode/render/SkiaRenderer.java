package com.ink.recode.render;

import com.ink.recode.render.nanovg.NanoVGRenderer;

public class SkiaRenderer {

    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            System.out.println("[SkiaRenderer] Already initialized");
            return;
        }

        try {
            System.out.println("[SkiaRenderer] Initializing NanoVG...");
            NanoVGRenderer.INSTANCE.initNanoVG();
            
            initialized = true;
            System.out.println("[SkiaRenderer] Initialization complete!");
        } catch (Exception e) {
            System.err.println("[SkiaRenderer] Failed to initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createSurface() {
        // NanoVG doesn't require surface creation like Skia
        System.out.println("[SkiaRenderer] Surface created (NanoVG)");
    }

    public static void resizeSurface(int width, int height) {
        // NanoVG automatically handles resizing
        System.out.println("[SkiaRenderer] Surface resized to: " + width + "x" + height);
    }

    public static boolean isInitialized() {
        return initialized && NanoVGRenderer.INSTANCE.isInitialized();
    }
}