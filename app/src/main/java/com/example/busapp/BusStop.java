package com.example.busapp;

public class BusStop {
    private String nodeName;
    private String nodeId;
    private String nodeNo;

    public BusStop(String nodeId, String nodeName, String nodeNo){
        this.nodeName = nodeName;
        this.nodeId = nodeId;
        this.nodeNo = nodeNo;
    }

    public String getNodeNo() {
        return nodeNo;
    }

    public void setNodeNo(String nodeNo) {
        this.nodeNo = nodeNo;
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
