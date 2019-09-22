package com.uet.procon.common.model;

import java.io.Serializable;
import java.util.List;

public class TeamModel implements Serializable {

    private int teamID;

    private List<AgentModel> agents;

    private int tilePoint;

    private int areaPoint;

    public int getTeamID() {
        return teamID;
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
    }

    public List<AgentModel> getAgents() {
        return agents;
    }

    public void setAgents(List<AgentModel> agents) {
        this.agents = agents;
    }

    public int getTilePoint() {
        return tilePoint;
    }

    public void setTilePoint(int tilePoint) {
        this.tilePoint = tilePoint;
    }

    public int getAreaPoint() {
        return areaPoint;
    }

    public void setAreaPoint(int areaPoint) {
        this.areaPoint = areaPoint;
    }
}
