package edu.uob;

import edu.uob.entities.EntityParser.GameWorld;
import edu.uob.entities.EntityType;
import edu.uob.entities.GameEntity;
import edu.uob.entities.Location;
import edu.uob.entities.Player;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GameState {
  private final Map<String, Map<String, Map<String, String>>> action;
  private final Map<String, Location> gameMap;
  private final Location startLocation;
  private final Location storeroom;
  private Map<String, GameEntity> allEntitiesByName = new HashMap<>();
  private Map<String, EntityType> entityTypesByName = new HashMap<>();

  private Map<String, Player> allPlayers = new HashMap<>();

  public GameState(GameWorld gameWorld, Map<String, Map<String, Map<String, String>>> action) {
    this.action = action;
    this.gameMap = gameWorld.gameMap(); // can change location status here
    this.startLocation = gameWorld.startLocation();
    this.storeroom = gameWorld.storeroom(); // same obj with gameMap, can change status here directly
    this.allEntitiesByName = gameWorld.allEntitiesByName();
    this.entityTypesByName = gameWorld.entityTypesByName();
  }

  public Map<String, Location> getGameMap() {
    return this.gameMap;
  }

  public Location getLocation(String name) {
    return this.gameMap.get(name.toLowerCase());
  }

  public GameEntity getEntity(String name) {
    return this.allEntitiesByName.get(name.toLowerCase());
  }

  public EntityType getEntityType(String name) {
    return this.entityTypesByName.get(name.toLowerCase());
  }

  public Set<String> getAllEntityNames() {
    return this.allEntitiesByName.keySet();
  }

  public Location getStartLocation() {
    return this.startLocation;
  }

  public Location getStoreroom() {
    return this.storeroom;
  }

  public Map<String, Map<String, Map<String, String>>> getActions() {
    return this.action;
  }

  public Player getPlayer(String username) {
    return allPlayers.get(username);
  }

  public Player getOrCreatePlayer(String username) {
    if (!allPlayers.containsKey(username)) {
      Player newPlayer = new Player(username);
      newPlayer.setCurrentLocation(this.startLocation);
      allPlayers.put(username, newPlayer);
      return newPlayer;
    }
    return allPlayers.get(username);
  }

  public Set<String> getPlayersInLocation(String locationName) {
    Set<String> playerInLocation = new HashSet<>();
    for (Map.Entry<String, Player> playerEntry : allPlayers.entrySet()) {
      Player p = playerEntry.getValue();

      if (p.getCurrentLocation() != null && p.getCurrentLocation().getName().equals(locationName)) {
        playerInLocation.add(playerEntry.getKey());
      }
    }
    return playerInLocation;
  }
}
