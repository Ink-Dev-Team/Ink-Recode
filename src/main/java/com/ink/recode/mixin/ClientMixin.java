package com.ink.recode.mixin;

import com.ink.recode.render.SkiaRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ClientMixin {
    
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(boolean tick, CallbackInfo ci) {
        if (!SkiaRenderer.isInitialized()) {
            try {
                System.out.println("[ClientMixin] Attempting to initialize Skia renderer...");
                SkiaRenderer.init();
                System.out.println("[InkRecode] Skia renderer initialized successfully");
            } catch (Exception e) {
                System.err.println("[InkRecode] Failed to initialize Skia renderer: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "onResolutionChanged", at = @At("HEAD"))
    private void onResolutionChanged(CallbackInfo ci) {
        if (SkiaRenderer.isInitialized()) {
            MinecraftClient mc = (MinecraftClient) (Object) this;
            Window window = mc.getWindow();
            if (window != null) {
                SkiaRenderer.resizeSurface(window.getFramebufferWidth(), window.getFramebufferHeight());
            }
        }
    }
}
