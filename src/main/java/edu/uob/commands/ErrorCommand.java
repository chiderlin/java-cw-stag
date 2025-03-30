package edu.uob.commands;

public class ErrorCommand extends GameCommand {
  public final String errorMessage;

  public ErrorCommand(String errorMessage) {
    super("", null);
    this.errorMessage = errorMessage;
  }

  @Override
  public String execute() {
    return errorMessage;
  }
}
