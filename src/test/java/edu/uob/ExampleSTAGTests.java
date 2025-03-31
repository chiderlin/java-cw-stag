package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.time.Duration;

class ExampleSTAGTests {

  private GameServer server;

  // Create a new server _before_ every @Test
  @BeforeEach
  void setup() {
    File entitiesFile = new File("config", "extended-entities.dot").getAbsoluteFile();
    File actionsFile = new File("config", "extended-actions.xml").getAbsoluteFile();
    server = new GameServer(entitiesFile, actionsFile);
  }

  String sendCommandToServer(String command) {
    // Try to send a command to the server - this call will timeout if it takes too
    // long (in case the server enters an infinite loop)
    return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
      return server.handleCommand(command);
    },
        "Server took too long to respond (probably stuck in an infinite loop)");
  }

  // A lot of tests will probably check the game state using 'look' - so we better
  // make sure 'look' works well !
  @Test
  void testLook() {
    String response = sendCommandToServer("simon: look");
    response = response.toLowerCase();
    assertTrue(response.contains("cabin"), "Did not see the name of the current room in response to look");
    assertTrue(response.contains("log cabin"), "Did not see a description of the room in response to look");
    assertTrue(response.contains("magic potion"), "Did not see a description of artifacts in response to look");
    assertTrue(response.contains("wooden trapdoor"), "Did not see description of furniture in response to look");
    assertTrue(response.contains("forest"), "Did not see available paths in response to look");
  }

  // Test that we can pick something up and that it appears in our inventory
  @Test
  void testGet() {
    String response;
    sendCommandToServer("simon: get potion");
    response = sendCommandToServer("simon: inv");
    response = response.toLowerCase();
    assertTrue(response.contains("potion"),
        "Did not see the potion in the inventory after an attempt was made to get it");
    response = sendCommandToServer("simon: look");
    response = response.toLowerCase();
    assertFalse(response.contains("potion"), "Potion is still present in the room after an attempt was made to get it");
  }

  // Test that we can goto a different location (we won't get very far if we can't
  // move around the game !)
  @Test
  void testGoto() {
    sendCommandToServer("simon: goto forest");
    String response = sendCommandToServer("simon: look");
    response = response.toLowerCase();
    assertTrue(response.contains("key"),
        "Failed attempt to use 'goto' command to move to the forest - there is no key in the current location");
  }

  // Add more unit tests or integration tests here.
  @Test
  void testOpenTrapdoorWithKey() {
    sendCommandToServer("simon: goto forest");
    sendCommandToServer("simon: get key");
    sendCommandToServer("simon: goto cabin");
    String response = sendCommandToServer("simon: open trapdoor with key");

    assertTrue(response.toLowerCase().contains("unlock"), "Expected narration about unlocking trapdoor");

    // check player can see cellar
    response = sendCommandToServer("simon: look");
    assertTrue(response.toLowerCase().contains("cellar"), "Cellar should be a new path after opening trapdoor");
  }

  @Test
  void testChopTreeWithAxe() {
    sendCommandToServer("simon: get axe");
    sendCommandToServer("simon: goto forest");
    String response = sendCommandToServer("simon: chop tree with axe");

    assertTrue(response.toLowerCase().contains("cut down the tree"), "Expected narration about chopping the tree");

    response = sendCommandToServer("simon: look");
    assertTrue(response.toLowerCase().contains("log"), "Expected to see 'log' in location after chopping tree");
  }

  @Test
  void testChopTreeWithoutAxe() {
    sendCommandToServer("simon: goto forest");
    String response = sendCommandToServer("simon: chop tree");

    assertTrue(response.toLowerCase().contains("error"), "Expected error due to missing axe");
  }

  @Test
  void testOpenTrapdoorWithoutKey() {
    String response = sendCommandToServer("simon: open trapdoor");

    assertTrue(response.toLowerCase().contains("error"), "Expected error due to missing key");
  }

  @Test
  void testDropAxe() {
    sendCommandToServer("simon: get axe");
    sendCommandToServer("simon: drop axe");

    String response = sendCommandToServer("simon: inv");
    assertFalse(response.toLowerCase().contains("axe"), "Axe should be removed from inventory after drop");

    response = sendCommandToServer("simon: look");
    assertTrue(response.toLowerCase().contains("axe"), "Axe should be present in location after drop");
  }

  @Test
  void testInvalidCommandHandling() {
    String response = sendCommandToServer("simon: fly to moon");
    assertTrue(response.toLowerCase().contains("error"), "Unknown command should return error");
  }

  @Test
  void testLocationTransitions() {
    sendCommandToServer("simon: look"); // only forest

    sendCommandToServer("simon: goto forest");
    String response = sendCommandToServer("simon: look");
    assertTrue(response.toLowerCase().contains("cabin"), "Should see 'cabin' path from forest");

    sendCommandToServer("simon: goto cabin");
    sendCommandToServer("simon: get key");
    sendCommandToServer("simon: open trapdoor with key");

    sendCommandToServer("simon: goto cellar");
    response = sendCommandToServer("simon: look");
    assertTrue(response.toLowerCase().contains("cabin"), "Should see 'cabin' path from cellar");
  }

  @Test
  void testExtraneousEntitiesInBuiltInCommand() {
    String response = sendCommandToServer("simon: get key from forest");
    assertTrue(response.toLowerCase().contains("error"), "Should reject built-in command with extra entities");
  }

  @Test
  void testMultiplePlayersSeparation() {
    sendCommandToServer("alice: get axe");
    sendCommandToServer("bob: inv");

    String response = sendCommandToServer("bob: inv");
    assertFalse(response.toLowerCase().contains("axe"), "Bob should not have axe that Alice picked up");
  }

  @Test
  void testOtherPlayersVisibleInLocation() {
    sendCommandToServer("alice: look");
    sendCommandToServer("bob: look");

    String response = sendCommandToServer("alice: look");
    assertTrue(response.toLowerCase().contains("bob"), "Alice should see Bob in the same location");
  }

  @Test
  void testInvalidPlayerName() {
    String response = sendCommandToServer("simon!123: look");
    assertTrue(response.toLowerCase().contains("error"), "Invalid player name should be rejected");
  }

  @Test
  void testInvalidPlayerNames() {
    String response1 = sendCommandToServer("john123: look");
    assertTrue(response1.toLowerCase().contains("error"));

    String response2 = sendCommandToServer("jack@home: look");
    assertTrue(response2.toLowerCase().contains("error"));

    String response3 = sendCommandToServer("okay-name: look");
    assertFalse(response3.toLowerCase().contains("error"));
  }

  @Test
  void testProduceItemFromAnyWhere() {
    sendCommandToServer("simon: get axe");
    sendCommandToServer("simon: goto forest");
    sendCommandToServer("simon: cut tree");
    String response = sendCommandToServer("simon: look");
    sendCommandToServer("simon: get log");
    sendCommandToServer("simon: goto riverbank");
    sendCommandToServer("simon: get horn");
    response = sendCommandToServer("simon: blow horn");
    response = sendCommandToServer("simon: look");
    assertTrue(response.contains("cutter"));
    sendCommandToServer("simon: goto forest");
    sendCommandToServer("simon: blow horn");
    response = sendCommandToServer("simon: look");
    response = response.toLowerCase();
    assertTrue(response.contains("cutter"));
    sendCommandToServer("simon: goto cellar");
    sendCommandToServer("simon: blow horn");
    response = sendCommandToServer("simon: look");
    response = response.toLowerCase();
    assertTrue(response.contains("cutter"));
  }

}
