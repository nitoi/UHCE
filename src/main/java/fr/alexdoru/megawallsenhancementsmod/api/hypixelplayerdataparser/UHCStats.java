package fr.alexdoru.megawallsenhancementsmod.api.hypixelplayerdataparser;

import com.google.gson.JsonObject;
import fr.alexdoru.megawallsenhancementsmod.utils.JsonUtil;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import static fr.alexdoru.megawallsenhancementsmod.utils.ChatUtil.*;

public class UHCStats {

    private int coins;
    private int score;
    // teams
    private int deaths;
    private int kills;
    private int heads_eaten;
    private int wins;

    private int deaths_solo;
    private int kills_solo;
    private int heads_eaten_solo;
    private int wins_solo;

    private int deaths_total;
    private int kills_total;
    private int heads_eaten_total;
    private int wins_total;

    private float kdr_team;
    private float kdr_solo;
    private float kdr_total;

    public UHCStats(JsonObject playerData) {

        if (playerData == null) {
            return;
        }

        JsonObject statsdata = playerData.get("stats").getAsJsonObject();

        if (statsdata == null) {
            return;
        }

        JsonObject uhcData = JsonUtil.getJsonObject(statsdata, "UHC");

        if (uhcData == null) {
            return;
        }

        coins = JsonUtil.getInt(uhcData, "coins");
        score = JsonUtil.getInt(uhcData, "score");
        deaths = JsonUtil.getInt(uhcData, "deaths");
        kills = JsonUtil.getInt(uhcData, "kills");
        heads_eaten = JsonUtil.getInt(uhcData, "heads_eaten");
        wins = JsonUtil.getInt(uhcData, "wins");
        deaths_solo = JsonUtil.getInt(uhcData, "deaths_solo");
        kills_solo = JsonUtil.getInt(uhcData, "kills_solo");
        heads_eaten_solo = JsonUtil.getInt(uhcData, "heads_eaten_solo");
        wins_solo = JsonUtil.getInt(uhcData, "wins_solo");

        deaths_total = deaths + deaths_solo;
        kills_total = kills + kills_solo;
        heads_eaten_total = heads_eaten + heads_eaten_solo;
        wins_total = wins + wins_solo;

        kdr_team = (float) kills / (deaths == 0 ? 1 : (float) deaths);
        kdr_solo = (float) kills_solo / (deaths_solo == 0 ? 1 : (float) deaths_solo);
        kdr_total = (float) kills_total / (deaths_total == 0 ? 1 : (float) deaths_total);

    }

    private int getStarLevel(int score) {
        if (score < 10) {
            return 1;
        } else if (score < 60) {
            return 2;
        } else if (score < 210) {
            return 3;
        } else if (score < 460) {
            return 4;
        } else if (score < 960) {
            return 5;
        } else if (score < 1710) {
            return 6;
        } else if (score < 2710) {
            return 7;
        } else if (score < 5210) {
            return 8;
        } else if (score < 10210) {
            return 9;
        } else if (score < 13210) {
            return 10;
        } else if (score < 16210) {
            return 11;
        } else if (score < 19210) {
            return 12;
        } else if (score < 22210) {
            return 13;
        } else if (score < 25210) {
            return 14;
        } else {
            return 15;
        }
    }

    public IChatComponent getFormattedMessage(String formattedName, String playername) {

        String[][] matrix = {
                {
                        EnumChatFormatting.YELLOW + "Overall : ",
                        EnumChatFormatting.AQUA + "W : " + EnumChatFormatting.GOLD + formatInt(wins_total) + " ",
                        EnumChatFormatting.AQUA + "Heads : " + EnumChatFormatting.GOLD + formatInt(heads_eaten_total)
                },

                {
                        EnumChatFormatting.AQUA + "K : " + EnumChatFormatting.GOLD + formatInt(kills_total) + " ",
                        EnumChatFormatting.AQUA + "D : " + EnumChatFormatting.RED + formatInt(deaths_total) + " ",
                        EnumChatFormatting.AQUA + "K/D : " + (kdr_total > 1 ? EnumChatFormatting.GOLD : EnumChatFormatting.RED) + String.format("%.2f", kdr_total) + "\n"
                },

                {
                        EnumChatFormatting.YELLOW + "Solo : ",
                        EnumChatFormatting.AQUA + "W : " + EnumChatFormatting.GOLD + formatInt(wins_solo) + " ",
                        EnumChatFormatting.AQUA + "Heads : " + EnumChatFormatting.GOLD + formatInt(heads_eaten_solo)
                },

                {
                        EnumChatFormatting.AQUA + "K : " + EnumChatFormatting.GOLD + formatInt(kills_solo) + " ",
                        EnumChatFormatting.AQUA + "D : " + EnumChatFormatting.RED + formatInt(deaths_solo) + " ",
                        EnumChatFormatting.AQUA + "K/D : " + (kdr_solo > 1 ? EnumChatFormatting.GOLD : EnumChatFormatting.RED) + String.format("%.2f", kdr_solo) + "\n"
                },

                {
                        EnumChatFormatting.YELLOW + "Team : ",
                        EnumChatFormatting.AQUA + "W : " + EnumChatFormatting.GOLD + formatInt(wins) + " ",
                        EnumChatFormatting.AQUA + "Heads : " + EnumChatFormatting.GOLD + formatInt(heads_eaten)
                },

                {
                        EnumChatFormatting.AQUA + "K : " + EnumChatFormatting.GOLD + formatInt(kills) + " ",
                        EnumChatFormatting.AQUA + "D : " + EnumChatFormatting.RED + formatInt(deaths) + " ",
                        EnumChatFormatting.AQUA + "K/D : " + (kdr_team > 1 ? EnumChatFormatting.GOLD : EnumChatFormatting.RED) + String.format("%.2f", kdr_team) + "\n"
                }};

        return new ChatComponentText(EnumChatFormatting.AQUA + bar() + "\n")
                .appendSibling(PlanckeHeaderText(EnumChatFormatting.GOLD + "[" + getStarLevel(score) + '\u272B' + "] " + formattedName, playername, " - UHC stats"))
                .appendSibling(new ChatComponentText("\n" + "\n"))
                .appendSibling(new ChatComponentText(alignText(matrix) + "\n"))
                .appendSibling(new ChatComponentText(centerLine(EnumChatFormatting.GREEN + "Score : " + EnumChatFormatting.GOLD + formatInt(score) + EnumChatFormatting.GREEN + " Coins : " + EnumChatFormatting.GOLD + formatInt(coins) + "\n")))
                .appendSibling(new ChatComponentText(EnumChatFormatting.AQUA + bar()));
    }

}