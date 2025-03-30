package edu.uob.commands;

import edu.uob.GameState;

import java.util.Map;
import java.util.Set;

import java.util.HashMap;
import edu.uob.entities.Player;
import edu.uob.entities.GameEntity;
import edu.uob.entities.Location;

public class BuiltInCommand extends GameCommand {

  private final Map<String, Command> commandMap = new HashMap<>();
  private String currentArg = null;
  private final GameState state;
  private final Map<String, Location> gameMap;

  public BuiltInCommand(String cmd, Player p, GameState state) {
    super(cmd, p);
    this.state = state;
    this.gameMap = this.state.getGameMap();
    initCommands();
  }

  private void initCommands() {
    commandMap.put("look", new LookCommand());
    commandMap.put("get", new GetCommand());
    commandMap.put("inv", new InvCommand());
    commandMap.put("inventory", new InvCommand());
    commandMap.put("drop", new DropCommand());
    commandMap.put("goto", new GotoCommand());
    commandMap.put("health", new HealthCommand());
  }

  @Override
  public String execute() {
    try {
      String[] tokens = this.cmd.trim().split("\\s+");
      if (tokens.length == 0) {
        return "[Error] Empty command.";
      }

      String action = tokens[0];
      Command cmd = commandMap.get(action);
      if (cmd == null)
        return "[Error] Unknown built-in command.";

      if (action.equals("get") || action.equals("drop") || action.equals("goto")) {
        if (tokens.length != 2) {
          String errMsg = String.format("[Error] %s command requires exactly one argument.", action);
          System.out.printf(errMsg);
          return errMsg;
        }
        this.currentArg = tokens[1].trim();
      }

      if ((action.equals("inv") || action.equals("inventory") || action.equals("look") || action.equals("health"))
          && tokens.length != 1) {
        String errMsg = String.format("[Error] %s command should not have extra arguments.", action);
        System.out.printf(errMsg);
        return errMsg;
      }

      return cmd.run();
    } catch (Exception e) {
      e.printStackTrace();
      String errorMsg = String.format("[Error] BuiltInCommand thrown: %s", e.getMessage());
      System.err.printf(errorMsg);
      return errorMsg;
    }
  };

  private interface Command {
    String run();
  }

  private class HealthCommand implements Command {
    public String run() {
      Player player = BuiltInCommand.this.player;
      int hp = player.getHealth();
      String res = String.format("Your current health: %s.", hp);
      return res;
    }
  }

  private class LookCommand implements Command {
    public String run() {
      Player player = BuiltInCommand.this.player;
      Location location = player.getCurrentLocation();
      String locationName = location.getName();

      Map<String, Location> gameMap = BuiltInCommand.this.gameMap;
      Map<String, GameEntity> artefacts = gameMap.get(locationName).getArtefacts();
      Map<String, GameEntity> furniture = gameMap.get(locationName).getFurniture();
      Map<String, GameEntity> characters = gameMap.get(locationName).getCharacters();

      Set<String> paths = gameMap.get(locationName).getConnectedLocations();
      String res = generateResponceText(location, artefacts, furniture, characters, paths);
      return res;
    }
  }

  private class GotoCommand implements Command {
    String errorMsg;

    public String run() {
      Player currentPlayer = BuiltInCommand.this.player;
      String toLocationName = BuiltInCommand.this.currentArg;
      Map<String, Location> gameMap = BuiltInCommand.this.gameMap;
      Location toLocation = gameMap.get(toLocationName);
      if (toLocation == null) {
        errorMsg = String.format("[Error] couldn't find this location '%s'.", toLocationName);
        System.err.println(errorMsg);
        return errorMsg;
      }

      Location playerCurrentLoca = currentPlayer.getCurrentLocation();

      // current location
      if (playerCurrentLoca.getName().equals(toLocationName)) {
        errorMsg = String.format("[Error] You are currently located: '%s'.", playerCurrentLoca.getName());
        System.err.println(errorMsg);
        return errorMsg;
      }

      // check path access
      if (!playerCurrentLoca.getConnectedLocations().contains(toLocationName)) {
        errorMsg = String.format("[Error] don't have access to this location '%s'.", toLocationName);
        System.err.println(errorMsg);
        return errorMsg;
      }

      Map<String, GameEntity> artefacts = gameMap.get(toLocationName).getArtefacts();
      Map<String, GameEntity> furniture = gameMap.get(toLocationName).getFurniture();
      Map<String, GameEntity> characters = gameMap.get(toLocationName).getCharacters();

      Set<String> paths = gameMap.get(toLocationName).getConnectedLocations();

      // set player currentlocation
      currentPlayer.setCurrentLocation(toLocation);

      String res = generateResponceText(toLocation, artefacts, furniture, characters, paths);
      return res;
    }
  }

  private class GetCommand implements Command {
    String errorMsg;

    public String run() {
      try {
        Player currentPlayer = BuiltInCommand.this.player;
        String artifactName = BuiltInCommand.this.currentArg;
        Location currentLoca = currentPlayer.getCurrentLocation();
        // check player current location have this artifact
        if (!currentLoca.getArtefacts().keySet().contains(artifactName)) {
          errorMsg = String.format("[Error] don't have '%s' this artifact.", artifactName);
          System.err.println(errorMsg);
          return errorMsg;
        }

        GameEntity item = currentLoca.getArtefacts().get(artifactName);

        // put it into payer inv
        currentPlayer.addToInventory(item);

        // remove location artifacts
        currentLoca.removeArtefacts(artifactName);
        String res = String.format("You picked up a %s", artifactName);
        return res;
      } catch (Exception e) {
        String errorMeg = String.format("[Error] Get Command error: ", e.getMessage());
        System.err.printf(errorMeg);
        return errorMeg;

      }

    }
  }

  private class InvCommand implements Command {
    public String run() {
      Player currentPlayer = BuiltInCommand.this.player;
      Set<GameEntity> inventory = currentPlayer.getInventory();
      StringBuilder res = new StringBuilder();

      for (GameEntity item : inventory) {
        res.append(item.getName()).append("\n");
      }
      return res.toString();
    }
  }

  private class DropCommand implements Command {
    String errorMsg;

    public String run() {
      Player currentPlayer = BuiltInCommand.this.player;
      String artifactName = BuiltInCommand.this.currentArg;
      Location currLoca = currentPlayer.getCurrentLocation();
      Set<GameEntity> invs = currentPlayer.getInventory();
      GameEntity dropItem = null;
      boolean found = false;
      for (GameEntity item : invs) {
        if (item.getName().equalsIgnoreCase(artifactName)) {
          found = true;
          dropItem = item;
          break;
        }
      }
      if (!found) {
        errorMsg = String.format("[Error] This item %s doesn't exist in your inventory.", artifactName);
        System.err.printf(errorMsg);
        return errorMsg;
      }

      // drop item in current location
      currentPlayer.removeFromInventory(dropItem);
      currLoca.addArtefacts(dropItem);
      String res = String.format("You drop item: '%s' successfully.", dropItem.getName());
      return res;
    }
  }

  /*
   * String res = "You are in {locationDesc}. You can see:
   * <artefacts>
   * <furniture>
   * <characters>
   * You can access from here:
   * <path>
   * 
   * Players here:
   * 1
   * 2
   * 3
   * 
   */
  public String generateResponceText(Location location, Map<String, GameEntity> artefacts,
      Map<String, GameEntity> furniture, Map<String, GameEntity> characters,
      Set<String> paths) {

    String locaDesc = location.getDescription();
    Set<String> playersHere = this.state.getPlayersInLocation(location.getName());

    StringBuilder res = new StringBuilder();
    res.append("You are in ").append(locaDesc).append(". You can see:\n");
    for (Map.Entry<String, GameEntity> entry : artefacts.entrySet()) {
      res.append(entry.getValue().getDescription()).append("\n");
    }

    for (Map.Entry<String, GameEntity> entry : furniture.entrySet()) {
      res.append(entry.getValue().getDescription()).append("\n");
    }

    for (Map.Entry<String, GameEntity> entry : characters.entrySet()) {
      res.append(entry.getValue().getDescription()).append("\n");
    }

    res.append("You can access from here:\n");
    for (String to : paths) {
      res.append(to).append("\n");
    }

    res.append("Players here:\n");
    for (String player : playersHere) {
      res.append(player).append("\n");
    }

    System.out.println(res);

    return res.toString();
  }
}
