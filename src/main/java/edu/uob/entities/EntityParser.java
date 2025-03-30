package edu.uob.entities;

import java.util.*;
import java.io.*;
import com.alexmerz.graphviz.*;
import com.alexmerz.graphviz.objects.*;

public class EntityParser {
  private final Map<String, Location> gameMap = new HashMap<>();
  private final Map<String, Set<String>> locationGraph = new HashMap<>();
  private Location startLocation;
  private Location storeroom;
  private Map<String, GameEntity> allEntitiesByName = new HashMap<>();
  private Map<String, EntityType> entityTypesByName = new HashMap<>();

  public record GameWorld(Map<String, Location> gameMap,
      Map<String, Set<String>> graph,
      Location startLocation,
      Location storeroom,
      Map<String, GameEntity> allEntitiesByName,
      Map<String, EntityType> entityTypesByName) {
  }

  private void _printEntityStructure() {
    try {
      for (Map.Entry<String, Location> entry : this.gameMap.entrySet()) {
        Location location = entry.getValue();
        System.out.printf("📍 Location: %s\n", location.getName());
        System.out.printf("  - %s\n", location.getDescription());

        if (!location.getArtefacts().isEmpty()) {
          System.out.println("  💎 Artefacts:");
          Map<String, GameEntity> artefacts = location.getArtefacts();
          for (Map.Entry<String, GameEntity> artefact : artefacts.entrySet()) {
            String name = artefact.getKey();
            String desc = artefact.getValue().getDescription();
            System.out.printf("    ▪ %s: %s\n", name, desc);
          }
        }

        if (!location.getFurniture().isEmpty()) {
          System.out.println("  🪑 Furniture:");
          Map<String, GameEntity> furniture = location.getFurniture();
          for (Map.Entry<String, GameEntity> item : furniture.entrySet()) {
            String name = item.getKey();
            String desc = item.getValue().getDescription();
            System.out.printf("    ▪ %s: %s\n", name, desc);
          }
        }

        if (!location.getCharacters().isEmpty()) {
          System.out.println("  👤 Characters:");
          Map<String, GameEntity> characters = location.getCharacters();
          for (Map.Entry<String, GameEntity> character : characters.entrySet()) {
            String name = character.getKey();
            String desc = character.getValue().getDescription();
            System.out.printf("    ▪ %s: %s\n", name, desc);
          }
        }

        System.out.println("------------------------------------------------");
      }
    } catch (Exception e) {
      System.err.printf("[Error] Failed to print entity structure: %s\n", e.getMessage());
    }
  }

  private void _printPath() {
    try {
      System.out.println("📌 Location Paths:");
      for (Map.Entry<String, Set<String>> entry : this.locationGraph.entrySet()) {
        String from = entry.getKey();
        Set<String> destination = entry.getValue();
        for (String to : destination) {
          System.out.printf(" %s -> %s\n", from, to);
        }
      }
    } catch (Exception e) {
      System.err.printf("[Error] Failed to print path: %s\n", e.getMessage());
    }

  }

  public GameWorld parse(File entitiesFile, boolean debugMode) {
    try {
      Parser parser = new Parser();
      FileReader reader = new FileReader(entitiesFile);
      parser.parse(reader);
      Graph wholeDocument = parser.getGraphs().get(0);
      List<Graph> sections = wholeDocument.getSubgraphs();

      this.parseLocations(sections);
      this.parsePath(sections);

      if (debugMode) {
        this._printEntityStructure();
        this._printPath();
      }

    } catch (FileNotFoundException | ParseException e) {
      System.err.printf("[Error] %s : %s", e.getClass().getSimpleName(), e.getMessage());
    }

    return new GameWorld(this.gameMap, this.locationGraph, this.startLocation, this.storeroom, this.allEntitiesByName,
        this.entityTypesByName);
  }

  private void parseLocations(List<Graph> sections) {
    Graph locationSection = null;
    for (Graph g : sections) {
      if ("locations".equals(g.getId().getId())) {
        locationSection = g;
        break;
      }
    }

    if (locationSection == null) {
      System.err.println("[ERROR] No 'locations' section found!");
      return;
    }

    List<Graph> locations = locationSection.getSubgraphs();
    for (int i = 0; i < locations.size(); i++) {
      Graph locationGraph = locations.get(i);
      // Node locationNode = locationGraph.getNodes(false).get(0);
      List<Node> nodes = locationGraph.getNodes(false);
      if (nodes.isEmpty()) {
        System.err.println("[WARN] Location node is empty, skipping...");
        continue;
      }
      Node locationNode = locationGraph.getNodes(false).get(0);
      String locationName = locationNode.getId().getId();
      String locationDesc = locationNode.getAttribute("description");

      Location location = new Location(locationName, locationDesc);
      if (i == 0) {
        this.startLocation = location;
      } else if ("storeroom".equalsIgnoreCase(locationName)) {
        this.storeroom = location;
      }

      List<Graph> subSections = locationGraph.getSubgraphs();
      for (Graph sub : subSections) {
        String subType = sub.getId().getId();
        for (Node entity : sub.getNodes(false)) {
          String name = entity.getId().getId();
          String desc = entity.getAttribute("description");

          EntityType type;
          switch (subType) {
            case "artefacts":
              type = EntityType.ARTEFACT;
              break;
            case "furniture":
              type = EntityType.FURNITURE;
              break;
            case "characters":
              type = EntityType.CHARACTER;
              break;
            default:
              type = null;
              break;
          }

          if (type != null) {
            GameEntity newEntity = new GameEntity(name, desc, type);
            location.addEntity(newEntity);
            allEntitiesByName.put(name, newEntity);
            entityTypesByName.put(name, type);
          }
        }
      }

      this.gameMap.put(locationName, location);
    }
  }

  private void parsePath(List<Graph> sections) {
    List<Edge> paths = sections.get(1).getEdges();
    for (Edge path : paths) {
      String fromName = path.getSource().getNode().getId().getId();
      String toName = path.getTarget().getNode().getId().getId();
      locationGraph.putIfAbsent(fromName, new HashSet<>());
      locationGraph.get(fromName).add(toName);
      Location fromLocation = this.gameMap.get(fromName);
      if (fromLocation == null) {
        System.err.printf("[WARN] Location '%s' not found when adding path to '%s'\n", fromName, toName);
        continue; // skip this path
      }

      fromLocation.addPathTo(toName);
    }
  }

  public void parseAll(Queue<File> files) {
    while (!files.isEmpty()) {
      this.parse(files.poll(), true);
    }
    System.out.printf("Finish loading All Actions Files.");
  }
}