package edu.uob;

import org.w3c.dom.NodeList;

public class ParseResponse {
  private NodeList nodeList;
  private String error;

  public ParseResponse(NodeList nodeList) {
    this.nodeList = nodeList;
  }

  public ParseResponse(String error) {
    this.error = error;
  }

  public boolean hasError() {
    return error != null;
  }

  public NodeList getNodeList() {
    return this.nodeList;
  }

  public String getError() {
    return this.error;
  }

}
