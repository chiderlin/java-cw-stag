package edu.uob.entities;

import java.util.HashSet;
import java.util.Set;

public class Player extends GameEntity {
  private int health = 3;
  private Location currentLocation;
  private final Set<GameEntity> inventory = new HashSet<>();

  public Player(String name) {
    super(name, "A brave adventurer.", EntityType.PLAYER);
  }

  public int getHealth() {
    return this.health;
  }

  public void setHealth(int newHP) {
    this.health = newHP;
  }

  public Location getCurrentLocation() {
    return this.currentLocation;
  }

  public void setCurrentLocation(Location location) {
    this.currentLocation = location;
  }

  public void addToInventory(GameEntity item) {
    this.inventory.add(item);
  }

  public void removeFromInventory(GameEntity item) {
    this.inventory.remove(item);
  }

  public Set<GameEntity> getInventory() {
    return this.inventory;
  }

  @Override
  public EntityType getType() {
    return EntityType.PLAYER;
  }
}
