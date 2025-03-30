
package edu.uob.entities;

public class GameEntity {
    private final String name;
    private final String description;
    private final EntityType type;

    public GameEntity(String name, String description, EntityType type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EntityType getType() {
        return type;
    }
}