package stream.flarebot.flarebot.commands.moderation;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.Group;
import stream.flarebot.flarebot.util.GeneralUtils;
import stream.flarebot.flarebot.util.MessageUtils;

import java.awt.Color;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

public class PermissionsCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length > 2){
            if(args[0].equals("group")){
                String groupString = args[1];
                if(args[2].equals("add")){
                    if(args.length == 4) {
                        Group group = getPermissions(channel).getGroup(groupString);
                        if (group == null) {
                            MessageUtils.sendErrorMessage("That group doesn't exist!! You can create it with `" + getPrefix(channel.getGuild()) + "permissions group " + groupString + " create`", channel);
                            return;
                        } else {
                            if(group.addPermission(args[3])){
                                EmbedBuilder eb = MessageUtils.getEmbed(sender);
                                eb.setDescription("Successfully added the permission `" + args[3] + "` from the group `" + groupString + "`");
                                eb.setColor(Color.GREEN);
                                channel.sendMessage(eb.build()).queue();
                                return;
                            } else {
                                MessageUtils.sendErrorMessage("Couldn't add the permission (it probably already exists)", channel);
                                return;
                            }
                        }
                    }
                } else if(args[2].equals("remove")){
                    if(args.length == 4) {
                        Group group = getPermissions(channel).getGroup(groupString);
                        if (group == null) {
                            MessageUtils.sendErrorMessage("That group doesn't exist!!", channel);
                            return;
                        } else {
                            if(group.removePermission(args[3])){
                                EmbedBuilder eb = MessageUtils.getEmbed(sender);
                                eb.setDescription("Successfully removed the permission `" + args[3] + "` from the group `" + groupString + "`");
                                eb.setColor(Color.GREEN);
                                channel.sendMessage(eb.build()).queue();
                                return;
                            } else {
                                MessageUtils.sendErrorMessage("Couldn't remove the permission (it probably didn't exist)", channel);
                                return;
                            }
                        }
                    }
                } else if(args[2].equals("create")){
                    if(getPermissions(channel).addGroup(groupString)){
                        EmbedBuilder eb = MessageUtils.getEmbed(sender);
                        eb.setDescription("Successfully created group: `" + groupString + "`");
                        eb.setColor(Color.GREEN);
                        channel.sendMessage(eb.build()).queue();
                        return;
                    } else {
                        MessageUtils.sendErrorMessage("That group allready exists!!", channel);
                        return;
                    }
                } else if(args[2].equals("delete")){
                    if(args.length == 4) {
                        if (getPermissions(channel).getGroup(groupString) == null) {
                            MessageUtils.sendErrorMessage("That group doesn't exist!!", channel);
                            return;
                        } else {
                            getPermissions(channel).deleteGroup(groupString);
                        }
                        return;
                    }
                } else if(args[2].equals("link")){
                    if(args.length == 4) {
                        Group group = getPermissions(channel).getGroup(groupString);
                        if (group == null) {
                            MessageUtils.sendErrorMessage("That group doesn't exist!! You can create it with `" + getPrefix(channel.getGuild()) + "permissions group " + groupString + " create`", channel);
                            return;
                        } else {
                            Role role = GeneralUtils.getRole(args[3], guild.getGuildId());
                            if(role != null) {
                                group.linkRole(role.getId());
                                EmbedBuilder eb = MessageUtils.getEmbed(sender);
                                eb.appendDescription("Successfully linked the group `" + groupString + "` to the role `" + role.getName() + "`");
                                eb.setColor(Color.GREEN);
                                channel.sendMessage(eb.build()).queue();
                                return;
                            } else {
                                MessageUtils.sendErrorMessage("That role doesn't exist!", channel);
                                return;
                            }
                        }
                    }
                } else if(args[2].equals("list")){
                    if(args.length == 3 || args.length == 4) {
                        Group group = getPermissions(channel).getGroup(groupString);
                        if (group == null) {
                            MessageUtils.sendErrorMessage("That group doesn't exist!!", channel);
                            return;
                        } else {
                            int page = args.length == 4 ? Integer.valueOf(args[3]) : 1;
                            Set<String> perms = group.getPermissions();
                            String list = getStringList(perms, page);
                            EmbedBuilder eb = MessageUtils.getEmbed(sender);
                            eb.addField("Perms", list, false);
                            eb.addField("Current page", String.valueOf(page),true);
                            int pageSize = 20;
                            int pages = perms.size() < pageSize ? 1 : (perms.size() / pageSize) + (perms.size() % pageSize != 0 ? 1 : 0);
                            eb.addField("Pages", String.valueOf(pages), true);
                            eb.setColor(Color.CYAN);
                            channel.sendMessage(eb.build()).queue();
                            return;
                        }
                    }
                } else {
                    return;
                }
            } else if(args[0].equals("user")){
                String userString = args[1];
                User user = GeneralUtils.getUser(userString, guild.getGuildId());
                if (user == null) {
                    MessageUtils.sendErrorMessage("That user doesn't exist!!", channel);
                    return;
                }
                stream.flarebot.flarebot.permissions.User permUser = getPermissions(channel).getUser(guild.getGuild().getMember(user));
                if(args[2].equals("group")){
                    if(args.length >= 4){
                        if(args[3].equals("add")){
                            if(args.length == 5){
                                String groupString = args[4];
                                Group group = getPermissions(channel).getGroup(groupString);
                                if(group == null){
                                    MessageUtils.sendErrorMessage("That group doesn't exists!! You can create it with `" + getPrefix(channel.getGuild()) + "permissions group " + groupString + " create`", channel);
                                    return;
                                }
                                permUser.addGroup(group);
                                EmbedBuilder eb = MessageUtils.getEmbed(sender);
                                eb.appendDescription("Successfully added the group `" + groupString + "` to " + user.getAsMention());
                                eb.setColor(Color.GREEN);
                                channel.sendMessage(eb.build()).queue();
                                return;
                            }
                        } else if(args[3].equals("remove")){
                            if(args.length == 5){
                                String groupString = args[4];
                                Group group = getPermissions(channel).getGroup(groupString);
                                if(group == null){
                                    MessageUtils.sendErrorMessage("That group doesn't exists!!", channel);
                                    return;
                                }
                                if(permUser.removeGroup(group)) {
                                    EmbedBuilder eb = MessageUtils.getEmbed(sender);
                                    eb.appendDescription("Successfully removed the group `" + groupString + "` from " + user.getAsMention());
                                    eb.setColor(Color.GREEN);
                                    channel.sendMessage(eb.build()).queue();
                                    return;
                                } else {
                                    MessageUtils.sendErrorMessage("The user doesn't have that group!!", channel);
                                    return;
                                }
                            }
                        } else if(args[3].equals("list")){
                            int page = args.length == 5 ? Integer.valueOf(args[4]) : 1;
                            Set<String> groups = permUser.getGroups();
                            String list = getStringList(groups, page);
                            EmbedBuilder eb = MessageUtils.getEmbed(sender);
                            eb.addField("Perms", list, false);
                            eb.addField("Current page", String.valueOf(page),true);
                            int pageSize = 20;
                            int pages = groups.size() < pageSize ? 1 : (groups.size() / pageSize) + (groups.size() % pageSize != 0 ? 1 : 0);
                            eb.addField("Pages", String.valueOf(pages), true);
                            eb.setColor(Color.CYAN);
                            channel.sendMessage(eb.build()).queue();
                            return;
                        }
                    }
                } else if(args[2].equals("permission")) {
                    if(args.length >= 4){
                        if(args[3].equals("add")){
                            if(args.length == 5){
                                if(permUser.addPermission(args[4])){
                                        EmbedBuilder eb = MessageUtils.getEmbed(sender);
                                        eb.appendDescription("Successfully added the permission `" + args[4] + "` to " + user.getAsMention());
                                        eb.setColor(Color.GREEN);
                                        channel.sendMessage(eb.build()).queue();
                                        return;
                                    } else {
                                        MessageUtils.sendErrorMessage("The user doesn't have that permission!!", channel);
                                        return;
                                }
                            }
                        } else if(args[3].equals("remove")){
                            if(args.length == 5){
                                if(permUser.removePermission(args[4])){
                                    EmbedBuilder eb = MessageUtils.getEmbed(sender);
                                    eb.appendDescription("Successfully removed the permission `" + args[4] + "` from " + user.getAsMention());
                                    eb.setColor(Color.GREEN);
                                    channel.sendMessage(eb.build()).queue();
                                    return;
                                } else {
                                    MessageUtils.sendErrorMessage("The user already has that permission!!", channel);
                                    return;
                                }
                            }
                        } else if(args[3].equals("list")){
                            int page = args.length == 5 ? Integer.valueOf(args[4]) : 1;
                            Set<String> perms = permUser.getPermissions();
                            String list = getStringList(perms, page);
                            EmbedBuilder eb = MessageUtils.getEmbed(sender);
                            eb.addField("Perms", list, false);
                            eb.addField("Current page", String.valueOf(page),true);
                            int pageSize = 20;
                            int pages = perms.size() < pageSize ? 1 : (perms.size() / pageSize) + (perms.size() % pageSize != 0 ? 1 : 0);
                            eb.addField("Pages", String.valueOf(pages), true);
                            eb.setColor(Color.CYAN);
                            channel.sendMessage(eb.build()).queue();
                            return;
                        }
                    }
                }
            }
        }
        MessageUtils.getUsage(this, channel, sender).queue();
    }

    private String getStringList(Set<String> perms, int page){
        int pageSize = 20;
        int pages = perms.size() < pageSize ? 1 : (perms.size() / pageSize) + (perms.size() % pageSize != 0 ? 1 : 0);
        int start;
        int end;

        start = pageSize * (page - 1);
        end = Math.min(start + pageSize, perms.size());
        if(page > pages || page < 0){
            return null;
        }
        String[] permsList = new String[perms.size()];
        permsList = perms.toArray(permsList);
        permsList = Arrays.copyOfRange(permsList, start, end);
        StringBuilder sb = new StringBuilder();
        sb.append("```\n");
        for(String perm : permsList){
            sb.append(perm + "\n");
        }
        sb.append("```");
        return sb.toString();
    }

    @Override
    public String getCommand() {
        return "permissions";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"perm", "perms"};
    }

    @Override
    public String getDescription() {
        return "Manages server-wide permissions for FlareBot.";
    }

    @Override
    public String getUsage() {
        return "**`{%}permissions group <group>`  - All usage in this section starts with this**\n" +
                "`add <perm>` - Adds a permission to a group\n" +
                "`remove <perm>` - removes a perm from a group\n" +
                "`create` - creates a group\n" +
                "`delete` - deletes the group\n" +
                "`link <role>` - links the group to a discord role\n" +
                "`list [page]` - lists the permissions this group has\n" +
                "\n" +
                "**`{%}permissions user <user>` - All usage in this section starts with this**\n" +
                "`group add <group>` - adds a group to this user\n" +
                "`group remove <group>` - removes a group from this user\n" +
                "`group list [page]` - lists the groups this user is in\n" +
                "`permissions add <perm>` - adds a permissions to this user\n" +
                "`permissions remove <perm>` - removes a permission from this user\n" +
                "`permissions list [page]` - list the permmissions this user has (exulding those obtained from groups)";
    }

    @Override
    public CommandType getType() {
        return CommandType.MODERATION;
    }

    @Override
    public boolean isDefaultPermission() {
        return false;
    }

    @Override
    public EnumSet<Permission> getDiscordPermission() {
        return EnumSet.of(Permission.MANAGE_PERMISSIONS);
    }
}