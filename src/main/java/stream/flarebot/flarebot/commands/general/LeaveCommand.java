package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;

import java.awt.Color;

public class LeaveCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (channel.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
            if ((!member.getVoiceState().inVoiceChannel() || !channel.getGuild().getSelfMember().getVoiceState()
                    .getAudioChannel().getId()
                    .equals(member.getVoiceState().getAudioChannel()
                            .getId())) && !getPermissions(channel)
                    .hasPermission(member, "flarebot.leave.other")) {
                channel.sendMessage(new EmbedBuilder().setColor(Color.red)
                        .setDescription("You need the permission `flarebot.leave.other` for me to leave a different voice channel!")
                        .build())
                        .queue();
                return;
            }
            channel.getGuild().getAudioManager().closeAudioConnection();
        }
    }

    @Override
    public String getCommand() {
        return "leave";
    }

    @Override
    public String getDescription() {
        return "Tell me to leave the voice channel.";
    }

    @Override
    public String getUsage() {
        return "`{%}leave` - Makes FlareBot leave its current channel";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"gtfo", "banish", "getout"};
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    @Override
    public String getPermission() {
        return "flarebot.leave";
    }
}
