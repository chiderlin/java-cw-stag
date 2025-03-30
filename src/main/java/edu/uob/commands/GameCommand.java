package edu.uob.commands;

import edu.uob.entities.Player;

public abstract class GameCommand {
  protected String cmd;
  protected Player player;

  public GameCommand(String cmd, Player p) {
    this.cmd = cmd;
    this.player = p;
  }

  public abstract String execute();
}
