package com.ink.recode.gui.clickgui;

import com.ink.recode.Category;
import com.ink.recode.ModuleManager;
import com.ink.recode.gui.clickgui.panel.CategoryPanel;
import com.ink.recode.modules.impl.render.ClickGUI;
import com.ink.recode.render.nanovg.NanoVGRenderer;
import com.ink.recode.render.nanovg.util.NanoVGHelper;
import com.ink.recode.utils.animations.Animation;
import com.ink.recode.utils.animations.Direction;
import com.ink.recode.utils.animations.impl.EaseOutSine;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.MinecraftClient.getInstance;

public class ClickGuiScreen extends Screen {
    public static Animation openingAnimation = new EaseOutSine(400, 1);
    private final List<CategoryPanel> panels = new ArrayList<>();
    public int scroll;
    private DrawContext currentContext;

    public ClickGuiScreen() {
        super(Text.literal("ClickGui"));
        openingAnimation.setDirection(Direction.BACKWARDS);
        float width = 0;
        for (Category category : Category.values()) {
            CategoryPanel panel = new CategoryPanel(category);
            panel.setX(50 + width);
            panel.setY(20);
            panels.add(panel);
            width += panel.getWidth() + 10;
        }
    }

    @Override
    public void init() {
        openingAnimation.setDirection(Direction.FORWARDS);
        openingAnimation.reset();

        for (CategoryPanel panel : panels) {
            panel.setOpened(true);
            panel.getOpenAnimation().setDirection(Direction.BACKWARDS);
            panel.getOpenAnimation().timerUtil.setTime(0);
        }
    }

    @Override
    public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.currentContext = guiGraphics;
        final float wheel = getDWheel();
        if (wheel != 0) {
            scroll += wheel > 0 ? 15 : -15;
            for (CategoryPanel panel : panels) {
                if (!panel.isDragging()) {
                    panel.setY(panel.getY() + (wheel > 0 ? 15 : -15));
                }
            }
        }

        NanoVGRenderer.INSTANCE.draw(canvas -> NanoVGHelper.drawRect(0, 0, getInstance().getWindow().getScaledWidth(), getInstance().getWindow().getScaledHeight(), new Color(18, 18, 18, 50)));

        panels.forEach(panel -> panel.render(guiGraphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (currentContext != null) {
            int finalMouseY = (int) mouseY;
            boolean handled = false;
            for (CategoryPanel panel : panels) {
                if (panel.mouseClicked(mouseX, finalMouseY, mouseButton)) {
                    handled = true;
                }
            }
            return handled || super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        if (currentContext != null) {
            int finalMouseY = (int) mouseY;
            boolean handled = false;
            for (CategoryPanel panel : panels) {
                if (panel.mouseReleased(mouseX, finalMouseY, state)) {
                    handled = true;
                }
            }

            return handled || super.mouseReleased(mouseX, mouseY, state);
        }

        return super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void close() {
        ClickGUI.setEnabled(false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean handled = false;
        for (CategoryPanel panel : panels) {
            if (panel.keyPressed(keyCode, scanCode, modifiers)) {
                handled = true;
            }
        }
        return handled || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        boolean handled = false;
        for (CategoryPanel panel : panels) {
            if (panel.charTyped(chr, modifiers)) {
                handled = true;
            }
        }
        return handled || super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private float accumulatedScroll = 0;

    private float getDWheel() {
        float scroll = accumulatedScroll;
        accumulatedScroll = 0;
        return scroll;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        accumulatedScroll += (float) scrollY;
        return true;
    }

    public List<CategoryPanel> getPanels() {
        return panels;
    }
}