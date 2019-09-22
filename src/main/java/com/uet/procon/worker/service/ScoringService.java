package com.uet.procon.worker.service;

import com.uet.procon.common.model.MapModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScoringService {

    private final int[] dX = {0, 0, -1, 1};
    private final int[] dY = {-1, 1, 0, 0};

    private MapModel map;

    private List<List<Boolean>> isMark = new ArrayList<>();

    private int tempPoint;

    private boolean isReachBoundary;

    public void setMap(MapModel map) {
        this.map = map;
    }

    public int scoringTilePoint(int teamId) {
        int point = 0;
        for (int i = 0; i < map.getWidth(); i++)
            for (int j = 0; j < map.getHeight(); j++)
                if (map.getTile(i + 1, j + 1) == teamId)
                    point += map.getPoint(i + 1, j + 1);
        return point;
    }

    private void dfs(int x, int y, int teamId) {
        if (x == 0 || x == map.getWidth() - 1 || y == 0 || y == map.getHeight() - 1)
            isReachBoundary = true;
        tempPoint += Math.abs(map.getPoint(x + 1, y + 1));
        isMark.get(x).set(y, true);
        for (int direction = 0; direction < 4; direction++) {
            int newX = x + dX[direction], newY = y + dY[direction];
            if (newX < 0 || newX >= map.getWidth() || newY < 0 || newY >= map.getHeight() || isMark.get(newX).get(newY) == true || map.getTile(newX + 1, newY + 1) == teamId)
                continue;
            dfs(newX, newY, teamId);
        }
    }

    public int scoringAreaPoint(int teamId) {
        isMark.clear();
        for (int i = 0; i < map.getWidth(); i++) {
            isMark.add(new ArrayList<>());
            for (int j = 0; j < map.getHeight(); j++)
                isMark.get(i).add(false);
        }
        int point = 0;
        for (int i = 0; i < map.getWidth(); i++)
            for (int j = 0; j < map.getHeight(); j++)
                if (map.getTile(i + 1, j + 1) != teamId && !isMark.get(i).get(j)) {
                    tempPoint = 0;
                    isReachBoundary = false;
                    dfs(i, j, teamId);
                    if (!isReachBoundary)
                        point += tempPoint;
                }
        return point;
    }
}
