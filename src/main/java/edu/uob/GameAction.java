package edu.uob;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * // Map<String, Map<String, Map<String,String>>> actionsMapping
 * actionsMapping = {
 * action#1:{
 * trigger:{ 1:open, 2:unlock},
 * subject:{1:trapdoor, 2: key},
 * consumed:{1:key},
 * produced:{1:cellar}
 * },
 * action#2:{
 * trigger:{ 1:open, 2:unlock},
 * subject:{1:trapdoor, 2: key},
 * consumed:{1:key},
 * produced:{1:cellar}
 * }
 * }
 */
public class GameAction {
  private Map<String, Map<String, Map<String, String>>> actionsMapping = new HashMap<>();

  private ParseResponse initParser(File actionsFile) {
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(actionsFile);
      Element root = document.getDocumentElement();
      NodeList actions = root.getChildNodes();
      System.out.println(actions.getLength());
      if (actions.getLength() == 0) {
        System.out.printf("[Error] No <action> elements found.");
        return new ParseResponse("[Error] No <action> elements found.");
      }

      return new ParseResponse(actions);

    } catch (ParserConfigurationException pce) {
      String errorMsg = String.format("[ERROR] ParserConfigurationException was thrown: %s", pce.getMessage());
      System.err.println(errorMsg);
      return new ParseResponse(errorMsg);

    } catch (SAXException saxe) {
      String errorMsg = String.format("[ERROR] SAXException was thrown: %s", saxe.getMessage());
      System.err.println(errorMsg);
      return new ParseResponse(errorMsg);

    } catch (IOException ioe) {
      String errorMsg = String.format("[ERROR] IOException was thrown: %s", ioe.getMessage());
      System.err.printf(errorMsg);
      return new ParseResponse(errorMsg);

    } catch (Exception e) {
      e.printStackTrace();
      String errorMsg = String.format("[ERROR] Exception was thrown: %s", e.getMessage());
      System.err.printf(errorMsg);
      return new ParseResponse(errorMsg);

    }
  }

  private Map<String, Map<String, String>> getNarration(Element actionEle) {
    NodeList narrations = actionEle.getElementsByTagName("narration");
    Map<String, Map<String, String>> narrMap = new HashMap<>(); // {1:xxx,2:xxxx}
    Map<String, String> narrationsMap = new HashMap<>();
    for (int n = 0; n < narrations.getLength(); n++) {
      Element narrationEle = (Element) narrations.item(n);
      String narratonValue = narrationEle.getTextContent();
      narrationsMap.put(Integer.toString(n + 1), narratonValue);
    }
    narrMap.put("narration", narrationsMap);
    return narrMap;
  }

  public Map<String, Map<String, Map<String, String>>> parse(File actionsFile, boolean debugMode) {

    try {
      ParseResponse res = this.initParser(actionsFile);
      if (res.hasError())
        return null;

      NodeList actions = res.getNodeList();
      int idx = 1;
      for (int i = 0; i < actions.getLength(); i++) {
        Node action = actions.item(i);
        if (action.getNodeType() != Node.ELEMENT_NODE)
          continue;

        Element actionEle = (Element) action;
        NodeList triggers = actionEle.getElementsByTagName("triggers");
        NodeList subjects = actionEle.getElementsByTagName("subjects");
        NodeList consumed = actionEle.getElementsByTagName("consumed");
        NodeList produced = actionEle.getElementsByTagName("produced");
        Map<String, Map<String, String>> triggerMap = this.actionParser(triggers, "triggers", "keyphrase");
        Map<String, Map<String, String>> subjectsMap = this.actionParser(subjects, "subjects", "entity");
        Map<String, Map<String, String>> consumedMap = this.actionParser(consumed, "consumed", "entity");
        Map<String, Map<String, String>> producedMap = this.actionParser(produced, "produced", "entity");

        // each action has new actionMap
        Map<String, Map<String, String>> fullActionMap = new HashMap<>();

        // get Narration Map
        Map<String, Map<String, String>> narrMap = this.getNarration(actionEle);

        String actionKey = String.format("action#%s", idx++);
        fullActionMap.putAll(triggerMap);
        fullActionMap.putAll(subjectsMap);
        fullActionMap.putAll(consumedMap);
        fullActionMap.putAll(producedMap);
        fullActionMap.putAll(narrMap);
        actionsMapping.put(actionKey, fullActionMap);
      }
      if (debugMode)
        this._printActionStructure();

      return actionsMapping;

    } catch (Exception e) {
      e.printStackTrace();
      System.err.printf("[ERROR] Exception was thrown: %s", e.getMessage());
      return null;
    }
  }

  public void parseAll(Queue<File> files) {
    while (!files.isEmpty()) {
      File actionsFile = files.poll(); // get first
      this.parse(actionsFile, true);
    }
    System.out.print("Finish loading All Actions Files.");
  }

  private Map<String, Map<String, String>> actionParser(NodeList childElement, String childEleName,
      String targetElementValue) {
    Map<String, Map<String, String>> childMap = new HashMap<>();

    try {
      if (childElement.getLength() == 0) {
        System.out.printf("No <%s> elements found.", childElement);
      } else {
        for (int j = 0; j < childElement.getLength(); j++) {
          Map<String, String> childNode = new HashMap<>();
          Element childChildEle = (Element) childElement.item(j);
          NodeList targetElements = childChildEle.getElementsByTagName(targetElementValue);
          if (targetElements.getLength() == 0) {
            System.out.printf("No <%s> elements found.\n", targetElementValue);
          } else {
            for (int k = 0; k < targetElements.getLength(); k++) {
              Node targetNode = targetElements.item(k);
              Element targetEle = (Element) targetNode;
              childNode.put(Integer.toString(k + 1), targetEle.getTextContent()); // trigger:{ 1:open, 2:unlock},
            }
          }
          childMap.put(childEleName, childNode);
        }
      }
      return childMap;
    } catch (Exception e) {
      e.printStackTrace();
      System.err.printf("[Error] actionParser: ", e.getMessage());
      return childMap;
    }
  }

  private void _printActionStructure() {
    try {
      for (Map.Entry<String, Map<String, Map<String, String>>> actionEntry : this.actionsMapping.entrySet()) {
        String actionName = actionEntry.getKey();
        System.out.printf("Action: %s\n", actionName);

        Map<String, Map<String, String>> categories = actionEntry.getValue();

        for (Map.Entry<String, Map<String, String>> categoryEntry : categories.entrySet()) {
          String category = categoryEntry.getKey();
          System.out.printf("  Category: %s\n", category);

          Map<String, String> items = categoryEntry.getValue();
          for (Map.Entry<String, String> itemEntry : items.entrySet()) {
            System.out.printf("    %s: %s\n", itemEntry.getKey(), itemEntry.getValue());
          }
        }
      }
      System.out.println("-----------");
    } catch (Exception e) {
      System.err.printf("[Error] Failed to print action structure: %s\n", e.getMessage());
    }

  }

}
