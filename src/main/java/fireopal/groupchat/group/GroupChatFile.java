package fireopal.groupchat.group;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fireopal.groupchat.GroupChat;
import net.minecraft.entity.player.PlayerEntity;

public class GroupChatFile {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public String FILE_VERSION_DO_NOT_TOUCH_PLS = GroupChat.VERSION.toString();
    public Set<Group> groups = Sets.newHashSet();

    public Set<String> getGroupsForPlayer(NameAndUUID nameAndUUID) {
        Set<String> set = Sets.newHashSet();

        for (Group g : this.groups) {
            if (g.getPlayerListUuids().contains(nameAndUUID.uuid())) {
                set.add(g.getName());
            }
        }

        this.save();
        return set;
    }

    public Set<String> getGroupsForPlayer(PlayerEntity player) {
        return this.getGroupsForPlayer(NameAndUUID.of(player));
    }

    public void removePlayerFromGroup(String groupName, UUID player) {
        for (Group g : this.groups) {
            if (g.name.equals(groupName)) {
                g.removePlayer(player);
            }
        }

        this.save();
    }

    public void removePlayerFromGroup(String groupName, PlayerEntity player) {
        this.removePlayerFromGroup(groupName, player.getUuid());
    }

    public void addGroup(Group group) {
        groups.add(group);
        this.save();
        GroupChat.LOGGER.info("Created group " + group);
    }

    public void setGroup(Group group) {
        boolean bl = false;

        for (Group g : this.groups) {
            if ((g.getName()).equals(group.getName())) {
                g.setPlayerList(group.getPlayerList());
                bl = true;
                break;
            }
        }

        if (!bl) {
            this.addGroup(group);
        }

        this.save();
    }

    public void save() {
        this.removeEmptyGroups();

        try {
            Paths.get("", "config").toFile().mkdirs();

            Path groupChatFilePath = Paths.get("", "config", "groups.json");

            BufferedWriter writer = new BufferedWriter(
                new FileWriter(groupChatFilePath.toFile())
            );

            writer.write(gson.toJson(this));
            writer.close();

        } catch (IOException exception) {
            exception.printStackTrace();
        } 

        GroupChat.groupChatFile = this;

        GroupChat.LOGGER.info("Saved all groups to file");
    }

    private void removeEmptyGroups() {
        int i = 0;

        for (Group g : this.groups) {
            if (g.getPlayerList().size() == 0) {
                this.groups.remove(g);
            }

            i += 1;
        }

        GroupChat.LOGGER.info("Removed " + i + " empty groups");
    }

    public static GroupChatFile init() {
        GroupChat.LOGGER.info("Initializing Group Chat File");

        GroupChatFile groupChatFile = null;

        try {
            Path path = Paths.get("", "config", "groups.json");

            if (Files.exists(path)) {
                GroupChat.LOGGER.info("File exists; reading from file");

                groupChatFile = gson.fromJson(
                    new FileReader(path.toFile()),
                    GroupChatFile.class
                );

                if (!groupChatFile.FILE_VERSION_DO_NOT_TOUCH_PLS.equals(GroupChat.VERSION.toString())) {
                    GroupChat.LOGGER.info("Different file version! Rewriting");

                    groupChatFile.FILE_VERSION_DO_NOT_TOUCH_PLS = GroupChat.VERSION.toString();

                    BufferedWriter writer = new BufferedWriter(
                        new FileWriter(path.toFile())
                    );

                    writer.write(gson.toJson(groupChatFile));
                    writer.close();
                }

            } else {
                GroupChat.LOGGER.info("File didn't exist, creating blank file");

                groupChatFile = new GroupChatFile();
                Paths.get("", "config").toFile().mkdirs();

                BufferedWriter writer = new BufferedWriter(
                    new FileWriter(path.toFile())
                );

                writer.write(gson.toJson(groupChatFile));
                writer.close();
            }


        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return groupChatFile;
    }

}
