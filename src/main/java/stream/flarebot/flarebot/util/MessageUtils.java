package stream.flarebot.flarebot.util;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.requests.RestAction;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.Markers;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.scheduler.FlarebotTask;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageUtils {

    private static final Pattern INVITE_REGEX = Pattern
            .compile("(?:https?://)?discord(?:app\\.com/invite|\\.gg)/(\\S+?)");
    private static final Pattern LINK_REGEX = Pattern
            .compile("((http(s)?://)(www\\.)?)[a-zA-Z0-9-]+\\.[a-zA-Z0-9]+(\\.[a-zA-Z0-9]+)?/?(.+)?");
    private static final Pattern YOUTUBE_LINK_REGEX = Pattern
            .compile("(http(s)?://)?(www\\.)?youtu(be\\.com)?(\\.be)?/(watch\\?v=)?[a-zA-Z0-9-_]+");

    private static final Pattern ESCAPE_MARKDOWN = Pattern.compile("[`~*_\\\\]");

    public static <T> Consumer<T> noOpConsumer() {
        return t -> {
        };
    }

    public static int getLength(EmbedBuilder embed) {
        int len = 0;
        MessageEmbed e = embed.build();
        if (e.getTitle() != null) len += e.getTitle().length();
        if (e.getDescription() != null) len += e.getDescription().length();
        if (e.getAuthor() != null) len += e.getAuthor().getName().length();
        if (e.getFooter() != null) len += e.getFooter().getText().length();
        if (e.getFields() != null) {
            for (MessageEmbed.Field f : e.getFields()) {
                len += f.getName().length() + f.getValue().length();
            }
        }
        return len;
    }

    public static Message sendPM(User user, String message) {
        try {
            return user.openPrivateChannel().complete()
                    .sendMessage(message.substring(0, Math.min(message.length(), 1999))).complete();
        } catch (ErrorResponseException e) {
            return null;
        }
    }

    public static Message sendPM(MessageChannel channel, User user, String message) {
        try {
            return user.openPrivateChannel().complete()
                    .sendMessage(message.substring(0, Math.min(message.length(), 1999))).complete();
        } catch (ErrorResponseException e) {
            channel.sendMessage(message).queue();
            return null;
        }
    }

    public static Message sendPM(MessageChannel channel, User user, EmbedBuilder message) {
        try {
            return user.openPrivateChannel().complete().sendMessage(message.build()).complete();
        } catch (ErrorResponseException e) {
            channel.sendMessage(message.build()).queue();
            return null;
        }
    }

    public static Message sendException(String s, Throwable e, MessageChannel channel) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String trace = sw.toString();
        pw.close();
        return sendErrorMessage(getEmbed().setDescription(s + "\n**Stack trace**: " + hastebin(trace)), channel);
    }

    public static String hastebin(String trace) {
        try {
            Response response = WebUtils.post("https://hastebin.com/documents", WebUtils.APPLICATION_JSON, trace);
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            if(response.body() != null) {
                String key = new JSONObject(response.body().string()).getString("key");
                return "https://hastebin.com/" + key;
            } else {
                FlareBot.LOGGER.error(Markers.NO_ANNOUNCE, "Hastebin is down");
                return null;
            }
        } catch (IOException | JSONException e) {
            FlareBot.LOGGER.error(Markers.NO_ANNOUNCE, "Could not make POST request to hastebin!", e);
            return null;
        }
    }

    public static void editMessage(Message message, String content) {
        message.editMessage(content).queue();
    }

    public static Message sendFile(MessageChannel channel, String s, String fileContent, String filename) {
        ByteArrayInputStream stream = new ByteArrayInputStream(fileContent.getBytes());
        return channel.sendFile(stream, filename, new MessageBuilder().append(s).build()).complete();
    }

    public static EmbedBuilder getEmbed() {
        return new EmbedBuilder()
                .setAuthor("FlareBot", "https://github.com/FlareBot/FlareBot", FlareBot.getInstance().getClients()[0]
                        .getSelfUser().getEffectiveAvatarUrl());
    }

    public static String getTag(User user) {
        return user.getName() + '#' + user.getDiscriminator();
    }

    public static EmbedBuilder getEmbed(User user) {
        return getEmbed().setFooter("Requested by @" + getTag(user), user.getEffectiveAvatarUrl());
    }

    public static String getAvatar(User user) {
        return user.getEffectiveAvatarUrl();
    }

    public static String getDefaultAvatar(User user) {
        return user.getDefaultAvatarUrl();
    }

    public static Message sendErrorMessage(EmbedBuilder builder, MessageChannel channel) {
        return channel.sendMessage(builder.setColor(Color.red).build()).complete();
    }

    public static Message sendErrorMessage(String message, MessageChannel channel) {
        return channel.sendMessage(MessageUtils.getEmbed().setColor(Color.red).setDescription(message).build())
                .complete();
    }

    public static void editMessage(EmbedBuilder embed, Message message) {
        editMessage(message.getRawContent(), embed, message);
    }

    public static void editMessage(String s, EmbedBuilder embed, Message message) {
        if (message != null)
            message.editMessage(new MessageBuilder().append(s).setEmbed(embed.build()).build()).queue();
    }

    public static boolean hasInvite(Message message) {
        return INVITE_REGEX.matcher(message.getRawContent()).find();
    }

    public static boolean hasInvite(String message) {
        return INVITE_REGEX.matcher(message).find();
    }

    public static boolean hasLink(Message message) {
        return LINK_REGEX.matcher(message.getRawContent()).find();
    }

    public static boolean hasLink(String message) {
        return LINK_REGEX.matcher(message).find();
    }

    public static boolean hasYouTubeLink(Message message) {
        return YOUTUBE_LINK_REGEX.matcher(message.getRawContent()).find();
    }

    public static void sendAutoDeletedMessage(MessageEmbed messageEmbed, long delay, MessageChannel channel) {
        sendAutoDeletedMessage(new MessageBuilder().setEmbed(messageEmbed).build(), delay, channel);
    }

    public static void sendAutoDeletedMessage(Message message, long delay, MessageChannel channel) {
        Message msg = channel.sendMessage(message).complete();
        new FlarebotTask("AutoDeleteTask") {
            @Override
            public void run() {
                msg.delete().queue();
            }
        }.delay(delay);
    }

    public static RestAction<Message> getUsage(Command command, TextChannel channel, User user) {
        String title = capitalize(command.getCommand()) + " Usage";
        String usage = GeneralUtils.formatCommandPrefix(channel, command.getUsage());
        if(command.getPermission() != null)
            return channel.sendMessage(getEmbed(user).setTitle(title, null).addField("Usage", usage, false)
                    .addField("Permission", command.getPermission() + "\n" +
                            "**Default permission: **" + command.isDefaultPermission(), false).setColor(Color.red).build());
        else
            return channel.sendMessage(getEmbed(user).setTitle(title, null).addField("Usage", usage, false)
                    .setColor(Color.red).build());
    }

    private static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String makeAsciiTable(java.util.List<String> headers, java.util.List<java.util.List<String>> table, String footer) {
        return makeAsciiTable(headers, table, footer, "");
    }


    public static String makeAsciiTable(java.util.List<String> headers, java.util.List<java.util.List<String>> table, String footer, String lang) {
        StringBuilder sb = new StringBuilder();
        int padding = 1;
        int[] widths = new int[headers.size()];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = 0;
        }
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).length() > widths[i]) {
                widths[i] = headers.get(i).length();
            }
        }
        for (java.util.List<String> row : table) {
            for (int i = 0; i < row.size(); i++) {
                String cell = row.get(i);
                if (cell.length() > widths[i]) {
                    widths[i] = cell.length();
                }
            }
        }
        sb.append("```").append(lang).append("\n");
        String formatLine = "|";
        for (int width : widths) {
            formatLine += " %-" + width + "s |";
        }
        formatLine += "\n";
        sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
        sb.append(String.format(formatLine, headers.toArray()));
        sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
        for (java.util.List<String> row : table) {
            sb.append(String.format(formatLine, row.toArray()));
        }
        if (footer != null) {
            sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
            sb.append(getFooter(footer, padding, widths));
        }
        sb.append(appendSeparatorLine("+", "+", "+", padding, widths));
        sb.append("```");
        return sb.toString();
    }

    private static String appendSeparatorLine(String left, String middle, String right, int padding, int... sizes) {
        boolean first = true;
        StringBuilder ret = new StringBuilder();
        for (int size : sizes) {
            if (first) {
                first = false;
                ret.append(left).append(StringUtils.repeat("-", size + padding * 2));
            } else {
                ret.append(middle).append(StringUtils.repeat("-", size + padding * 2));
            }
        }
        return ret.append(right).append("\n").toString();
    }

    public static String getFooter(String footer, int padding, int... sizes) {
        StringBuilder sb = new StringBuilder();
        sb.append("|");
        int total = 0;
        for (int i = 0; i < sizes.length; i++) {
            int size = sizes[i];
            total += size + (i == sizes.length - 1 ? 0 : 1) + padding * 2;
        }
        sb.append(footer);
        sb.append(StringUtils.repeat(" ", total - footer.length()));
        sb.append("|\n");
        return sb.toString();
    }

    public static String getMessage(String[] args, int min) {
        return Arrays.stream(args).skip(min).collect(Collectors.joining(" ")).trim();
    }

    public static String getMessage(String[] args, int min, int max) {
        StringBuilder message = new StringBuilder();
        for (int index = min; index < max; index++) {
            message.append(args[index]).append(" ");
        }
        return message.toString().trim();
    }

    public static String escapeMarkdown(String s) {
        return ESCAPE_MARKDOWN.matcher(s).replaceAll("\\\\$0");
    }
}