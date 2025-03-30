
package edu.uob.entities;

import java.util.*;

public class Location extends GameEntity {
  private final Set<String> connectedLocations = new HashSet<>();
  private final Map<String, GameEntity> artefacts = new HashMap<>();
  private final Map<String, GameEntity> furniture = new HashMap<>();
  private final Map<String, GameEntity> characters = new HashMap<>();

  public Location(String name, String description) {
    super(name, description, EntityType.LOCATION);
  }

  public Set<String> getConnectedLocations() {
    return connectedLocations;
  }

  public void addPathTo(String locationName) {
    connectedLocations.add(locationName);
  }

  public void addEntity(GameEntity entity) {
    switch (entity.getType()) {
      case ARTEFACT:
        artefacts.put(entity.getName(), entity);
        break;
      case FURNITURE:
        furniture.put(entity.getName(), entity);
        break;
      case CHARACTER:
        characters.put(entity.getName(), entity);
        break;
      default:
        break;
    }
  }

  public void removeEntity(GameEntity entity) {
    switch (entity.getType()) {
      case ARTEFACT:
        artefacts.remove(entity.getName());
        break;
      case FURNITURE:
        furniture.remove(entity.getName());
        break;
      case CHARACTER:
        characters.remove(entity.getName());
        break;
      default:
        break;
    }
  }

  public boolean containsEntity(String name) {
    return artefacts.containsKey(name) || furniture.containsKey(name) || characters.containsKey(name);
  }

  public boolean containsArtefact(String name) {
    return artefacts.containsKey(name);
  }

  public boolean containsFurniture(String name) {
    return furniture.containsKey(name);
  }

  public boolean containsCharacter(String name) {
    return characters.containsKey(name);
  }

  public GameEntity getEntityByName(String name) {
    if (artefacts.containsKey(name))
      return artefacts.get(name);
    if (furniture.containsKey(name))
      return furniture.get(name);
    if (characters.containsKey(name))
      return characters.get(name);
    return null;
  }

  public Map<String, GameEntity> getArtefacts() {
    return artefacts;
  }

  public void removeArtefacts(String name) {
    this.artefacts.remove(name);
  }

  public void addArtefacts(GameEntity artefact) {
    this.artefacts.put(artefact.getName(), artefact);
  }

  public Map<String, GameEntity> getFurniture() {
    return furniture;
  }

  public Map<String, GameEntity> getCharacters() {
    return characters;
  }
}