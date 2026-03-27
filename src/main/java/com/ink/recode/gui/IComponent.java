package com.ink.recode.gui;

import net.minecraft.client.gui.DrawContext;

public interface IComponent {
    default void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
    }

    default boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return false;
    }

    default boolean mouseReleased(double mouseX, double mouseY, int state) {
        return false;
    }

    default boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default boolean charTyped(char chr, int modifiers) {
        return false;
    }

    default boolean isVisible() {
        return true;
    }
}