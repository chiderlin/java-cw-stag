package edu.uob;

import edu.uob.commands.CustomActionCommand;
import edu.uob.commands.ErrorCommand;
import edu.uob.commands.GameCommand;
import edu.uob.commands.BuiltInCommand;
import edu.uob.entities.Player;

public class CmdHandler {
  private GameState state;

  public CmdHandler(GameState state) {
    this.state = state;
  }

  public GameCommand parse(String command) {
    try {
      String[] cmdStrings = command.split(":");
      String username = cmdStrings[0];
      String userCmdLowerCase = cmdStrings[1].trim().toLowerCase();
      if (!username.matches("[A-Za-z\\s'-]+")) {
        String errorMsg = String
            .format("[Error] Invalid player name. Only letters, spaces, apostrophes and hyphens are allowed.");
        System.err.printf(errorMsg);
        return new ErrorCommand(errorMsg);
      }
      Player player = this.state.getOrCreatePlayer(username);

      if (userCmdLowerCase.contains("and")) {
        String errorMsg = String
            .format("[Error] Invalid command, only execute one command at the time.");
        System.err.printf(errorMsg);
        return new ErrorCommand(errorMsg);
      }

      if (!this.isBasicCmd(userCmdLowerCase))
        return new CustomActionCommand(userCmdLowerCase, player, state);

      return new BuiltInCommand(userCmdLowerCase, player, state);

    } catch (Exception e) {
      e.printStackTrace();
      String errorMsg = String.format("[Error] CmdHandler thrown: %s", e.getMessage());
      System.err.printf(errorMsg);
      return new ErrorCommand(errorMsg);
    }

  }

  private boolean isBasicCmd(String cmd) {
    if (cmd.startsWith("inv") || cmd.startsWith("inventory") || cmd.startsWith("get") || cmd.startsWith("drop")
        || cmd.startsWith("goto")
        || cmd.startsWith("look") || cmd.startsWith("health"))
      return true;

    return false;
  }

}
