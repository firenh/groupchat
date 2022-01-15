# GroupChat

## by FireNH

A mod to add server-side group chats to Minecraft to allow for private messaging.

This mod adds a new command: /group. The /group command allows players to manage private group chats through the in-game chat. A group can be created using `/group create <groupname>` with an option to also add a password on the end. Other players can join this group using `/group join <groupname> [password]` (or the password can be omitted if the group doesn't have a password). Players can use `/group message <groupname> <message>` to message all of the online players in that group. In addition, players can also use an alias command `/g <groupname> <message>` for short. Players can see the members in that group and the password with `/group players <groupname>`. Players can also quickly invite others to a group using `/group invite <groupname> <players>` which sends a chat message with an autocomplete to join the group, along with the password if it is a password locked group. Finally, players can leave a group with `/group leave <groupname>`. If there is ever a point where there are no players in a group, that group is deleted, and another new group can be created with that same name.

Groups, along with their player lists and passwords, are stored in `./config/groups.json`. If you ever need to manually edit a group, you can do so there.

This mod is server-side, meaning it can be installed on a server without requiring clients to also install it. However, clients can use it in Singleplayer worlds as well, even if there's really no point.

Developed by [FireNH on Github](https://github.com/firenh) 

[(Sources)](https://github.com/firenh/groupchat)