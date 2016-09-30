package com.bwfcwalshy.flarebot.commands.administrator;

import com.bwfcwalshy.flarebot.FlareBot;
import com.bwfcwalshy.flarebot.MessageUtils;
import com.bwfcwalshy.flarebot.commands.Command;
import com.bwfcwalshy.flarebot.commands.CommandType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateCommand implements Command {

    @Override
    public void onCommand(IUser sender, IChannel channel, IMessage message, String[] args) {
        if(getPermissions(channel).isCreator(sender)){
            try {
                URL url = new URL("https://raw.githubusercontent.com/bwfcwalshyPluginDev/FlareBot/master/pom.xml");
                boolean searching = true;
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while (searching) {
                    line = br.readLine();
                    if(line != null && (line.contains("<version>") && line.contains("</version>"))){
                        searching = false;
                        String latestVersion = line.replace("<version>", "").replace("</version>", "").replaceAll(" ", "").replaceAll("\t", "");
                        String currentVersion = FlareBot.getInstance().getVersion();
                        if(isHigher(latestVersion, currentVersion)){
                            MessageUtils.sendMessage(channel, "Updating to version `" + latestVersion + "` from `" + currentVersion + "`");
                            FlareBot.getInstance().quit(true);
                        }else{
                            MessageUtils.sendMessage(channel, "I am currently up to date! Current version: `" + currentVersion + "`");
                        }
                    }
                }
            }catch(IOException e){
                FlareBot.LOGGER.error("Could not update!", e);
            }
        }
    }

    /**
     * Check if a string is higher than another.
     * @param s1 This is the string that will be checked. Use this for things like latest version.
     * @param s2 This is the string being compared with. Use this for things like current version.
     * @return If s1 is greater than s2.
     */
    private boolean isHigher(String s1, String s2) {
        String[] split = s1.split("\\.");
        int s1Major = Integer.parseInt(split[0]);
        int s1Minor = Integer.parseInt(split[1]);
        int s1Build = 0;
        if (split.length == 3)
            s1Build = Integer.parseInt(split[2]);

        String[] split2 = s2.split("\\.");
        int s2Major = Integer.parseInt(split[0]);
        int s2Minor = Integer.parseInt(split[0]);
        int s2Build = 0;
        if (split2.length == 3)
            s2Build = Integer.parseInt(split[0]);

        return s1Major > s2Major || s1Minor > s2Minor || s1Build > s2Build;
    }

    @Override
    public String getCommand() {
        return "update";
    }

    @Override
    public String getDescription() {
        return "Update the bot.";
    }

    @Override
    public CommandType getType() {
        return CommandType.HIDDEN;
    }
}