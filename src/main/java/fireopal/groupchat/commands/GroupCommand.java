package fireopal.groupchat.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import fireopal.groupchat.GroupChat;
import fireopal.groupchat.group.Group;
import fireopal.groupchat.group.NameAndUUID;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GroupCommand {
    private GroupCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder<ServerCommandSource>)((LiteralArgumentBuilder<ServerCommandSource>)
            CommandManager.literal("g").requires(serverCommandSource -> true))
                .then(CommandManager.argument("Group", StringArgumentType.string())
                .suggests(GroupCommand::suggestGroups)
                    .then(CommandManager.argument("message", MessageArgumentType.message())
                        .executes(GroupCommand::message)
                    )
                )
            );
        dispatcher.register((LiteralArgumentBuilder<ServerCommandSource>)((LiteralArgumentBuilder<ServerCommandSource>)
            CommandManager.literal("group").requires(serverCommandSource -> true))
                .then(CommandManager.literal("message")
                    .then(CommandManager.argument("Group", StringArgumentType.string())
                    .suggests(GroupCommand::suggestGroups)
                        .then(CommandManager.argument("message", MessageArgumentType.message())
                            .executes(GroupCommand::message)
                        )
                    )
                )
                .then(CommandManager.literal("create")
                    .then(CommandManager.argument("Group Name", StringArgumentType.string())
                        .executes((context) -> create(context))
                        .then(CommandManager.argument("Password", StringArgumentType.string())
                            .executes((context) -> create(context))
                        )
                    )
                )
                .then(CommandManager.literal("players")
                    .then(CommandManager.argument("Group", StringArgumentType.string())
                    .suggests(GroupCommand::suggestGroups)
                        .executes(GroupCommand::players)
                    )
                )
                .then(CommandManager.literal("join")
                    .then(CommandManager.argument("Group", StringArgumentType.string())
                        .executes(GroupCommand::join)
                        .then(CommandManager.argument("Password", StringArgumentType.string())
                            .executes(GroupCommand::join)
                        )
                    )
                )
                .then(CommandManager.literal("leave")
                    .then(CommandManager.argument("Group", StringArgumentType.string())
                    .suggests(GroupCommand::suggestGroups)
                        .executes(GroupCommand::leave)
                    )
                )
                .then(CommandManager.literal("invite")
                    .then(CommandManager.argument("Group", StringArgumentType.string())
                    .suggests(GroupCommand::suggestGroups)
                        .then(CommandManager.argument("Players", EntityArgumentType.players())
                            .executes(GroupCommand::invite)
                        )
                    )
                )
            );
    }

    private static CompletableFuture<Suggestions> suggestGroups(CommandContext<ServerCommandSource> context, SuggestionsBuilder suggestionsBuilder)
            throws CommandSyntaxException {
        for (String s : GroupChat.groupChatFile.getGroupsForPlayer(context.getSource().getPlayer())) {
            suggestionsBuilder.suggest(s);
        }

        return suggestionsBuilder.buildFuture();
    }
    
    private static int message(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final String groupName = StringArgumentType.getString(context, "Group");
        Group group = null;
        ServerCommandSource source = context.getSource();

        for (Group g : GroupChat.groupChatFile.groups) {
            if (g.getName().equals(groupName)) {
                group = g;
            }
        }

        if (group == null) {
            source.sendError(new LiteralText("Group \"" + groupName + "\" does not exist!"));
            return 0;
        }

        UUID sender = null;

        try {
            sender = source.getPlayer().getUuid();
        } catch (CommandSyntaxException e) {
        }

        if (sender != null) {
            if (!group.getPlayerListUuids().contains(sender)) {
                source.sendError(new LiteralText(
                    "You are not in Group \"" + groupName + "\""
                ));
                return 0;
            }
        }
        MutableText groupMessage = new LiteralText("(" + groupName + ")").styled(style -> style
            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/g " + groupName + " "))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(
                groupName + "\n" +
                "Click to suggest command"
            )))
        );

        Text message = groupMessage.append(new LiteralText(
            " " + source.getName() + ": " + MessageArgumentType.getMessage(context, "message").asString()
        ));

        List<ServerPlayerEntity> players = source.getServer().getPlayerManager().getPlayerList();
        int i = 0;

        for (ServerPlayerEntity p : players) {
            if (group.getPlayerListUuids().contains(p.getUuid())) {
                p.sendMessage(message, MessageType.CHAT, sender);
                i += 1;
            }
        }

        return i;
    }

    private static int create(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String newGroup = StringArgumentType.getString(context, "Group Name");
        boolean bl = true;
        ServerPlayerEntity player = null;
        Set<NameAndUUID> players = new HashSet<NameAndUUID>();
        String password = "";

        try {
            password = StringArgumentType.getString(context, "Password");
        } catch (IllegalArgumentException e) {
        }

        for (Group g : GroupChat.groupChatFile.groups) {
            if (g.getName().equals(newGroup)) bl = false;
        }

        if (!bl) {
            context.getSource().sendError(new LiteralText(
                "Group \"" + newGroup + "\" already exists!"
            ));
                
            return 0;
        }

        try {
            player = context.getSource().getPlayer();
        } catch (CommandSyntaxException e) {}

        if (player != null) {
            players.add(new NameAndUUID(player));
        }

        GroupChat.groupChatFile.addGroup(
            new Group(newGroup, players, password)  
        );

        context.getSource().sendFeedback(new LiteralText(
            "Group \"" + newGroup + "\" created!"
        ), false);

        return 1;
    }

    private static int players(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final String groupName = StringArgumentType.getString(context, "Group");
        Group group = null;
        ServerCommandSource source = context.getSource();

        for (Group g : GroupChat.groupChatFile.groups) {
            if (g.getName().equals(groupName)) {
                group = g;
            }
        }

        if (group == null) {
            source.sendError(new LiteralText("Group \"" + groupName + "\" does not exist!"));
            return 0;
        }

        UUID sender = null;

        try {
            sender = source.getPlayer().getUuid();
        } catch (CommandSyntaxException e) {
        }

        if (sender != null) {
            if (!group.getPlayerListUuids().contains(sender)) {
                source.sendError(new LiteralText(
                    "You are not in Group \"" + groupName + "\""
                ));
                return 0;
            }
        }

        String playerListAsString = "";
        int i = 0;

        for (String s : group.getPlayerListNames()) {
            playerListAsString += ", " + s;
            i += 1;
        }

        playerListAsString = playerListAsString.substring(2);

        int playerCount = group.getPlayerList().size();
        Text feedback = new LiteralText(
            "Group \"" + groupName + "\" has " + playerCount + " player" + (playerCount == 1 ? "" : "s") + "\n" +
            "Players: " + playerListAsString + "\n" + 
            "Password: " + group.getPassword()
        );

        source.sendFeedback(feedback, false);
        return i;
    }

    private static int join(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final String groupName = StringArgumentType.getString(context, "Group");
        Group group = null;
        ServerCommandSource source = context.getSource();

        for (Group g : GroupChat.groupChatFile.groups) {
            if (g.getName().equals(groupName)) {
                group = g;
            }
        }

        if (group == null) {
            source.sendError(new LiteralText("Group \"" + groupName + "\" does not exist!"));
            return 0;
        }

        UUID sender = null;

        try {
            sender = source.getPlayer().getUuid();
        } catch (CommandSyntaxException e) {
        }

        if (sender != null) {
            if (group.getPlayerListUuids().contains(sender)) {
                source.sendError(new LiteralText(
                    "You are already in Group \"" + groupName + "\""
                ));
                return 0;
            }
        }

        ServerPlayerEntity player = null;
        String password = "";

        try {
            player = source.getPlayer();
        } catch (CommandSyntaxException e) {
            source.sendError(new LiteralText("Player does not exist!"));
            return 0;
        } 

        try {
            password = StringArgumentType.getString(context, "Password");
        } catch (IllegalArgumentException e) {}

        if (group.hasPassword() && !group.passwordMatches(password)) {
            source.sendError(new LiteralText("That password does not match!"));
            return 0;
        }
        
        group.addPlayer(player);
        source.sendFeedback(new LiteralText(
            "Joined group \"" + groupName + "\" successfully!" 
        ), false);

        return 1;
    }

    private static int leave(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final String groupName = StringArgumentType.getString(context, "Group");
        Group group = null;
        ServerCommandSource source = context.getSource();

        for (Group g : GroupChat.groupChatFile.groups) {
            if (g.getName().equals(groupName)) {
                group = g;
            }
        }

        if (group == null) {
            source.sendError(new LiteralText("Group \"" + groupName + "\" does not exist!"));
            return 0;
        }

        UUID sender = null;

        try {
            sender = source.getPlayer().getUuid();
        } catch (CommandSyntaxException e) {
        }

        if (sender != null) {
            if (!group.getPlayerListUuids().contains(sender)) {
                source.sendError(new LiteralText(
                    "You are not in Group \"" + groupName + "\""
                ));
                return 0;
            }
        }

        ServerPlayerEntity player = null;

        try {
            player = source.getPlayer();
        } catch (CommandSyntaxException e) {
            source.sendError(new LiteralText("Player does not exist!"));
            return 0;
        } 

        GroupChat.groupChatFile.removePlayerFromGroup(groupName, player);
        source.sendFeedback(new LiteralText(
            "Left Group \"" + groupName + "\""
        ), false);

        return 1;
    }

    public static int invite(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final String groupName = StringArgumentType.getString(context, "Group");
        Group group = null;
        ServerCommandSource source = context.getSource();

        for (Group g : GroupChat.groupChatFile.groups) {
            if (g.getName().equals(groupName)) {
                group = g;
            }
        }

        if (group == null) {
            source.sendError(new LiteralText("Group \"" + groupName + "\" does not exist!"));
            return 0;
        }

        UUID sender = null;

        try {
            sender = source.getPlayer().getUuid();
        } catch (CommandSyntaxException e) {
        }

        if (sender != null) {
            if (!group.getPlayerListUuids().contains(sender)) {
                source.sendError(new LiteralText(
                    "You are not in Group \"" + groupName + "\""
                ));
                return 0;
            }
        }

        Collection<ServerPlayerEntity> playerEntities = EntityArgumentType.getPlayers(context, "Players");
        
        final Group group2 = group;

        MutableText groupText = new LiteralText(groupName).styled(style -> style
            .withColor(Formatting.GREEN)
            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/group join " + groupName + " " + group2.getPassword()))
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(
                "Group: " + groupName + "\n" + 
                "Password: " + group2.getPassword() + "\n" +
                "Click to join Group"
            )))
        );

        boolean bl = false;
        Text message = new LiteralText(source.getPlayer().getEntityName() + " has invited you to join Group ").append(groupText).append("!");

        for (ServerPlayerEntity p : playerEntities) {
            if (!group.getPlayerListUuids().contains(p.getUuid())) {
                p.sendMessage(message, MessageType.CHAT, sender);
                bl = true;
            }
        }

        if (bl) {
            source.sendFeedback(new LiteralText(
                "Invited players to group \"" + groupName + "\""
            ), false);
            return 1;
        } else {
            source.sendError(new LiteralText("Players already in group!"));
            return 0;
        }
    }
}
