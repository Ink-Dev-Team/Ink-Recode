package com.ink.recode.gui.clickgui.component.values;

import com.ink.recode.gui.Component;
import com.ink.recode.modules.impl.render.ClickGUI;
import com.ink.recode.render.nanovg.NanoVGRenderer;
import com.ink.recode.render.nanovg.font.FontLoader;
import com.ink.recode.render.nanovg.util.NanoVGHelper;
import com.ink.recode.utils.render.RenderUtil;
import com.ink.recode.value.ModeValue;
import net.minecraft.client.gui.DrawContext;

import java.awt.*;

public class EnumValueComponent extends Component {
    private final ModeValue setting;

    public EnumValueComponent(ModeValue setting) {
        this.setting = setting;
    }

    @Override
    public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTicks) {
        float baseFontSize = (float) ClickGUI.getFontSize();
        float titleFontSize = baseFontSize * 0.75f;
        setHeight(18 * scale);

        NanoVGRenderer.INSTANCE.draw(vg -> {
            NanoVGHelper.drawString(setting.getName(), getX(), getY() + 12 * scale, FontLoader.regular(titleFontSize), titleFontSize, new Color(255, 255, 255, 255));

            float boxWidth = 80 * scale;
            float boxHeight = 14 * scale;
            float boxX = getX() + getWidth() - boxWidth;
            float boxY = getY() + (18 * scale - boxHeight) / 2;

            boolean isHovered = RenderUtil.isHovering(boxX, boxY, boxWidth, boxHeight, mouseX, mouseY);

            NanoVGHelper.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 7 * scale, new Color(60, 60, 60));

            String currentMode = setting.getCurrent();
            float textWidth = NanoVGHelper.getTextWidth(currentMode, FontLoader.regular(titleFontSize), titleFontSize);
            float textX = boxX + (boxWidth - textWidth) / 2;
            NanoVGHelper.drawString(currentMode, textX, boxY + 10 * scale, FontLoader.regular(titleFontSize), titleFontSize, Color.WHITE);
        });

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
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

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean isVisible() {
        return setting.isAvailable();
    }
}