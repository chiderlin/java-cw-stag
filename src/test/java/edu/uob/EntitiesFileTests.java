package edu.uob;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;

import edu.uob.entities.EntityParser;
import edu.uob.entities.EntityType;
import edu.uob.entities.Location;

import com.alexmerz.graphviz.objects.Edge;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class EntitiesFileTests {
    /**
     * layout
     * - subgraph: locations
     * 1. node
     * 2. location name
     * - subgraph: paths
     */

    // Test to make sure that the basic entities file is readable
    @Test
    void testBasicEntitiesFileIsReadable() {
        try {
            Parser parser = new Parser();

            // FileReader reader = new FileReader("config" + File.separator +
            // "basic-entities.dot");
            Path entitiesPath = Paths.get("config", "basic-entities.dot");
            FileReader reader = new FileReader(entitiesPath.toFile());

            parser.parse(reader);
            Graph wholeDocument = parser.getGraphs().get(0);
            ArrayList<Graph> sections = wholeDocument.getSubgraphs();

            // The locations will always be in the first subgraph
            ArrayList<Graph> locations = sections.get(0).getSubgraphs();
            Graph firstLocation = locations.get(0);
            Node locationDetails = firstLocation.getNodes(false).get(0);
            // Yes, you do need to get the ID twice !
            String locationName = locationDetails.getId().getId();
            assertEquals("cabin", locationName, "First location should have been 'cabin'");

            // The paths will always be in the second subgraph
            ArrayList<Edge> paths = sections.get(1).getEdges();
            Edge firstPath = paths.get(0);
            Node fromLocation = firstPath.getSource().getNode();
            String fromName = fromLocation.getId().getId(); // cabin ->
            Node toLocation = firstPath.getTarget().getNode();
            String toName = toLocation.getId().getId(); // forest
            assertEquals("cabin", fromName, "First path should have been from 'cabin'");
            assertEquals("forest", toName, "First path should have been to 'forest'");

        } catch (FileNotFoundException fnfe) {
            fail("FileNotFoundException was thrown when attempting to read basic entities file");
        } catch (ParseException pe) {
            fail("ParseException was thrown when attempting to read basic entities file");
        }
    }

    @Test
    void testCabinHasPathToForest() {
        EntityParser parser = new EntityParser();
        File file = Paths.get("config", "basic-entities.dot").toFile();
        EntityParser.GameWorld gameWorld = parser.parse(file, false);

        Set<String> cabinPaths = gameWorld.graph().get("cabin");
        assertEquals(true, cabinPaths.contains("forest"), "Cabin should connect to forest");
    }

    @Test
    void testCellarContainsElfCharacter() {
        EntityParser parser = new EntityParser();
        File file = Paths.get("config", "basic-entities.dot").toFile();
        EntityParser.GameWorld gameWorld = parser.parse(file, false);

        Location cellar = gameWorld.gameMap().get("cellar");
        assertEquals(true, cellar.containsCharacter("elf"), "Cellar should contain elf");
    }

    @Test
    void testTotalNumberOfLocations() {
        EntityParser parser = new EntityParser();
        File file = Paths.get("config", "extended-entities.dot").toFile();
        EntityParser.GameWorld gameWorld = parser.parse(file, false);
        assertEquals(6, gameWorld.gameMap().size(), "There should be 6 locations including storeroom");
    }

    @Test
    void testArtefactTypeMapping() {
        EntityParser parser = new EntityParser();
        File file = Paths.get("config", "extended-entities.dot").toFile();
        EntityParser.GameWorld gameWorld = parser.parse(file, false);

        Map<String, EntityType> types = gameWorld.entityTypesByName();
        assertEquals(EntityType.ARTEFACT, types.get("potion"), "Potion should be of type ARTEFACT");
        assertEquals(EntityType.ARTEFACT, types.get("coin"), "Coin should be of type ARTEFACT");
    }

}
