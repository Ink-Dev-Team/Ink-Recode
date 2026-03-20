package com.ink.recode.render;

import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL33;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.humbleui.skija.BackendRenderTarget;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.ColorSpace;
import io.github.humbleui.skija.ColorType;
import io.github.humbleui.skija.DirectContext;
import io.github.humbleui.skija.Surface;
import io.github.humbleui.skija.SurfaceOrigin;
import net.minecraft.client.MinecraftClient;

public class SkiaContext {

	private static DirectContext context = null;
	private static Surface surface;
	private static BackendRenderTarget renderTarget;

	public static Canvas getCanvas() {
		return surface != null ? surface.getCanvas() : null;
	}

	public static void createSurface(int width, int height) {
		try {
			if (context == null) {
				context = DirectContext.makeGL();
			}

			if (surface != null) {
				surface.close();
				surface = null;
			}

			if (renderTarget != null) {
				renderTarget.close();
				renderTarget = null;
			}

			renderTarget = BackendRenderTarget.makeGL(width, height, 0, 8,
					MinecraftClient.getInstance().getFramebuffer().fbo, GL11.GL_RGBA8);
			surface = Surface.wrapBackendRenderTarget(context, renderTarget, SurfaceOrigin.BOTTOM_LEFT,  
	        ColorType.RGBA_8888, ColorSpace.getSRGB());
	        
	        System.out.println("[SkiaContext] Surface created: " + width + "x" + height);
		} catch (Exception e) {
			System.err.println("[SkiaContext] Failed to create surface: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void draw(Consumer<Canvas> drawingLogic) {
		if (getCanvas() == null) {
			System.err.println("[SkiaContext] Canvas is null");
			return;
		}
		
		RenderSystem.enableBlend();
		GL11.glEnable(GL11.GL_BLEND);
		RenderSystem.defaultBlendFunc();
		
		RenderSystem.pixelStore(GlConst.GL_UNPACK_ROW_LENGTH, 0);
		RenderSystem.pixelStore(GlConst.GL_UNPACK_SKIP_PIXELS, 0);
		RenderSystem.pixelStore(GlConst.GL_UNPACK_SKIP_ROWS, 0);
		RenderSystem.pixelStore(GlConst.GL_UNPACK_ALIGNMENT, 4);
		
		if (context != null) context.resetGLAll();
		
		Canvas canvas = getCanvas();
		if (canvas != null) {
			try {
				drawingLogic.accept(canvas);
			} catch (Exception e) {
				System.err.println("[SkiaContext] Error during drawing: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		if (context != null) context.flush();
		
		GL33.glBindSampler(0, 0);
		RenderSystem.disableBlend();
		GL11.glDisable(GL11.GL_BLEND);
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		RenderSystem.blendEquation(GL33.GL_FUNC_ADD);
		GL33.glBlendEquation(GL33.GL_FUNC_ADD);
		RenderSystem.colorMask(true, true, true, true);
		GL11.glColorMask(true, true, true, true);
		RenderSystem.depthMask(true);
		GL11.glDepthMask(true);
		RenderSystem.disableScissor();
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		RenderSystem.disableDepthTest();
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		RenderSystem.activeTexture(GL13.GL_TEXTURE0);
		RenderSystem.disableCull();
	}

	public static DirectContext getContext() {
		return context;
	}
}
