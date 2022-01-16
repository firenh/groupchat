package fireopal.groupchat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fireopal.groupchat.commands.GroupCommand;
import fireopal.groupchat.group.GroupChatFile;
import fireopal.groupchat.group.GroupChatFile.Config;
import fireopal.groupchat.util.FOModVersion;

public class GroupChat implements ModInitializer {
	public static final FOModVersion VERSION = FOModVersion.fromString("1.1.0");
	public static final FOModVersion FILE_VERSION = FOModVersion.fromString("1.1.0");
	public static final String MODID = "groupchat";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static GroupChatFile groupChatFile = GroupChatFile.init();

	@Override
	public void onInitialize() {
		GroupChatFile.init();

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            GroupCommand.register(dispatcher);
        });
	}

	public static Config getConfig() {
		return groupChatFile.config;
	}
}
