package com.uet.procon.common.entity;

import com.uet.procon.common.entity.base.AbstractEntity;
import com.uet.procon.common.model.ActionModel;
import com.uet.procon.common.model.TeamModel;
import com.uet.procon.common.util.ListUtil;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

public class MapModel extends AbstractEntity<String> {

    @Field
    private int width;

    @Field
    private int height;

    @Field
    private List<List<Integer>> points;

    @Field
    private int startedAtUnixTime;

    @Field
    private int turn;

    @Field
    private List<List<Integer>> tiled;

    @Field
    private List<TeamModel> teams;

    @Field
    private List<ActionModel> actions;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<List<Integer>> getPoints() {
        return points;
    }

    public void setPoints(List<List<Integer>> points) {
        this.points = points;
    }

    public int getStartedAtUnixTime() {
        return startedAtUnixTime;
    }

    public void setStartedAtUnixTime(int startedAtUnixTime) {
        this.startedAtUnixTime = startedAtUnixTime;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public List<List<Integer>> getTiled() {
        return tiled;
    }

    public void setTiled(List<List<Integer>> tiled) {
        this.tiled = tiled;
    }

    public List<TeamModel> getTeams() {
        return teams;
    }

    public void setTeams(List<TeamModel> teams) {
        this.teams = teams;
    }

    public List<ActionModel> getActions() {
        return actions;
    }

    public void setActions(List<ActionModel> actions) {
        this.actions = actions;
    }

    // More methods
    public void initPoints() {
        points = ListUtil.init(points, width, height);
    }

    public int getPoint(int x, int y) {
        return ListUtil.get(points, x, y);
    }

    public void setPoint(int x, int y, int value) {
        points = ListUtil.set(points, x, y, value);
    }

    public void initTiled() {
        tiled = ListUtil.init(tiled, width, height);
    }

    public int getTile(int x, int y) {
        return ListUtil.get(tiled, x, y);
    }

    public void setTile(int x, int y, int value) {
        tiled = ListUtil.set(tiled, x, y, value);
    }
}
