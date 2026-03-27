package com.ink.recode.gui.clickgui.component.values;

import com.ink.recode.gui.Component;
import com.ink.recode.modules.impl.render.ClickGUI;
import com.ink.recode.render.nanovg.NanoVGRenderer;
import com.ink.recode.render.nanovg.font.FontLoader;
import com.ink.recode.render.nanovg.util.NanoVGHelper;
import com.ink.recode.utils.render.RenderUtil;
import com.ink.recode.value.NumberValue;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class NumberValueComponent extends Component {
    private final NumberValue setting;
    private boolean dragging = false;

    public NumberValueComponent(NumberValue setting) {
        this.setting = setting;
    }

    @Override
    public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
        float baseFontSize = (float) ClickGUI.getFontSize();
        float titleFontSize = baseFontSize * 0.75f;
        setHeight(22 * scale);

        NanoVGRenderer.INSTANCE.draw(vg -> {
            NanoVGHelper.drawString(setting.getName(), getX(), getY() + 14 * scale, FontLoader.regular(titleFontSize), titleFontSize, new Color(255, 255, 255, 255));

            float sliderWidth = getWidth() - 80 * scale;
            float sliderHeight = 4 * scale;
            float sliderX = getX() + 5 * scale;
            float sliderY = getY() + 16 * scale;

            boolean isHovered = RenderUtil.isHovering(sliderX, sliderY, sliderWidth, sliderHeight, mouseX, mouseY);

            double value = setting.get();
            double min = setting.min;
            double max = setting.max;
            double range = max - min;
            double progress = (value - min) / range;

            NanoVGHelper.drawRoundRect(sliderX, sliderY, sliderWidth, sliderHeight, 2 * scale, new Color(80, 80, 80));
            NanoVGHelper.drawRoundRect(sliderX, sliderY, (float) (sliderWidth * progress), sliderHeight, 2 * scale, ClickGUI.color(0));

            String valueString = String.format("%.2f", value);
            float valueWidth = NanoVGHelper.getTextWidth(valueString, FontLoader.regular(titleFontSize), titleFontSize);
            float valueX = getX() + getWidth() - valueWidth - 5 * scale;
            NanoVGHelper.drawString(valueString, valueX, getY() + 14 * scale, FontLoader.regular(titleFontSize), titleFontSize, ClickGUI.color(0));
        });

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
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

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            updateValue((float) mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
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

    @Override
    public boolean isVisible() {
        return setting.isAvailable();
    }
}