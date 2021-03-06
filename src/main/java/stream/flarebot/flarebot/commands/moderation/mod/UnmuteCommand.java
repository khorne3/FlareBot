package stream.flarebot.flarebot.commands.moderation.mod;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;

public class UnmuteCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length != 1) {
            MessageUtils.getUsage(this, channel, sender).queue();
        } else {
            User user = GeneralUtils.getUser(args[0], guild.getGuildId());
            if (user == null) {
                MessageUtils.sendErrorMessage("Invalid user!!", channel);
                return;
            }
            if (guild.getMutedRole() == null) {
                MessageUtils.sendErrorMessage("Error getting the \"Muted\" role! Check FlareBot has permissions to create it!", channel);
                return;
            }
            if (guild.getGuild().getMember(user).getRoles().contains(guild.getMutedRole())) {
                guild.getGuild().getController().removeSingleRoleFromMember(guild.getGuild().getMember(user), guild.getMutedRole()).queue();
                EmbedBuilder eb = new EmbedBuilder();
                eb.appendDescription("Unmuted " + user.getAsMention());
                eb.setColor(Color.CYAN);
                channel.sendMessage(eb.build()).queue();
            } else {
                MessageUtils.sendErrorMessage("That user isn't muted!!", channel);
            }

        }
    }

    @Override
    public String getCommand() {
        return "unmute";
    }

    @Override
    public String getDescription() {
        return "Unmutes a user";
    }

    @Override
    public String getUsage() {
        return "`{%}unmute <user>` - unmutes a user";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }
}
