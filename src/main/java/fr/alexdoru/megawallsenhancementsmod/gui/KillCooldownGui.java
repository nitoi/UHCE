package fr.alexdoru.megawallsenhancementsmod.gui;

import fr.alexdoru.fkcountermod.FKCounterMod;
import fr.alexdoru.megawallsenhancementsmod.config.ConfigHandler;
import fr.alexdoru.megawallsenhancementsmod.utils.TimerUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;

public class KillCooldownGui extends MyCachedGui {

    public static KillCooldownGui instance;

    private static final String DUMMY_TEXT = EnumChatFormatting.DARK_RED + "/kill cooldown : 60s";
    private long lastkilltime = 0;
    private final TimerUtil timerKillCooldown = new TimerUtil(60000L);
    private final TimerUtil timerUpdateText = new TimerUtil(1000L);

    public KillCooldownGui() {
        super(ConfigHandler.killcooldownHUDPosition);
        instance = this;
    }

    /**
     * Called to draw the gui, when you use /kill
     */
    public void drawCooldownGui() {
        if (timerKillCooldown.update()) {
            lastkilltime = System.currentTimeMillis();
        }
    }

    public void hideGUI() {
        lastkilltime = 0;
    }

    @Override
    public void updateDisplayText() {
        final int timeleft = 60 - ((int) (System.currentTimeMillis() - lastkilltime)) / 1000;
        displayText = EnumChatFormatting.DARK_RED + "/kill cooldown : " + timeleft + "s";
    }

    @Override
    public void render(ScaledResolution resolution) {
        if (timerUpdateText.update()) {
            updateDisplayText();
        }
        final int[] absolutePos = this.guiPosition.getAbsolutePosition(resolution);
        frObj.drawStringWithShadow(displayText, absolutePos[0], absolutePos[1], 0);
    }

    @Override
    public void renderDummy() {
        final int[] absolutePos = this.guiPosition.getAbsolutePosition();
        frObj.drawStringWithShadow(DUMMY_TEXT, absolutePos[0], absolutePos[1], 0);
    }

    @Override
    public boolean isEnabled() {
        return System.currentTimeMillis() - lastkilltime < 60000L && FKCounterMod.isInMwGame;
    }

}
