package com.ink.recode.render;

import com.ink.recode.render.font.Fonts;
import net.minecraft.client.MinecraftClient;

public class SkiaRenderer {

    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            System.out.println("[SkiaRenderer] Already initialized");
            return;
        }

        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null) {
                System.err.println("[SkiaRenderer] MinecraftClient is null");
                return;
            }
            
            if (mc.getWindow() == null) {
                System.err.println("[SkiaRenderer] Window is null");
                return;
            }
            
            System.out.println("[SkiaRenderer] Loading fonts...");
            Fonts.loadAll();
            
            System.out.println("[SkiaRenderer] Creating surface...");
            createSurface();
            
            initialized = true;
            System.out.println("[SkiaRenderer] Initialization complete!");
        } catch (Exception e) {
            System.err.println("[SkiaRenderer] Failed to initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void createSurface() {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.getWindow() != null) {
                int width = mc.getWindow().getFramebufferWidth();
                int height = mc.getWindow().getFramebufferHeight();
                System.out.println("[SkiaRenderer] Creating surface: " + width + "x" + height);
                SkiaContext.createSurface(width, height);
            }
        } catch (Exception e) {
            System.err.println("[SkiaRenderer] Failed to create surface: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void resizeSurface(int width, int height) {
        if (initialized) {
            System.out.println("[SkiaRenderer] Resizing surface to: " + width + "x" + height);
            SkiaContext.createSurface(width, height);
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
