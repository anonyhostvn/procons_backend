package com.uet.procon.common.model;

import java.io.Serializable;

public class RequestActionModel implements Serializable {

    private int agentID;

    private String type;

    private int dx;

    private int dy;

    public int getAgentID() {
        return agentID;
    }

    public void setAgentID(int agentID) {
        this.agentID = agentID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDx() {
        return dx;
    }

    public void setDx(int dx) {
        this.dx = dx;
    }

    public int getDy() {
        return dy;
    }

    public void setDy(int dy) {
        this.dy = dy;
    }
}
