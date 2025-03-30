package edu.uob;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Paths;
import java.io.File;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ActionsFileTests {

    /**
     * actions
     * - action
     * 1. trigger: { 1: open, 2: unlock }
     * 2. subject: { 1: trapdoor, 2: key }
     * 3. consumed: { 1: key }
     * 4. produced: { 1: cellar }
     */

    // Test to make sure that the basic actions file is readable
    @Test
    void testBasicActionsFileIsReadable() {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            File xmlFile = Paths.get("config", "basic-actions.xml").toFile();
            Document document = builder.parse(xmlFile);
            Element root = document.getDocumentElement();
            NodeList actions = root.getChildNodes();
            // Get the first action (only the odd items are actually actions - 1, 3, 5 etc.)
            Element firstAction = (Element) actions.item(1);
            Element triggers = (Element) firstAction.getElementsByTagName("triggers").item(0);
            // Get the first trigger phrase
            String firstTriggerPhrase = triggers.getElementsByTagName("keyphrase").item(0).getTextContent();
            assertEquals("open", firstTriggerPhrase, "First trigger phrase was not 'open'");
        } catch (ParserConfigurationException pce) {
            fail("ParserConfigurationException was thrown when attempting to read basic actions file");
        } catch (SAXException saxe) {
            fail("SAXException was thrown when attempting to read basic actions file");
        } catch (IOException ioe) {
            fail("IOException was thrown when attempting to read basic actions file");
        }
    }

    @Test
    void testFirstActionTriggersIncludeOpenUnlock() {
        GameAction parser = new GameAction();
        File xmlFile = Paths.get("config", "extended-actions.xml").toFile();
        Map<String, Map<String, Map<String, String>>> actions = parser.parse(xmlFile, false);

        Map<String, Map<String, String>> action1 = actions.get("action#1");
        Map<String, String> triggers = action1.get("triggers");

        assertTrue(triggers.containsValue("open"));
        assertTrue(triggers.containsValue("unlock"));
    }

    @Test
    void testHealthIsProducedInThirdAction() {
        GameAction parser = new GameAction();
        File xmlFile = Paths.get("config", "extended-actions.xml").toFile();
        Map<String, Map<String, Map<String, String>>> actions = parser.parse(xmlFile, false);

        Map<String, Map<String, String>> action3 = actions.get("action#3");
        Map<String, String> produced = action3.get("produced");

        assertTrue(produced.containsValue("health"));
    }

    @Test
    void testPayActionProducesShovel() {
        GameAction parser = new GameAction();
        File xmlFile = Paths.get("config", "extended-actions.xml").toFile();
        Map<String, Map<String, Map<String, String>>> actions = parser.parse(xmlFile, false);

        Map<String, Map<String, String>> action5 = actions.get("action#5");
        Map<String, String> produced = action5.get("produced");

        assertTrue(produced.containsValue("shovel"));
    }

}
