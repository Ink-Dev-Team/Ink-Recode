package com.ink.recode.mixin;

import com.ink.recode.render.SkiaContext;
import com.ink.recode.render.SkiaRenderer;
import com.ink.recode.render.FontManager;
import io.github.humbleui.skija.Font;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.awt.Color;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    // 空实现，不修改标题界面
}
