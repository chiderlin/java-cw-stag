
package edu.uob.commands;

import edu.uob.GameState;
import edu.uob.entities.GameEntity;
import edu.uob.entities.Player;
import edu.uob.entities.Location;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class CustomActionCommand extends GameCommand {
  private final GameState state;

  public CustomActionCommand(String cmd, Player p, GameState state) {
    super(cmd, p);
    this.state = state;
  }

  public String execute() {
    try {
      Set<String> inputWords = new HashSet<>();
      String commandCopy = this.cmd.trim().toLowerCase();
      int start = 0;

      for (int i = 0; i < commandCopy.length(); i++) {
        if (Character.isWhitespace(commandCopy.charAt(i))) {
          if (start < i) {
            inputWords.add(commandCopy.substring(start, i));
          }
          start = i + 1;
        }
      }
      if (start < commandCopy.length()) {
        inputWords.add(commandCopy.substring(start));
      }

      Set<Map<String, Map<String, String>>> matchedActions = new LinkedHashSet<>();
      Map<String, Map<String, Map<String, String>>> actionsMapping = this.state.getActions();

      for (Map<String, Map<String, String>> action : actionsMapping.values()) {

        // validate input
        if (!validateInputWords(action, inputWords))
          continue;

        // validate subjects
        boolean isAllSubjectsAccessible = this.allSubjectsAccessible(action);
        if (!isAllSubjectsAccessible)
          continue;

        matchedActions.add(action);
      }

      if (matchedActions.size() == 0)
        return "[Error] No matching action found.";
      if (matchedActions.size() > 1)
        return "[Error] Ambiguous command. Please be more specific.";

      // only the first cmd valid -> get first action work.
      Map<String, Map<String, String>> matchedAction = matchedActions.iterator().next();
      return this.applyAction(matchedAction);

    } catch (Exception e) {
      e.printStackTrace();
      String errMsg = String.format("[Error] in execute: %s", e.getMessage());
      System.out.printf(errMsg);
      return errMsg;
    }
  }

  private boolean validateInputWords(Map<String, Map<String, String>> action, Set<String> inputWords) {
    // action match at least one trigger, action match at least one subject for
    // input command
    Map<String, String> triggerMap = action.get("triggers");
    Map<String, String> subjectMap = action.get("subjects");

    int findTriggers = 0;
    int findSubjects = 0;
    if (triggerMap == null)
      return false;

    for (String trigger : triggerMap.values()) {
      if (inputWords.contains(trigger))
        findTriggers += 1;
    }

    for (String subject : subjectMap.values()) {
      if (inputWords.contains(subject))
        findSubjects += 1;
    }

    if (findTriggers >= 1 && findSubjects >= 1)
      return true;

    return false;
  }

  private boolean allSubjectsAccessible(Map<String, Map<String, String>> action) {
    // action's subject should all be in current location. or inv
    Map<String, String> subjectMap = action.get("subjects");
    for (String subject : subjectMap.values()) {
      Location currLoca = this.player.getCurrentLocation();
      Set<GameEntity> invs = this.player.getInventory();
      if (!this.isInInventory(invs, subject) && !currLoca.containsEntity(subject)) {
        return false;
      }
    }
    return true;
  }

  private String applyAction(Map<String, Map<String, String>> action) {
    try {
      Map<String, String> consumed = action.get("consumed");
      if (consumed != null) {
        for (Map.Entry<String, String> entry : consumed.entrySet()) {
          String name = entry.getValue();
          String error = this.consume(name);
          if (error != null) {
            return error;
          }
        }
      }

      Map<String, String> produced = action.get("produced");
      if (produced != null) {
        for (Map.Entry<String, String> entry : produced.entrySet()) {
          String name = entry.getValue();
          this.produce(name);
        }
      }

      Map<String, String> narration = action.get("narration");
      if (narration != null) {
        return narration.get("1");
      }

      return "[Info] Action Completed.";
    } catch (Exception e) {
      e.printStackTrace();
      String errMsg = String.format("[Error] applyAction error: %s", e.getMessage());
      System.out.printf(errMsg);
      return errMsg;
    }
  }

  private String consume(String name) {
    String errMsg;
    if (name.equalsIgnoreCase("health")) {
      int hp = this.player.getHealth();
      this.player.setHealth(hp - 1);

      // if health <= 0. drop all items at current location
      if (this.player.getHealth() <= 0) {
        for (GameEntity item : this.player.getInventory()) {
          this.player.getCurrentLocation().addEntity(item);
        }
        // clear current inventory
        this.player.getInventory().clear();

        // player restart game to start location
        this.player.setCurrentLocation(this.state.getStartLocation());

        // reset health to 3
        this.player.setHealth(3);

        return "You died and lost all of your items, you must return to the start of the game.";
      }
      return null; // no error
    }

    GameEntity item = this.state.getEntity(name);
    errMsg = String.format("[Error] Cannot find entity: %s", name);
    if (item == null) {
      System.out.printf(errMsg);
      return errMsg;
    }

    Set<GameEntity> invObj = this.player.getInventory();
    if (isInInventory(invObj, name)) {
      this.player.removeFromInventory(item);
    } else if (player.getCurrentLocation().containsEntity(name)) {
      this.player.getCurrentLocation().removeEntity(item);
    } else {
      errMsg = String.format("[Error] %s not in inventory or current location", name);
      System.out.printf(errMsg);
      return errMsg;
    }

    this.state.getStoreroom().addEntity(item);
    return null; // no error
  }

  private boolean isInInventory(Set<GameEntity> invObj, String name) {
    for (GameEntity obj : invObj) {
      if (obj.getName().equalsIgnoreCase(name)) {
        return true;
      }
    }
    return false;
  }

  private void produce(String name) {
    try {
      if (name.equalsIgnoreCase("health")) {
        if (this.player.getHealth() < 3) {
          this.player.setHealth(this.player.getHealth() + 1);
        }
        return;
      }

      GameEntity item = this.state.getEntity(name);
      if (item != null) {
        boolean isInStoreRoom = this.state.getStoreroom().containsEntity(name);
        if (isInStoreRoom) {
          this.state.getStoreroom().removeEntity(item);
        } else {
          // look for all locations
          Map<String, Location> gameMap = this.state.getGameMap();
          for (Map.Entry<String, Location> entry : gameMap.entrySet()) {
            Location getEachLocation = entry.getValue();
            if (getEachLocation.containsEntity(name)) {
              getEachLocation.removeEntity(item);
            }
          }
        }

        this.player.getCurrentLocation().addEntity(item);
        return;
      }

      Location produceIsLocation = this.state.getLocation(name);
      if (produceIsLocation instanceof Location) {
        this.player.getCurrentLocation().addPathTo(name);
      }

    } catch (Exception e) {
      e.printStackTrace();
      String errMsg = String.format("[Error] produce error: %s", e.getMessage());
      System.out.printf(errMsg);
      return;
    }
  }

}
