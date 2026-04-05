package com.ink.recode.gui.clickgui;

import com.ink.recode.Category;
import com.ink.recode.Module;
import com.ink.recode.ModuleManager;
import com.ink.recode.modules.impl.render.ClickGUI;
import com.ink.recode.render.nanovg.NanoVGRenderer;
import com.ink.recode.render.nanovg.font.FontLoader;
import com.ink.recode.render.nanovg.util.NanoVGHelper;
import com.ink.recode.utils.animations.Direction;
import com.ink.recode.utils.animations.impl.EaseInOutQuad;
import com.ink.recode.utils.animations.impl.EaseOutSine;
import com.ink.recode.utils.color.ColorUtil;
import com.ink.recode.utils.render.RenderUtil;
import com.ink.recode.value.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.minecraft.client.MinecraftClient.getInstance;

public class ClickGuiScreen extends Screen {
    private final List<CategoryPanel> panels = new ArrayList<>();
    public int scroll;
    private DrawContext currentContext;
    private static final float MIN_PANEL_Y = 10.0f;
    private static final float MAX_SCROLL = 100.0f;
    private final EaseOutSine openingAnimation = new EaseOutSine(400, 1);

    public ClickGuiScreen() {
        super(Text.literal("ClickGui"));
        openingAnimation.setDirection(Direction.BACKWARDS);

        float xOffset = 50.0f;
        for (Category category : Category.values()) {
            CategoryPanel panel = new CategoryPanel(category);
            panel.setX(xOffset);
            panel.setY(MIN_PANEL_Y);
            panels.add(panel);
            xOffset += panel.getWidth() + 15;
        }
    }

    // ... existing code ...
    @Override
    public void init() {
        openingAnimation.setDirection(Direction.FORWARDS);
        openingAnimation.reset();

        for (CategoryPanel panel : panels) {
            panel.setOpened(false);
            panel.getOpenAnimation().setDirection(Direction.BACKWARDS);
            panel.getOpenAnimation().reset();
        }

        new Thread(() -> {
            try {
                Thread.sleep(100);
                for (CategoryPanel panel : panels) {
                    panel.setOpened(true);
                    panel.getOpenAnimation().setDirection(Direction.FORWARDS);
                    panel.getOpenAnimation().reset();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
// ... existing code ...


    @Override
    public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.currentContext = guiGraphics;

        final float wheel = getDWheel();
        if (wheel != 0) {
            scroll = (int) Math.max(-MAX_SCROLL, Math.min(MAX_SCROLL, scroll + (int)(wheel * 15)));
            for (CategoryPanel panel : panels) {
                if (!panel.isDragging()) {
                    float newY = panel.getY() + (wheel > 0 ? 15 : -15);
                    panel.setY(Math.max(MIN_PANEL_Y, newY));
                }
            }
        }

        NanoVGRenderer.INSTANCE.draw(canvas -> {
            Color bgColor = ClickGUI.backgroundColor.get();
            Color overlayColor = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 50);
            NanoVGHelper.drawRect(0, 0, getInstance().getWindow().getScaledWidth(),
                    getInstance().getWindow().getScaledHeight(), overlayColor);
        });

        panels.forEach(panel -> panel.render(guiGraphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (currentContext != null) {
            boolean handled = false;
            for (CategoryPanel panel : panels) {
                if (panel.mouseClicked(mouseX, mouseY, mouseButton)) {
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
            boolean handled = false;
            for (CategoryPanel panel : panels) {
                if (panel.mouseReleased(mouseX, mouseY, state)) {
                    handled = true;
                }
            }

            return handled || super.mouseReleased(mouseX, mouseY, state);
        }

        return super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (currentContext != null) {
            boolean handled = false;
            for (CategoryPanel panel : panels) {
                if (panel.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                    handled = true;
                }
            }
            return handled || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void close(){

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

    // ==================== CategoryPanel 内部类 ====================
    public static class CategoryPanel {
        private static final float PANEL_WIDTH = 120.0f;
        private final Category category;
        private float x, y;
        private boolean opened;
        private final CopyOnWriteArrayList<ModuleComponent> modules = new CopyOnWriteArrayList<>();
        private final EaseInOutQuad openAnimation = new EaseInOutQuad(300, 1);
        private final EaseOutSine hoverAnimation = new EaseOutSine(200, 1);
        private boolean dragging = false;
        private float dragOffsetX, dragOffsetY;

        public CategoryPanel(Category category) {
            this.category = category;
            openAnimation.setDirection(Direction.BACKWARDS);
            hoverAnimation.setDirection(Direction.BACKWARDS);

            for (Module module : ModuleManager.getModulesByCategory(category)) {
                modules.add(new ModuleComponent(module));
            }
        }

        public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
            float totalHeight = 25;
            if (opened) {
                for (ModuleComponent module : modules) {
                    totalHeight += module.getHeight() * (float) openAnimation.getOutput();
                }
            }

            hoverAnimation.setDirection(isHovered(mouseX, mouseY) ? Direction.FORWARDS : Direction.BACKWARDS);

            final float finalTotalHeight = totalHeight;

            NanoVGRenderer.INSTANCE.draw(vg -> {
                float hoverFactor = (float) hoverAnimation.getOutput();

                Color headerColor = ClickGUI.backgroundColor.get();
                Color finalHeaderColor = ColorUtil.applyOpacity(headerColor, 0.7f + hoverFactor * 0.2f);
                NanoVGHelper.drawRoundRect(x, y, PANEL_WIDTH, 25, 6, finalHeaderColor);

                String categoryName = category.name();
                float textWidth = NanoVGHelper.getTextWidth(categoryName, FontLoader.bold(14), 14);
                float textX = x + (PANEL_WIDTH - textWidth) / 2;
                NanoVGHelper.drawString(categoryName, textX, y + 17, FontLoader.bold(14), 14,
                        isHovered(mouseX, mouseY) ? ClickGUI.color(255) : Color.WHITE);

                if (opened && openAnimation.getOutput() > 0) {
                    float contentHeight = (finalTotalHeight - 25) * (float) openAnimation.getOutput();
                    Color contentBg = ColorUtil.applyOpacity(ClickGUI.backgroundColor.get(), 0.5f);
                    NanoVGHelper.drawRoundRect(x, y + 25, PANEL_WIDTH, contentHeight, 6, contentBg);
                }
            });

            if (opened) {
                float yOffset = y + 25;
                for (ModuleComponent module : modules) {
                    module.setX(x + 5);
                    module.setY(yOffset);
                    module.setWidth(PANEL_WIDTH - 10);

                    if (openAnimation.getOutput() > 0.5f) {
                        module.render(guiGraphics, mouseX, mouseY, partialTicks);
                    }

                    yOffset += module.getHeight() * (float) openAnimation.getOutput();
                }
            }
        }

        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            if (isHeaderHovered((int) mouseX, (int) mouseY) && mouseButton == 0) {
                dragging = true;
                dragOffsetX = (float) mouseX - x;
                dragOffsetY = (float) mouseY - y;
                return true;
            }

            if (isHeaderHovered((int) mouseX, (int) mouseY) && mouseButton == 1) {
                opened = !opened;
                return true;
            }

            if (opened) {
                for (ModuleComponent module : modules) {
                    if (module.mouseClicked(mouseX, mouseY, mouseButton)) {
                        return true;
                    }
                }
            }

            return false;
        }

        public boolean mouseReleased(double mouseX, double mouseY, int state) {
            dragging = false;

            if (opened) {
                for (ModuleComponent module : modules) {
                    if (module.mouseReleased(mouseX, mouseY, state)) {
                        return true;
                    }
                }
            }

            return false;
        }

        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (dragging) {
                x = (float) mouseX - dragOffsetX;
                y = (float) mouseY - dragOffsetY;
                return true;
            }

            if (opened) {
                for (ModuleComponent module : modules) {
                    if (module.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                        return true;
                    }
                }
            }

            return false;
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (opened) {
                for (ModuleComponent module : modules) {
                    if (module.keyPressed(keyCode, scanCode, modifiers)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean charTyped(char chr, int modifiers) {
            if (opened) {
                for (ModuleComponent module : modules) {
                    if (module.charTyped(chr, modifiers)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isHovered(int mouseX, int mouseY) {
            return RenderUtil.isHovering(x, y, PANEL_WIDTH, 25, mouseX, mouseY);
        }

        private boolean isHeaderHovered(int mouseX, int mouseY) {
            return isHovered(mouseX, mouseY);
        }

        public float getX() { return x; }
        public float getY() { return y; }
        public void setX(float x) { this.x = x; }
        public void setY(float y) { this.y = y; }
        public float getWidth() { return PANEL_WIDTH; }
        public boolean isDragging() { return dragging; }
        public boolean isOpened() { return opened; }
        public void setOpened(boolean opened) { this.opened = opened; }
        public EaseInOutQuad getOpenAnimation() { return openAnimation; }
    }

    // ==================== ModuleComponent 内部类 ====================
    public static class ModuleComponent {
        private static final int MODULE_HEIGHT = 20;
        private float x, y, width, height = MODULE_HEIGHT;
        private float scale = 1.0f;
        private final Module module;
        private boolean opened;
        private boolean listening = false;
        private boolean previewEnabled = false;
        private final EaseInOutQuad openAnimation = new EaseInOutQuad(250, 1);
        private final EaseOutSine toggleAnimation = new EaseOutSine(300, 1);
        private final EaseOutSine hoverAnimation = new EaseOutSine(200, 1);
        private final CopyOnWriteArrayList<ValueComponent> settings = new CopyOnWriteArrayList<>();

        public ModuleComponent(Module module) {
            this.module = module;
            openAnimation.setDirection(Direction.BACKWARDS);
            toggleAnimation.setDirection(Direction.BACKWARDS);
            hoverAnimation.setDirection(Direction.BACKWARDS);

            for (Value<?> value : module.values) {
                if (value instanceof BooleanValue boolValue) {
                    settings.add(new BoolValueComponent(boolValue));
                } else if (value instanceof NumberValue numberValue) {
                    settings.add(new NumberValueComponent(numberValue));
                } else if (value instanceof ModeValue modeValue) {
                    settings.add(new EnumValueComponent(modeValue));
                } else if (value instanceof StringValue stringValue) {
                    settings.add(new StringValueComponent(stringValue));
                }
            }
        }

        public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
            float baseFontSize = (float) ClickGUI.getFontSize();
            float scaledHeight = MODULE_HEIGHT * scale;
            float yOffset = scaledHeight;

            openAnimation.setDirection(opened ? Direction.FORWARDS : Direction.BACKWARDS);
            toggleAnimation.setDirection(module.enabled || previewEnabled ? Direction.FORWARDS : Direction.BACKWARDS);
            hoverAnimation.setDirection(isHovered(mouseX, mouseY) ? Direction.FORWARDS : Direction.BACKWARDS);

            boolean hasVisibleSettings = false;
            for (ValueComponent component : settings) {
                if (!component.isVisible()) continue;
                hasVisibleSettings = true;
                component.setScale(scale);
                yOffset += (float) (component.getHeight() * openAnimation.getOutput());
            }

            if (hasVisibleSettings && openAnimation.getOutput() > 0) {
                yOffset += (float) (3 * scale * openAnimation.getOutput());
            }

            this.height = yOffset;

            final boolean finalHasVisibleSettings = hasVisibleSettings;
            final float finalYOffset = yOffset;

            NanoVGRenderer.INSTANCE.draw(vg -> {
                float hoverFactor = (float) hoverAnimation.getOutput();

                if (module.enabled || previewEnabled) {
                    Color activeColor1 = ClickGUI.color(80);
                    Color activeColor2 = ClickGUI.color2(80);
                    NanoVGHelper.drawGradientRRect2(x, y, width, scaledHeight, 4, activeColor1, activeColor2);
                }

                Color bgColor = ClickGUI.backgroundColor.get();
                Color normalBg = ColorUtil.applyOpacity(bgColor, 0.5f + hoverFactor * 0.1f);
                NanoVGHelper.drawRoundRect(x, y, width, scaledHeight, 4, normalBg);

                if (finalHasVisibleSettings && openAnimation.getOutput() > 0) {
                    float expandedHeight = (finalYOffset - scaledHeight) * (float) openAnimation.getOutput();
                    Color expandBg = ColorUtil.applyOpacity(ClickGUI.expandedBackgroundColor.get(),
                            (float) (0.35f * openAnimation.getOutput()));
                    NanoVGHelper.drawRoundRect(x, y + scaledHeight - 2, width, expandedHeight + 2, 4, expandBg);
                }

                int textAlpha = Math.min(255, 200 + (int)(55 * hoverFactor));
                Color textColor = new Color(255, 255, 255, textAlpha);
                NanoVGHelper.drawString(module.name, x + 5 * scale, y + 12 * scale,
                        FontLoader.regular(baseFontSize * 0.8f), baseFontSize * 0.8f, textColor);

                float boxWidth = 20 * scale;
                float boxHeight = 9 * scale;
                float boxX = x + width - boxWidth - 5 * scale;
                float boxY = y + (scaledHeight - boxHeight) / 2;

                int keyCode = module.key;
                boolean hasKey = keyCode != 0 && keyCode != GLFW.GLFW_KEY_UNKNOWN;
                boolean isHold = module.bindMode == Module.BindMode.HOLD;

                Color themeColor = ClickGUI.color(0);
                Color bindBoxBg;
                Color bindBoxBorder;

                if (listening) {
                    bindBoxBg = new Color(255, 120, 120, 200);
                    bindBoxBorder = new Color(255, 180, 180, 240);
                } else if (hasKey) {
                    bindBoxBg = ColorUtil.applyOpacity(themeColor, 0.65f);
                    bindBoxBorder = ColorUtil.applyOpacity(themeColor, 0.95f);
                } else {
                    bindBoxBg = ColorUtil.applyOpacity(themeColor, 0.35f);
                    bindBoxBorder = ColorUtil.applyOpacity(themeColor, 0.55f);
                }

                NanoVGHelper.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 2.5f * scale, bindBoxBg);
                NanoVGHelper.drawRoundRectOutline(boxX, boxY, boxWidth, boxHeight, 2.5f * scale, 0.75f * scale, bindBoxBorder);

                float fontSize = 5.5f * scale;
                int font = FontLoader.regular(fontSize);
                String displayText = listening ? "..." : (hasKey ? getKeyName(keyCode) : "");
                float textWidth = NanoVGHelper.getTextWidth(displayText, font, fontSize);
                float textX = boxX + (boxWidth - textWidth) / 2;
                float textY = boxY + boxHeight - 2.5f * scale;
                if (!displayText.isEmpty()) {
                    NanoVGHelper.drawString(displayText, textX, textY, font, fontSize, Color.WHITE);
                }

                if (isHold && !listening && hasKey) {
                    float lineWidth = textWidth + 2 * scale;
                    float lineX = textX - 1 * scale;
                    float lineY = boxY + boxHeight - 1.2f * scale;
                    NanoVGHelper.drawRoundRect(lineX, lineY, lineWidth, 0.75f * scale, 0.35f * scale, Color.WHITE);
                }
            });

            float componentYOffset = scaledHeight;
            for (ValueComponent component : settings) {
                if (!component.isVisible()) continue;
                component.setX(x + 5 * scale);
                component.setY(y + 12 * scale + componentYOffset);
                component.setWidth(width - 10 * scale);
                if (openAnimation.getOutput() > 0.5f) {
                    component.render(guiGraphics, mouseX, mouseY, partialTicks);
                }
                componentYOffset += component.getHeight();
            }
        }

        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            if (isBindBoxHovered((int) mouseX, (int) mouseY)) {
                if (mouseButton == 0) {
                    listening = !listening;
                    return true;
                } else if (mouseButton == 2) {
                    module.bindMode = module.bindMode == Module.BindMode.TOGGLE ? Module.BindMode.HOLD : Module.BindMode.TOGGLE;
                    return true;
                }
            } else if (listening) {
                listening = false;
            }

            if (isHovered((int) mouseX, (int) mouseY) && !isBindBoxHovered((int) mouseX, (int) mouseY)) {
                switch (mouseButton) {
                    case 0 -> module.toggle();
                    case 1 -> opened = !opened;
                }
            }

            if (opened && !isHovered((int) mouseX, (int) mouseY)) {
                for (ValueComponent setting : settings) {
                    if (setting.mouseClicked(mouseX, mouseY, mouseButton)) {
                        return true;
                    }
                }
            }

            return false;
        }

        public boolean mouseReleased(double mouseX, double mouseY, int state) {
            if (opened && !isHovered((int) mouseX, (int) mouseY)) {
                for (ValueComponent setting : settings) {
                    if (setting.mouseReleased(mouseX, mouseY, state)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (opened && !isHovered((int) mouseX, (int) mouseY)) {
                for (ValueComponent setting : settings) {
                    if (setting.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (listening) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    module.key = 0;
                } else if (keyCode != GLFW.GLFW_KEY_UNKNOWN) {
                    module.key = keyCode;
                }
                listening = false;
                return true;
            }

            if (opened) {
                for (ValueComponent setting : settings) {
                    if (setting.keyPressed(keyCode, scanCode, modifiers)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean charTyped(char chr, int modifiers) {
            if (opened) {
                for (ValueComponent setting : settings) {
                    if (setting.charTyped(chr, modifiers)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean isHovered(int mouseX, int mouseY) {
            return RenderUtil.isHovering(x, y, width, MODULE_HEIGHT * scale, mouseX, mouseY);
        }

        public boolean isBindBoxHovered(int mouseX, int mouseY) {
            float boxWidth = 20 * scale;
            float boxHeight = 9 * scale;
            float boxX = x + width - boxWidth - 5 * scale;
            float boxY = y + (MODULE_HEIGHT * scale - boxHeight) / 2;
            return RenderUtil.isHovering(boxX, boxY, boxWidth, boxHeight, mouseX, mouseY);
        }

        public float getX() { return x; }
        public float getY() { return y; }
        public float getWidth() { return width; }
        public float getHeight() { return height; }
        public void setX(float x) { this.x = x; }
        public void setY(float y) { this.y = y; }
        public void setWidth(float width) { this.width = width; }
        public void setHeight(float height) { this.height = height; }
        public void setScale(float scale) { this.scale = scale; }
        public Module getModule() { return module; }
        public EaseInOutQuad getOpenAnimation() { return openAnimation; }

        private String getKeyName(int keyCode) {
            if (keyCode < 0) {
                return "M" + (-keyCode);
            }
            try {
                InputUtil.Key key = InputUtil.Type.KEYSYM.createFromCode(keyCode);
                String name = key.getLocalizedText().getString();
                if (name.length() > 6) {
                    name = name.substring(0, 5) + ".";
                }
                return name.toUpperCase();
            } catch (Exception e) {
                return "?";
            }
        }
    }

    // ==================== ValueComponent 基类 ====================
    public abstract static class ValueComponent {
        protected float x, y, width, height;
        protected float scale = 1.0f;

        public abstract void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks);
        public abstract boolean mouseClicked(double mouseX, double mouseY, int mouseButton);
        public abstract boolean mouseReleased(double mouseX, double mouseY, int state);
        public abstract boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);
        public abstract boolean keyPressed(int keyCode, int scanCode, int modifiers);
        public abstract boolean charTyped(char chr, int modifiers);
        public abstract boolean isVisible();

        public float getX() { return x; }
        public float getY() { return y; }
        public float getWidth() { return width; }
        public float getHeight() { return height; }
        public float getScale() { return scale; }

        public void setX(float x) { this.x = x; }
        public void setY(float y) { this.y = y; }
        public void setWidth(float width) { this.width = width; }
        public void setHeight(float height) { this.height = height; }
        public void setScale(float scale) { this.scale = scale; }
    }

    // ==================== BoolValueComponent ====================
    public static class BoolValueComponent extends ValueComponent {
        private final BooleanValue setting;
        private final EaseOutSine toggleAnimation = new EaseOutSine(200, 1);

        public BoolValueComponent(BooleanValue setting) {
            this.setting = setting;
            toggleAnimation.setDirection(Direction.BACKWARDS);
        }

        @Override
        public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
            float baseFontSize = (float) ClickGUI.getFontSize();
            float titleFontSize = baseFontSize * 0.75f;
            setHeight(18 * scale);

            toggleAnimation.setDirection(setting.get() ? Direction.FORWARDS : Direction.BACKWARDS);

            NanoVGRenderer.INSTANCE.draw(vg -> {
                NanoVGHelper.drawString(setting.getName(), getX(), getY() + 12 * scale,
                        FontLoader.regular(titleFontSize), titleFontSize, new Color(255, 255, 255, 255));

                float toggleWidth = 30 * scale;
                float toggleHeight = 14 * scale;
                float toggleX = getX() + getWidth() - toggleWidth;
                float toggleY = getY() + (18 * scale - toggleHeight) / 2;

                float animValue = (float) toggleAnimation.getOutput();

                Color bgColor = new Color(
                        (int) (80 + (ClickGUI.color(0).getRed() - 80) * animValue),
                        (int) (80 + (ClickGUI.color(0).getGreen() - 80) * animValue),
                        (int) (80 + (ClickGUI.color(0).getBlue() - 80) * animValue)
                );
                NanoVGHelper.drawRoundRect(toggleX, toggleY, toggleWidth, toggleHeight, 7 * scale, bgColor);

                float indicatorSize = 10 * scale;
                float indicatorX = toggleX + 2 * scale + (toggleWidth - indicatorSize - 4 * scale) * animValue;
                float indicatorY = toggleY + (toggleHeight - indicatorSize) / 2;

                NanoVGHelper.drawRoundRect(indicatorX, indicatorY, indicatorSize, indicatorSize, 5 * scale, Color.WHITE);
            });
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            float toggleWidth = 30 * scale;
            float toggleHeight = 14 * scale;
            float toggleX = getX() + getWidth() - toggleWidth;
            float toggleY = getY() + (18 * scale - toggleHeight) / 2;

            if (RenderUtil.isHovering(toggleX, toggleY, toggleWidth, toggleHeight, (float) mouseX, (float) mouseY) && mouseButton == 0) {
                setting.set(!setting.get());
                return true;
            }
            return false;
        }

        @Override public boolean mouseReleased(double mouseX, double mouseY, int state) { return false; }
        @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) { return false; }
        @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
        @Override public boolean charTyped(char chr, int modifiers) { return false; }
        @Override public boolean isVisible() { return setting.isAvailable(); }
    }

    // ==================== NumberValueComponent ====================
    public static class NumberValueComponent extends ValueComponent {
        private final NumberValue setting;
        private boolean dragging = false;
        private final EaseOutSine hoverAnimation = new EaseOutSine(150, 1);

        public NumberValueComponent(NumberValue setting) {
            this.setting = setting;
            hoverAnimation.setDirection(Direction.BACKWARDS);
        }

        @Override
        public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
            float baseFontSize = (float) ClickGUI.getFontSize();
            float titleFontSize = baseFontSize * 0.75f;
            setHeight(22 * scale);

            NanoVGRenderer.INSTANCE.draw(vg -> {
                NanoVGHelper.drawString(setting.getName(), getX(), getY() + 14 * scale,
                        FontLoader.regular(titleFontSize), titleFontSize, new Color(255, 255, 255, 255));

                float sliderWidth = getWidth() - 80 * scale;
                float sliderHeight = 4 * scale;
                float sliderX = getX() + 5 * scale;
                float sliderY = getY() + 16 * scale;

                boolean isHovered = RenderUtil.isHovering(sliderX, sliderY, sliderWidth, sliderHeight, mouseX, mouseY);
                hoverAnimation.setDirection((isHovered || dragging) ? Direction.FORWARDS : Direction.BACKWARDS);

                float hoverFactor = (float) hoverAnimation.getOutput();

                double value = setting.get();
                double min = setting.min;
                double max = setting.max;
                double range = max - min;
                double progress = (value - min) / range;

                NanoVGHelper.drawRoundRect(sliderX, sliderY, sliderWidth, sliderHeight, 2 * scale, new Color(80, 80, 80));

                float filledWidth = (float) (sliderWidth * progress);
                Color fillColor = ColorUtil.applyOpacity(ClickGUI.color(0), 0.8f + hoverFactor * 0.2f);
                NanoVGHelper.drawRoundRect(sliderX, sliderY, filledWidth, sliderHeight, 2 * scale, fillColor);

                float knobX = sliderX + filledWidth - 3 * scale;
                float knobSize = 8 * scale;
                Color knobColor = dragging ? ClickGUI.color(255) : ColorUtil.applyOpacity(ClickGUI.color(0), 0.9f);
                NanoVGHelper.drawRoundRect(knobX, sliderY - (knobSize - sliderHeight) / 2,
                        knobSize, knobSize, knobSize / 2, knobColor);

                String valueString = String.format("%.2f", value);
                float valueWidth = NanoVGHelper.getTextWidth(valueString, FontLoader.regular(titleFontSize), titleFontSize);
                float valueX = getX() + getWidth() - valueWidth - 5 * scale;
                NanoVGHelper.drawString(valueString, valueX, getY() + 14 * scale,
                        FontLoader.regular(titleFontSize), titleFontSize, ClickGUI.color(0));
            });
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            float sliderWidth = getWidth() - 80 * scale;
            float sliderHeight = 4 * scale;
            float sliderX = getX() + 5 * scale;
            float sliderY = getY() + 16 * scale;

            if (RenderUtil.isHovering(sliderX, sliderY, sliderWidth, sliderHeight, (float) mouseX, (float) mouseY) && mouseButton == 0) {
                dragging = true;
                updateValue((float) mouseX);
                return true;
            }
            return false;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int state) {
            dragging = false;
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (dragging) {
                updateValue((float) mouseX);
                return true;
            }
            return false;
        }

        private void updateValue(float mouseX) {
            float sliderWidth = getWidth() - 80 * scale;
            float sliderX = getX() + 5 * scale;

            double progress = (mouseX - sliderX) / sliderWidth;
            progress = Math.max(0, Math.min(1, progress));

            double min = setting.min;
            double max = setting.max;
            double range = max - min;
            double newValue = min + (progress * range);

            setting.set(newValue);
        }

        @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
        @Override public boolean charTyped(char chr, int modifiers) { return false; }
        @Override public boolean isVisible() { return setting.isAvailable(); }
    }

    // ==================== EnumValueComponent ====================
    public static class EnumValueComponent extends ValueComponent {
        private final ModeValue setting;
        private final EaseOutSine hoverAnimation = new EaseOutSine(150, 1);

        public EnumValueComponent(ModeValue setting) {
            this.setting = setting;
            hoverAnimation.setDirection(Direction.BACKWARDS);
        }

        @Override
        public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
            float baseFontSize = (float) ClickGUI.getFontSize();
            float titleFontSize = baseFontSize * 0.75f;
            setHeight(18 * scale);

            NanoVGRenderer.INSTANCE.draw(vg -> {
                NanoVGHelper.drawString(setting.getName(), getX(), getY() + 12 * scale,
                        FontLoader.regular(titleFontSize), titleFontSize, new Color(255, 255, 255, 255));

                float boxWidth = 80 * scale;
                float boxHeight = 14 * scale;
                float boxX = getX() + getWidth() - boxWidth;
                float boxY = getY() + (18 * scale - boxHeight) / 2;

                boolean isHovered = RenderUtil.isHovering(boxX, boxY, boxWidth, boxHeight, mouseX, mouseY);
                hoverAnimation.setDirection(isHovered ? Direction.FORWARDS : Direction.BACKWARDS);

                float hoverFactor = (float) hoverAnimation.getOutput();

                Color bgColor = ColorUtil.applyOpacity(new Color(60, 60, 60), 0.7f + hoverFactor * 0.2f);
                NanoVGHelper.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 7 * scale, bgColor);
                NanoVGHelper.drawRoundRectOutline(boxX, boxY, boxWidth, boxHeight, 7 * scale, 1 * scale,
                        isHovered ? ClickGUI.color(0) : new Color(100, 100, 100, 100));

                String currentMode = setting.getCurrent();
                float textWidth = NanoVGHelper.getTextWidth(currentMode, FontLoader.regular(titleFontSize), titleFontSize);
                float textX = boxX + (boxWidth - textWidth) / 2;

                Color textColor = isHovered ? ClickGUI.color(255) : Color.WHITE;
                NanoVGHelper.drawString(currentMode, textX, boxY + 10 * scale,
                        FontLoader.regular(titleFontSize), titleFontSize, textColor);
            });
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            float boxWidth = 80 * scale;
            float boxHeight = 14 * scale;
            float boxX = getX() + getWidth() - boxWidth;
            float boxY = getY() + (18 * scale - boxHeight) / 2;

            if (RenderUtil.isHovering(boxX, boxY, boxWidth, boxHeight, (float) mouseX, (float) mouseY) && mouseButton == 0) {
                setting.next();
                return true;
            }
            return false;
        }

        @Override public boolean mouseReleased(double mouseX, double mouseY, int state) { return false; }
        @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) { return false; }
        @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
        @Override public boolean charTyped(char chr, int modifiers) { return false; }
        @Override public boolean isVisible() { return setting.isAvailable(); }
    }

    // ==================== StringValueComponent ====================
    public static class StringValueComponent extends ValueComponent {
        private final StringValue setting;
        private boolean editing = false;
        private String tempText = "";
        private int cursorPos = 0;
        private long lastBlinkTime = 0;
        private boolean cursorVisible = true;

        public StringValueComponent(StringValue setting) {
            this.setting = setting;
        }

        @Override
        public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
            float baseFontSize = (float) ClickGUI.getFontSize();
            float titleFontSize = baseFontSize * 0.75f;
            setHeight(26 * scale);

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastBlinkTime > 530) {
                cursorVisible = !cursorVisible;
                lastBlinkTime = currentTime;
            }

            NanoVGRenderer.INSTANCE.draw(vg -> {
                NanoVGHelper.drawString(setting.getName(), getX(), getY(), FontLoader.regular(titleFontSize), titleFontSize, new Color(255, 255, 255, 255));

                float inputWidth = getWidth();
                float inputX = getX();
                float inputY = getY() + 5 * scale;
                float inputHeight = 12 * scale;

                NanoVGHelper.drawRoundRect(inputX, inputY, inputWidth, inputHeight, 2 * scale,
                        editing ? new Color(60, 60, 80) : new Color(40, 40, 40));

                NanoVGHelper.drawRoundRectOutline(inputX, inputY, inputWidth, inputHeight, 2 * scale, 0.5f * scale,
                        editing ? new Color(100, 100, 150) : new Color(80, 80, 80));

                String displayText = editing ? tempText : setting.get();
                if (displayText == null) displayText = "";

                float textFontSize = baseFontSize * 0.65f;
                float textWidth = NanoVGHelper.getTextWidth(displayText, FontLoader.regular(textFontSize), textFontSize);
                String trimmedText = displayText;

                if (textWidth > inputWidth - 6 * scale) {
                    while (textWidth > inputWidth - 6 * scale && !trimmedText.isEmpty()) {
                        if (editing && cursorPos == displayText.length()) {
                            trimmedText = trimmedText.substring(1);
                        } else {
                            trimmedText = trimmedText.substring(0, trimmedText.length() - 1);
                        }
                        textWidth = NanoVGHelper.getTextWidth(trimmedText + (editing && cursorPos == displayText.length() ? "" : "..."),
                                FontLoader.regular(textFontSize), textFontSize);
                    }
                    if (!editing || cursorPos < displayText.length()) {
                        trimmedText = trimmedText + "...";
                    }
                }

                NanoVGHelper.drawString(trimmedText, inputX + 2 * scale, inputY + 9 * scale,
                        FontLoader.regular(textFontSize), textFontSize,
                        editing ? new Color(255, 255, 255) : new Color(200, 200, 200));

                if (editing && cursorVisible) {
                    String beforeCursor = tempText.substring(0, Math.min(cursorPos, tempText.length()));
                    float cursorX = inputX + 2 * scale + NanoVGHelper.getTextWidth(beforeCursor, FontLoader.regular(textFontSize), textFontSize);

                    if (cursorX < inputX + inputWidth - 2 * scale) {
                        NanoVGHelper.drawRect(cursorX, inputY + 2 * scale, 0.5f * scale, inputHeight - 4 * scale, new Color(255, 255, 255));
                    }
                }
            });
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
            float inputWidth = getWidth() - 8 * scale;
            float inputX = getX() + 1 * scale;
            float inputY = getY() + 5 * scale;
            float inputHeight = 12 * scale;

            if (RenderUtil.isHovering(inputX, inputY, inputWidth, inputHeight, (float) mouseX, (float) mouseY) && mouseButton == 0) {
                if (!editing) {
                    editing = true;
                    tempText = setting.get();
                    cursorPos = tempText.length();
                    lastBlinkTime = System.currentTimeMillis();
                    cursorVisible = true;
                }
                return true;
            } else if (editing && mouseButton == 0) {
                finishEditing();
            }
            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (!editing) return false;

            switch (keyCode) {
                case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                    finishEditing();
                    return true;
                }
                case GLFW.GLFW_KEY_ESCAPE -> {
                    editing = false;
                    return true;
                }
                case GLFW.GLFW_KEY_BACKSPACE -> {
                    if (cursorPos > 0) {
                        tempText = tempText.substring(0, cursorPos - 1) + tempText.substring(cursorPos);
                        cursorPos--;
                        resetCursor();
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_DELETE -> {
                    if (cursorPos < tempText.length()) {
                        tempText = tempText.substring(0, cursorPos) + tempText.substring(cursorPos + 1);
                        resetCursor();
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_LEFT -> {
                    if (cursorPos > 0) {
                        cursorPos--;
                        resetCursor();
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_RIGHT -> {
                    if (cursorPos < tempText.length()) {
                        cursorPos++;
                        resetCursor();
                    }
                    return true;
                }
                case GLFW.GLFW_KEY_HOME -> {
                    cursorPos = 0;
                    resetCursor();
                    return true;
                }
                case GLFW.GLFW_KEY_END -> {
                    cursorPos = tempText.length();
                    resetCursor();
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
            if (!editing) return false;

            if (setting.isOnlyNumber() && !Character.isDigit(chr) && chr != '.' && chr != '-') {
                return false;
            }

            tempText = tempText.substring(0, cursorPos) + chr + tempText.substring(cursorPos);
            cursorPos++;
            resetCursor();
            return true;
        }

        private void finishEditing() {
            editing = false;
            setting.setText(tempText);
        }

        private void resetCursor() {
            lastBlinkTime = System.currentTimeMillis();
            cursorVisible = true;
        }

        @Override public boolean mouseReleased(double mouseX, double mouseY, int state) { return false; }
        @Override public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) { return false; }
        @Override public boolean isVisible() { return setting.isAvailable(); }
    }
}
