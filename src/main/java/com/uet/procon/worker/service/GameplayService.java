package com.uet.procon.worker.service;

import com.uet.procon.common.enums.ApplyTypes;
import com.uet.procon.common.enums.MoveMsgs;
import com.uet.procon.common.enums.MoveTypes;
import com.uet.procon.common.model.*;
import com.uet.procon.common.util.CompareUtil;
import com.uet.procon.common.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class GameplayService {

    @Autowired
    public ScoringService scoringService;

    private boolean isPlaying = false;

    private int cntRegisterMove = 0;

    private Map<Integer, Boolean> teamAlreadyMoved = new HashMap<>();

    private Map<Integer, Boolean> agentAlreadyMoved = new HashMap<>();

    private Map<Integer, Integer> agentIdToTeamId = new HashMap<>();

    private Map<Integer, AgentModel> agentIdToAgent = new HashMap<>();

    private RequestActionListModel localActions = new RequestActionListModel();

    private MapModel map = new MapModel();

    private GameInfo gameInfo = new GameInfo();

    public MapModel getMap() {
        return this.map;
    }

    public void setMap(int mapId) throws Exception {
        isPlaying = true;
        cntRegisterMove = 0;
        agentIdToTeamId = new HashMap<>();
        agentIdToAgent = new HashMap<>();
        teamAlreadyMoved = new HashMap<>();
        agentAlreadyMoved = new HashMap<>();
        localActions = new RequestActionListModel();
        localActions.setActions(new ArrayList<>());
        map = new MapModel();
        gameInfo = new GameInfo();
        map = JsonUtil.readMapFromFile(mapId);
        gameInfo = JsonUtil.readGameInfosFromFile()[mapId - 1];
        for (TeamModel team : map.getTeams()) {
            teamAlreadyMoved.put(team.getTeamID(), false);
            for (AgentModel agent : team.getAgents()) {
                agentAlreadyMoved.put(agent.getAgentID(), false);
                agentIdToTeamId.put(agent.getAgentID(), team.getTeamID());
                agentIdToAgent.put(agent.getAgentID(), agent);
            }
        }
    }

    private String isValidation(int teamId, RequestActionListModel actions) {
        // Check finished game
        if (!isPlaying)
            return MoveMsgs.FINISHED;
        // Check already moved
        if (!CompareUtil.isEqualsNull(teamAlreadyMoved.get(teamId)) && teamAlreadyMoved.get(teamId))
            return MoveMsgs.ALREADY_MOVED;
        // Check true agents
        for (RequestActionModel action : actions.getActions())
            if (agentIdToTeamId.get(action.getAgentID()) != teamId)
                return MoveMsgs.INVALID_AGENT;
        // Check boundary cases
        for (RequestActionModel action : actions.getActions()) {
            if (!action.getType().equals(MoveTypes.MOVE) && !action.getType().equals(MoveTypes.REMOVE) && !action.getType().equals(MoveTypes.STAY))
                return MoveMsgs.INVALID_COMMAND;
            if (action.getDx() < -1 || action.getDx() > 1 || action.getDy() < -1 || action.getDy() > 1)
                return MoveMsgs.INVALID_DIRECTION;
        }
        if (cntRegisterMove == 0)
            return MoveMsgs.WAITING;
        return MoveMsgs.ACCEPTED;
    }

    public String registerMove(int teamId, RequestActionListModel actions) {
        String msg = isValidation(teamId, actions);
        if (!msg.equals(MoveMsgs.ACCEPTED) && !msg.equals(MoveMsgs.WAITING))
            return msg;
        teamAlreadyMoved.put(teamId, true);
        localActions.getActions().addAll(actions.getActions());
        cntRegisterMove++;
        if (cntRegisterMove == 2) {
            move();
        }
        return msg;
    }

    private boolean canMove(RequestActionModel requestAction) {
        ActionModel action = new ActionModel();
        action.setAgentID(requestAction.getAgentID());
        action.setType(requestAction.getType());
        action.setDx(requestAction.getDx());
        action.setDy(requestAction.getDy());
        action.setTurn(map.getTurn());
        int oldX = agentIdToAgent.get(requestAction.getAgentID()).getX();
        int oldY = agentIdToAgent.get(requestAction.getAgentID()).getY();
        int newX = oldX + requestAction.getDx();
        int newY = oldY + requestAction.getDy();
        if (requestAction.getType().equals(MoveTypes.MOVE)) {
            // Check boundary case
            if (newX <= 0 || newX > map.getWidth() || newY <= 0 || newY > map.getHeight()) {
                action.setApply(ApplyTypes.INVALID);
                map.getActions().add(action);
                return true;
            }
            // Check tile
            if (map.getTile(newX, newY) != 0 && map.getTile(newX, newY) != agentIdToTeamId.get(requestAction.getAgentID())) {
                action.setApply(ApplyTypes.INVALID);
                map.getActions().add(action);
                return true;
            }
            // Check obstacle
            for (RequestActionModel tempClientAction : localActions.getActions())
                if (agentIdToAgent.get(tempClientAction.getAgentID()).getX() == newX
                        && agentIdToAgent.get(tempClientAction.getAgentID()).getY() == newY)
                    return false;
            // Check conflict
            for (RequestActionModel tempClientAction : localActions.getActions())
                if (!agentAlreadyMoved.get(tempClientAction.getAgentID()) && tempClientAction.getType() == MoveTypes.MOVE) {
                    int newTempX = agentIdToAgent.get(tempClientAction.getAgentID()).getX() + tempClientAction.getDx();
                    int newTempY = agentIdToAgent.get(tempClientAction.getAgentID()).getY() + tempClientAction.getDy();
                    if (newTempX == newX && newTempY == newY) {
                        action.setApply(ApplyTypes.CONFLICT);
                        map.getActions().add(action);
                        return true;
                    }
                }
            // Move normally
            map.setTile(newX, newY, agentIdToTeamId.get(requestAction.getAgentID()));
            for (TeamModel team : map.getTeams())
                if (team.getTeamID() == agentIdToTeamId.get(requestAction.getAgentID())) {
                    for (AgentModel agent : team.getAgents())
                        if (agent.getAgentID() == requestAction.getAgentID()) {
                            agent.setX(newX);
                            agent.setY(newY);
                            break;
                        }
                    break;
                }
            action.setApply(ApplyTypes.VALID);
            map.getActions().add(action);
            return true;
        } else if (requestAction.getType().equals(MoveTypes.REMOVE)) {
            // Check boundary case
            if (newX <= 0 || newX > map.getWidth() || newY <= 0 || newY > map.getHeight()) {
                action.setApply(ApplyTypes.INVALID);
                map.getActions().add(action);
                return true;
            }
            // Check tile
            if (map.getTile(newX, newY) != 0 && map.getTile(newX, newY) == agentIdToTeamId.get(requestAction.getAgentID())) {
                action.setApply(ApplyTypes.INVALID);
                map.getActions().add(action);
                return true;
            }
            // Check obstacle
            for (RequestActionModel tempClientAction : localActions.getActions())
                if (agentIdToAgent.get(tempClientAction.getAgentID()).getX() == newX
                        && agentIdToAgent.get(tempClientAction.getAgentID()).getY() == newY)
                    return false;
            // Check conflict
            for (RequestActionModel tempClientAction : localActions.getActions())
                if (!agentAlreadyMoved.get(tempClientAction.getAgentID())) {
                    int newTempX = agentIdToAgent.get(tempClientAction.getAgentID()).getX() + tempClientAction.getDx();
                    int newTempY = agentIdToAgent.get(tempClientAction.getAgentID()).getY() + tempClientAction.getDy();
                    if (newTempX == newX && newTempY == newY) {
                        action.setApply(ApplyTypes.CONFLICT);
                        map.getActions().add(action);
                        return true;
                    }
                }
            // Remove normally
            map.setTile(newX, newY, 0);
            action.setApply(ApplyTypes.VALID);
            map.getActions().add(action);
            return true;
        } else {
            action.setApply(ApplyTypes.VALID);
            map.getActions().add(action);
            return true;
        }
    }

    private void move() {
        map.setTurn(map.getTurn() + 1);
        // Moving
        agentAlreadyMoved = new HashMap<>();
        for (RequestActionModel clientAction : localActions.getActions())
            agentAlreadyMoved.put(clientAction.getAgentID(), false);
        boolean isHasStep = true;
        while (isHasStep) {
            isHasStep = false;
            for (RequestActionModel clientAction : localActions.getActions()) {
                if (agentAlreadyMoved.get(clientAction.getAgentID()))
                    continue;
                if (canMove(clientAction)) {
                    agentAlreadyMoved.put(clientAction.getAgentID(), true);
                    isHasStep = true;
                }
            }
        }
        // Check remained unmoved agents
        for (RequestActionModel clientAction : localActions.getActions())
            if (!agentAlreadyMoved.get(clientAction.getAgentID())) {
                ActionModel action = new ActionModel();
                action.setAgentID(clientAction.getAgentID());
                action.setType(clientAction.getType());
                action.setDx(clientAction.getDx());
                action.setDy(clientAction.getDy());
                action.setTurn(map.getTurn());
                action.setApply(ApplyTypes.INVALID);
                map.getActions().add(action);
            }
        // Re-calculate scores
        scoringService.setMap(map);
        for (TeamModel team : map.getTeams()) {
            team.setTilePoint(scoringService.scoringTilePoint(team.getTeamID()));
            team.setAreaPoint(scoringService.scoringAreaPoint(team.getTeamID()));
        }
        // Reset moves
        cntRegisterMove = 0;
        for (TeamModel team : map.getTeams())
            teamAlreadyMoved.put(team.getTeamID(), false);
        localActions.getActions().clear();
        // Check finished game
        if (map.getTurn() == gameInfo.getTurns())
            isPlaying = false;
    }
}