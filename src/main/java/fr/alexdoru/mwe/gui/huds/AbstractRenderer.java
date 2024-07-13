package fr.alexdoru.mwe.gui.huds;

import fr.alexdoru.mwe.gui.guiapi.GuiPosition;
import fr.alexdoru.mwe.gui.guiapi.IRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public abstract class AbstractRenderer extends Gui implements IRenderer {

    protected static final Minecraft mc = Minecraft.getMinecraft();
    protected final GuiPosition guiPosition;

    public AbstractRenderer(GuiPosition guiPosition) {
        this.guiPosition = guiPosition;
    }

    @Override
    public GuiPosition getGuiPosition() {
        return this.guiPosition;
    }

}