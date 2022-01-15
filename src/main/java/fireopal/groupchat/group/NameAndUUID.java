package fireopal.groupchat.group;

import java.util.UUID;

import net.minecraft.entity.Entity;

public class NameAndUUID {
    private String name;
    private UUID uuid;

    public NameAndUUID(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public NameAndUUID(Entity entity) {
        this(entity.getEntityName(), entity.getUuid());
    }

    public String name() {
        return this.name;
    }

    public UUID uuid() {
        return this.uuid;
    }

    public static NameAndUUID of(Entity entity) {
        return new NameAndUUID(entity);
    }
}
