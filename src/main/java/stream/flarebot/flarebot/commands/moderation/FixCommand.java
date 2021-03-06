package stream.flarebot.flarebot.commands.moderation;

import com.arsenarsen.lavaplayerbridge.player.Player;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.ConfirmUtil;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.objects.RunnableWrapper;

import java.awt.Color;
import java.util.Iterator;

public class FixCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
            if (ConfirmUtil.checkExists(sender.getId(), this.getClass())) {
                ConfirmUtil.run(sender.getId(), this.getClass());
                ConfirmUtil.remove(sender.getId(), this.getClass());
            } else {
                MessageUtils.sendErrorMessage("You haven't got any action to confirm!", channel);
            }
            return;
        }
        channel.sendMessage(MessageUtils.getEmbed(sender)
                .setColor(Color.RED)
                .setDescription(GeneralUtils.formatCommandPrefix(channel,
                        "Are you sure you want to fix any potential autoassign roles "
                                + "and FlareBot's nickname if songnick is enabled?"
                                + "\nWe assign roles to users without any so be aware that if you allow "
                                + "the removal of your autoassign roles they may be added back to users."))
                .build()).queue();

        ConfirmUtil.pushAction(sender.getId(),
                new RunnableWrapper(new Runnable() {
                    @Override
                    public void run() {
                        fix(guild, sender, channel);
                    }
                }, this.getClass()));
    }

    private void fix(GuildWrapper guild, User sender, TextChannel channel) {
        int rolesAdded = 0;
        for (Member member1 : guild.getGuild().getMembers()) {
            if (member1.getRoles().size() > 0) continue;
            Iterator<String> iterator = guild.getAutoAssignRoles().iterator();
            while (iterator.hasNext()) {
                Role role = guild.getGuild().getRoleById(iterator.next());
                if (role == null) {
                    iterator.remove();
                } else {
                    if (!member1.getRoles().contains(role)) {
                        guild.getGuild().getController().addRolesToMember(member1, role).queue();
                        rolesAdded++;
                    }
                }
            }
        }
        boolean nickReset = false;
        if (guild.isSongnickEnabled()) {
            Player player = FlareBot.getInstance().getMusicManager().getPlayer(guild.getGuildId());
            String nickname = null;
            if (player.getPlayingTrack() != null) {
                nickname = player.getPlayingTrack().getTrack().getInfo().title;
                if (nickname.length() > 32) {
                    nickname = nickname.substring(0, 32);
                }
                nickname = nickname.substring(0, nickname.lastIndexOf(' ') + 1);
            }
            guild.getGuild().getController()
                    .setNickname(guild.getGuild().getSelfMember(), nickname)
                    .queue();
            nickReset = true;
        } else {
            guild.getGuild().getController().setNickname(guild.getGuild().getSelfMember(), null).queue();
        }

        channel.sendMessage(MessageUtils.getEmbed(sender).setDescription(
                (rolesAdded == 0 && !nickReset ? "No fix needed!\n" +
                        "If you are still having issues, please join our support server here: " + FlareBot.INVITE_URL :
                        "Added " + rolesAdded + " roles. Fixed nick: " + nickReset)).build()).queue();
    }

    @Override
    public String getCommand() {
        return "fix";
    }

    @Override
    public String getDescription() {
        return "A command to fix common errors caused by downtime or crash";
    }

    @Override
    public String getUsage() {
        return "`{%}fix` - Fixes common issues";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }
}
