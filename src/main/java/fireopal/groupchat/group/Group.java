package fireopal.groupchat.group;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.entity.player.PlayerEntity;

public class Group {
    final String name;
    String password = "";
    private Set<NameAndUUID> playerList = Sets.newHashSet();
    
    public Group(String name) {
        this.name = name;
    }

    public Group(String name, Set<NameAndUUID> playerList) {
        this.name = name;
        
        for (NameAndUUID p : playerList) {
            this.playerList.add(p);
        }
    }

    public Group(String name, Set<NameAndUUID> playerList, String password) {
        this(name, playerList);
        this.password = password;
    }

    public boolean hasPassword() {
        return !this.password.equals("");
    }

    public String getPassword() {
        return this.password;
    }

    public boolean passwordMatches(String inputPassword) {
        if (this.password.equals("")) return true;
        return this.password.equals(inputPassword);
    }

    public String getName() {
        return this.name;
    }

    public Set<NameAndUUID> getPlayerList() {
        return this.playerList;
    }

    public Set<UUID> getPlayerListUuids() {
        Set<UUID> set = Sets.newHashSet();
        
        for (NameAndUUID p : playerList) {
            set.add(p.uuid());
        }

        return set;
    }

    public Set<String> getPlayerListNames() {
        Set<String> set = Sets.newHashSet();
        
        for (NameAndUUID p : playerList) {
            set.add(p.name());
        }

        return set;
    }

    public void addPlayer(NameAndUUID player) {
        this.playerList.add(player);
    }

    public void addPlayer(PlayerEntity player) {
        this.addPlayer(NameAndUUID.of(player));
    }
    
    public void removePlayer(UUID uuid) {
        for (NameAndUUID n : this.playerList) {
            if (n.uuid().equals(uuid)) {
                this.playerList.remove(n);
            }
        }
    }

    public void setPlayerList(Set<NameAndUUID> players) {
        this.playerList = players;
    }
}
