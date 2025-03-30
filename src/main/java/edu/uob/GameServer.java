package edu.uob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import edu.uob.commands.GameCommand;
import edu.uob.entities.EntityParser;
import edu.uob.entities.EntityParser.GameWorld;

public final class GameServer {

    private static final char END_OF_TRANSMISSION = 4;
    private final GameState gameState;
    private final CmdHandler handler;

    public static void main(String[] args) throws IOException {
        // File entitiesFile = new File("config",
        // "basic-entities.dot").getAbsoluteFile();
        // File actionsFile = new File("config", "basic-actions.xml").getAbsoluteFile();

        File entitiesFile = new File("config", "extended-entities.dot").getAbsoluteFile();
        File actionsFile = new File("config", "extended-actions.xml").getAbsoluteFile();

        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    /**
     * Do not change the following method signature or we won't be able to mark your
     * submission
     * Instanciates a new server instance, specifying a game with some configuration
     * files
     *
     * @param entitiesFile The game configuration file containing all game entities
     *                     to use in your game
     * @param actionsFile  The game configuration file containing all game actions
     *                     to use in your game
     */
    public GameServer(File entitiesFile, File actionsFile) {
        // implement your server logic here
        EntityParser entity = new EntityParser();
        GameWorld gameWorld = entity.parse(entitiesFile, false);
        GameAction actions = new GameAction();
        Map<String, Map<String, Map<String, String>>> actionsMapping = actions.parse(actionsFile, false);

        this.gameState = new GameState(gameWorld, actionsMapping);
        this.handler = new CmdHandler(this.gameState);
    }

    /**
     * Do not change the following method signature or we won't be able to mark your
     * submission
     * This method handles all incoming game commands and carries out the
     * corresponding actions.
     * </p>
     *
     * @param command The incoming command to be processed
     */
    public String handleCommand(String command) {
        // implement your server logic here
        try {
            GameCommand gameCmd = this.handler.parse(command);
            return gameCmd.execute();
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = String.format("[Error] handleCommand: %s", e.getMessage());
            System.err.printf(errorMsg);
            return errorMsg;
        }
    }

    /**
     * Do not change the following method signature or we won't be able to mark your
     * submission
     * Starts a *blocking* socket server listening for new connections.
     *
     * @param portNumber The port to listen on.
     * @throws IOException If any IO related operation fails.
     */
    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            String res = String.format("Server listening on port %s", portNumber);
            System.out.println(res);
            while (!Thread.interrupted()) {
                try {
                    this.blockingHandleConnection(s);
                } catch (IOException e) {
                    System.out.println("Connection closed");
                }
            }
        }
    }

    /**
     * Do not change the following method signature or we won't be able to mark your
     * submission
     * Handles an incoming connection from the socket server.
     *
     * @param serverSocket The client socket to read/write from.
     * @throws IOException If any IO related operation fails.
     */
    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if (incomingCommand != null) {
                System.out.printf("Received message from %s", incomingCommand);
                String result = this.handleCommand(incomingCommand);
                writer.write(result);
                // writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.newLine();
                writer.write(END_OF_TRANSMISSION);
                writer.newLine();
                writer.flush();
            }
        }
    }
}
