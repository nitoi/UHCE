package fr.alexdoru.nocheatersmod.commands;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.alexdoru.fkcountermod.FKCounterMod;
import fr.alexdoru.megawallsenhancementsmod.api.cache.CachedHypixelPlayerData;
import fr.alexdoru.megawallsenhancementsmod.api.cache.CachedMojangUUID;
import fr.alexdoru.megawallsenhancementsmod.api.exceptions.ApiException;
import fr.alexdoru.megawallsenhancementsmod.misc.NameModifier;
import fr.alexdoru.megawallsenhancementsmod.utils.ChatUtil;
import fr.alexdoru.megawallsenhancementsmod.utils.DateUtil;
import fr.alexdoru.megawallsenhancementsmod.utils.HypixelApiKeyUtil;
import fr.alexdoru.megawallsenhancementsmod.utils.TabCompletionUtil;
import fr.alexdoru.nocheatersmod.data.TimeMark;
import fr.alexdoru.nocheatersmod.data.WDR;
import fr.alexdoru.nocheatersmod.data.WdredPlayers;
import fr.alexdoru.nocheatersmod.events.GameInfoGrabber;
import fr.alexdoru.nocheatersmod.events.NoCheatersEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class CommandWDR extends CommandBase {

	private static int nbTimeMarks = 0;
	private static HashMap<String,TimeMark> TimeMarksMap = new HashMap<String,TimeMark>();

	private static final char timestampreportkey = '-';
	private static final char timemarkedreportkey = '#';

	@Override
	public String getCommandName() {
		return "watchdogreport";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {

		if (args.length == 0) {
			ChatUtil.addChatMessage((IChatComponent)new ChatComponentText(EnumChatFormatting.RED + "Usage : " + getCommandUsage(sender)));
			return;
		}

		(new Thread(() -> {
			boolean isaTimestampedReport = false;
			boolean usesTimeMark = false;			
			ArrayList<String> arraycheats = new ArrayList<String>();    // for WDR object
			String message = "/wdr " + args[0];
			String playername = args[0];
			String serverID = "?";
			String timerOnReplay = "?";
			long longtimetosubtract = 0;
			long timestamp = 0;

			if(args.length == 1) {
				arraycheats.add("cheating");
			} else {
				for (int i = 1; i < args.length; i++) { // reads each arg one by one

					if(args[i].charAt(0) == timestampreportkey) { // handling timestamped reports

						if(isaTimestampedReport) {
							ChatUtil.addChatMessage((IChatComponent)new ChatComponentText(ChatUtil.getTagNoCheaters()
									+ EnumChatFormatting.RED + "You can't have more than one timestamp in the arguments."));
							return;
						}

						if(usesTimeMark) {
							ChatUtil.addChatMessage((IChatComponent)new ChatComponentText(ChatUtil.getTagNoCheaters()
									+ EnumChatFormatting.RED + "You can't use both special arguments in the same reports!"));
							return;
						}

						isaTimestampedReport = true;
						String rawtimestamp = args[i].substring(1);

						if(args[i] == "-") { // computing the -time argument
							longtimetosubtract=0;
						} else {
							Matcher Matcher0 = Pattern.compile("(\\d+)").matcher(rawtimestamp);
							Matcher Matcher1 = Pattern.compile("(\\d+)s").matcher(rawtimestamp);
							Matcher Matcher2 = Pattern.compile("(\\d+)m").matcher(rawtimestamp);
							Matcher Matcher3 = Pattern.compile("(\\d+)m(\\d+)s").matcher(rawtimestamp);

							if(Matcher0.matches()) {
								longtimetosubtract=Long.parseLong(Matcher0.group(1));
							} else if (Matcher1.matches()) {
								longtimetosubtract=Long.parseLong(Matcher1.group(1));
							} else if (Matcher2.matches()) {
								longtimetosubtract=60*Long.parseLong(Matcher2.group(1));
							} else if (Matcher3.matches()) {
								longtimetosubtract=60*Long.parseLong(Matcher3.group(1)) + Long.parseLong(Matcher3.group(2));
							} 
						}

						timestamp = (new Date()).getTime()-longtimetosubtract*1000; // Milliseconds
						serverID = GameInfoGrabber.getGameIDfromscoreboard();
						timerOnReplay = GameInfoGrabber.getTimeSinceGameStart(timestamp, serverID, (int)longtimetosubtract);

					} else if (args[i].charAt(0) == timemarkedreportkey) { // process the command if you use a stored timestamp 

						if(usesTimeMark) {
							ChatUtil.addChatMessage((IChatComponent)new ChatComponentText(ChatUtil.getTagNoCheaters()
									+ EnumChatFormatting.RED + "You can't use more than one #timestamp in the arguments."));
							return;
						}

						if(isaTimestampedReport) {
							ChatUtil.addChatMessage((IChatComponent)new ChatComponentText(ChatUtil.getTagNoCheaters()
									+ EnumChatFormatting.RED + "You can't use both special arguments in the same reports!"));
							return;
						}

						usesTimeMark = true;

						String key = args[i].substring(1);
						TimeMark timemark = TimeMarksMap.get(key);

						if(timemark == null) {

							ChatUtil.addChatMessage((IChatComponent)new ChatComponentText(ChatUtil.getTagNoCheaters()
									+ EnumChatFormatting.YELLOW + key + EnumChatFormatting.RED + " isn't a valid timestamp #"));
							return;	
						} else {

							timestamp = timemark.timestamp;
							serverID = timemark.serverID;
							timerOnReplay = timemark.timerOnReplay;

						}

					} else if(args[i].contains("stalk") || args[i].equals("test") || args[i].equals("fastbreak")) {
						
						arraycheats.add(args[i]);

					} else if(args[i].equalsIgnoreCase("bhop")) {

						arraycheats.add(args[i]);
						message = message + " bhop aura reach velocity speed antiknockback";

					} else if(args[i].equalsIgnoreCase("autoblock")) {

						arraycheats.add(args[i]);
						message = message + " killaura";

					} else if(args[i].equalsIgnoreCase("noslowdown") || args[i].equalsIgnoreCase("keepsprint")) {

						arraycheats.add(args[i]);
						message = message + " velocity";

					} else {

						arraycheats.add(args[i]);
						message = message + " " + args[i]; //reconstructs the message to send it to the server

					}

				}
				
			}

			if((isaTimestampedReport || usesTimeMark) && args.length == 2) {
				ChatUtil.addChatMessage((IChatComponent)new ChatComponentText(EnumChatFormatting.RED + "Usage : " + getCommandUsage(sender)));
				return;
			}

			if((isaTimestampedReport || usesTimeMark)) { // adds the timestamp to the report
				message = message + " " + (timerOnReplay.equals("?") ? "" : timerOnReplay );
			}
			
			if(!(args.length == 2 && (args[1].contains("stalk") || args[1].equals("test")))) {
				(Minecraft.getMinecraft()).thePlayer.sendChatMessage(message); //sends command to server	
			}
					
			CachedMojangUUID apireq;
			CachedHypixelPlayerData playerdata;
			String uuid = null;
			boolean isaNick = false;

			try {
				apireq = new CachedMojangUUID(args[0]);
				uuid = apireq.getUuid();
				playername = apireq.getName();

				if(uuid != null) {
					playerdata = new CachedHypixelPlayerData(uuid, HypixelApiKeyUtil.getApiKey());
				}

			} catch (ApiException e) {

				if(e.getMessage().equals("This player never joined Hypixel")){
					uuid = null;
				}

			}

			if (uuid == null) {  // The playername doesn't exist or never joined hypixel								

				// search for the player's gameprofile in the tablist			
				for (NetworkPlayerInfo networkplayerinfo : Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap())
				{
					if (networkplayerinfo.getGameProfile().getName().equalsIgnoreCase(args[0]))
					{
						uuid = networkplayerinfo.getGameProfile().getName();
						playername = args[0];
						isaNick = true;
					}
				}

				if(!isaNick) { // couldn't find the nicked player in the tab list
					ChatUtil.addChatMessage((IChatComponent)new ChatComponentText(ChatUtil.getTagNoCheaters()
							+ ChatUtil.invalidplayernameMsg(playername) + EnumChatFormatting.RED + " Couldn't find the " + EnumChatFormatting.DARK_PURPLE + "nicked" + EnumChatFormatting.RED + " player in the tablist"));
					return;
				}												

			} 

			// save the player in the wdr map

			if(isaTimestampedReport || usesTimeMark) { // format for timestamps reports : UUID timestamplastreport -serverID timeonreplay playernameduringgame timestampforcheat specialcheat cheat1 cheat2 cheat3 etc

				ArrayList<String> argsinWDR = new ArrayList<String>();
				argsinWDR.add("-" + serverID);
				argsinWDR.add(timerOnReplay);
				argsinWDR.add(playername);
				argsinWDR.add(Long.toString(timestamp));

				for(String cheat : arraycheats) {
					argsinWDR.add(cheat);
				}

				WDR wdr = WdredPlayers.getWdredMap().get(uuid); // look if he was already reported and add the previous cheats without duplicates

				if (wdr != null) { // the player was already reported before

					//if(wdr.hacks.get(0).charAt(0) == '-') { // previous report was also a timestamped report

					for (int i = 0; i < wdr.hacks.size(); i++) { // adds the previous arguments after the current report
						argsinWDR.add(wdr.hacks.get(i));
					}
					//} 
				}

				if(isaNick) {argsinWDR.add("nick");}

				WDR newreport = new WDR(timestamp, argsinWDR);
				WdredPlayers.getWdredMap().put(uuid, newreport);
				NameModifier.transformDisplayName(playername);
				ChatUtil.addChatMessage((IChatComponent)new ChatComponentText(ChatUtil.getTagNoCheaters() +
						EnumChatFormatting.GREEN +  "You reported " + (isaNick ? EnumChatFormatting.GREEN + "the" + EnumChatFormatting.DARK_PURPLE + " nicked player " : ""))
						.appendSibling(IChatComponent.Serializer.jsonToComponent("[\"\"" + NoCheatersEvents.createPlayerTimestampedMsg(playername, newreport, "light_purple")[0] + "]"))
						.appendSibling(new ChatComponentText(EnumChatFormatting.GREEN + " with a " + EnumChatFormatting.YELLOW +
								"timestamp" + EnumChatFormatting.GREEN + " and will receive warnings about this player in-game"							
								+ EnumChatFormatting.GREEN + (isaNick ? " for the next 48 hours." : "."))));
				return;

			} else {  // isn't a timestamped report

				ArrayList<String> argsinWDR = new ArrayList<String>();
				WDR wdr = WdredPlayers.getWdredMap().get(uuid); // look if he was already reported and add the previous cheats without duplicates

				if (wdr != null) { // the player was already reported before

					for (int i = 0; i < wdr.hacks.size(); i++) {
						argsinWDR.add(wdr.hacks.get(i));
					}

					for (int i = 0; i < arraycheats.size(); i++) { // adds the report at the end of the previous informations and avoids duplicates
						boolean doublon = false;
						for(String arg : argsinWDR) {
							if((arraycheats.get(i)).equals(arg)) {
								doublon = true;
							}
						}
						if(doublon==false) {
							argsinWDR.add(arraycheats.get(i));
						}
					}

				} else {
					for(String cheat : arraycheats) {
						argsinWDR.add(cheat);
					}
				}

				if(isaNick) {argsinWDR.add("nick");}
				WdredPlayers.getWdredMap().put(uuid, new WDR((new Date()).getTime(), argsinWDR));				
				NameModifier.transformDisplayName(playername);
				ChatUtil.addChatMessage((IChatComponent)new ChatComponentText(ChatUtil.getTagNoCheaters() +
						EnumChatFormatting.GREEN +  "You reported " + (isaNick ? EnumChatFormatting.GREEN + "the" + EnumChatFormatting.DARK_PURPLE + " nicked player " : "") 
						+ EnumChatFormatting.LIGHT_PURPLE + playername + EnumChatFormatting.GREEN + " and will receive warnings about this player in-game"
						+ EnumChatFormatting.GREEN + (isaNick ? " for the next 48 hours." : ".")));
				return;				

			}

		})).start();
		
	}

	@Override
	public List<String> getCommandAliases() {
		ArrayList<String> res = new ArrayList<String>();
		res.add("wdr");
		res.add("Wdr");
		res.add("WDR");
		return res;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/wdr <player> <cheats(optional)> <timestamp(optional)>";
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		//return args.length == 1 ? ( FKCounterMod.isInMwGame() && !GameInfoGrabber.isitPrepPhase() ? getListOfStringsMatchingLastWord(args, TabCompletionUtil.getOnlinePlayersByName()) : null ) : (args.length > 1 ? getListOfStringsMatchingLastWord(args, cheats) : null);
		return args.length == 1 ? ( FKCounterMod.isInMwGame() && FKCounterMod.isitPrepPhase() ? getListOfStringsMatchingLastWord(args, TabCompletionUtil.getOnlinePlayersByName()) : null ) : (args.length > 1 ? getListOfStringsMatchingLastWord(args, CommandReport.cheatsArray) : null);
	}

	public static void addTimeMark() {

		nbTimeMarks++;

		String key = String.valueOf(nbTimeMarks);
		long timestamp = (new Date()).getTime();
		String serverID = GameInfoGrabber.getGameIDfromscoreboard();
		String timerOnReplay = GameInfoGrabber.getTimeSinceGameStart(timestamp, serverID, 0);
		TimeMarksMap.put(key, new TimeMark(timestamp , serverID, timerOnReplay));

		ChatUtil.addChatMessage((IChatComponent)new ChatComponentText(ChatUtil.getTagNoCheaters()

					+ EnumChatFormatting.GREEN + "Added timestamp : " + EnumChatFormatting.GOLD + "#" + key + EnumChatFormatting.GREEN + ".")

				.setChatStyle(new ChatStyle()
						.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
								new ChatComponentText(EnumChatFormatting.GREEN + "Key : " + EnumChatFormatting.GOLD + "#" + key + "\n" +
										EnumChatFormatting.GREEN + "Timestamp : " + EnumChatFormatting.GOLD + DateUtil.ESTformatTimestamp(timestamp) + "\n" +
										EnumChatFormatting.GREEN + "ServerID : " + EnumChatFormatting.GOLD + serverID + "\n" +
										EnumChatFormatting.GREEN + "Timer on replay (approx.) : " + EnumChatFormatting.GOLD + timerOnReplay + "\n" + 
										EnumChatFormatting.YELLOW + "Click to fill a report with this timestmap")))
						.setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/wdr  " + "#" + key))));

	}

}