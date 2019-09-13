package com.uet.procon.common.model;

public class AgentModel {

    private int agentID;

    private int x;

    private int y;

    public AgentModel(int agentID, int x, int y) {
        this.agentID = agentID;
        this.x = x;
        this.y = y;
    }

    public int getAgentID() {
        return agentID;
    }

    public void setAgentID(int agentID) {
        this.agentID = agentID;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
