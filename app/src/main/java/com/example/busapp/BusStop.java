package com.example.busapp;

public class BusStop {
    private String nodeName;
    private String nodeId;

    public BusStop(){

    }
    public BusStop(String nodeName, String nodeId){
        this.nodeName = nodeName;
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}
