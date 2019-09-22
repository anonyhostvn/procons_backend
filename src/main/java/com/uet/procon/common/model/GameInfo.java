package com.uet.procon.common.model;

public class GameInfo {

    private int id;

    private int intervalMillis;

    private String matchTo;

    private int teamID;

    private int turnMillis;

    private int turns;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIntervalMillis() {
        return intervalMillis;
    }

    public void setIntervalMillis(int intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    public String getMatchTo() {
        return matchTo;
    }

    public void setMatchTo(String matchTo) {
        this.matchTo = matchTo;
    }

    public int getTeamID() {
        return teamID;
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
    }

    public int getTurnMillis() {
        return turnMillis;
    }

    public void setTurnMillis(int turnMillis) {
        this.turnMillis = turnMillis;
    }

    public int getTurns() {
        return turns;
    }

    public void setTurns(int turns) {
        this.turns = turns;
    }
}
