package com.ink.recode.gui.clickgui.component.values;

import com.ink.recode.gui.Component;
import com.ink.recode.modules.impl.render.ClickGUI;
import com.ink.recode.render.nanovg.NanoVGRenderer;
import com.ink.recode.render.nanovg.font.FontLoader;
import com.ink.recode.render.nanovg.util.NanoVGHelper;
import com.ink.recode.utils.render.RenderUtil;
import com.ink.recode.value.BooleanValue;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class BoolValueComponent extends Component {
    private final BooleanValue setting;

    public BoolValueComponent(BooleanValue setting) {
        this.setting = setting;
    }

    @Override
    public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
        float baseFontSize = (float) ClickGUI.getFontSize();
        float titleFontSize = baseFontSize * 0.75f;
        setHeight(18 * scale);

        NanoVGRenderer.INSTANCE.draw(vg -> {
            NanoVGHelper.drawString(setting.getName(), getX(), getY() + 12 * scale, FontLoader.regular(titleFontSize), titleFontSize, new Color(255, 255, 255, 255));

            float toggleWidth = 30 * scale;
            float toggleHeight = 14 * scale;
            float toggleX = getX() + getWidth() - toggleWidth;
            float toggleY = getY() + (18 * scale - toggleHeight) / 2;

            boolean isHovered = RenderUtil.isHovering(toggleX, toggleY, toggleWidth, toggleHeight, mouseX, mouseY);

            NanoVGHelper.drawRoundRect(toggleX, toggleY, toggleWidth, toggleHeight, 7 * scale, setting.get() ? ClickGUI.color(0) : new Color(80, 80, 80));

            float indicatorSize = 10 * scale;
            float indicatorX = setting.get() ? toggleX + toggleWidth - indicatorSize - 2 * scale : toggleX + 2 * scale;
            float indicatorY = toggleY + (toggleHeight - indicatorSize) / 2;

            NanoVGHelper.drawRoundRect(indicatorX, indicatorY, indicatorSize, indicatorSize, 5 * scale, Color.WHITE);
        });

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        float baseFontSize = (float) ClickGUI.getFontSize();
        float toggleWidth = 30 * scale;
        float toggleHeight = 14 * scale;
        float toggleX = getX() + getWidth() - toggleWidth;
        float toggleY = getY() + (18 * scale - toggleHeight) / 2;

        if (RenderUtil.isHovering(toggleX, toggleY, toggleWidth, toggleHeight, (float) mouseX, (float) mouseY) && mouseButton == 0) {
            setting.set(!setting.get());
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean isVisible() {
        return setting.isAvailable();
    }
}