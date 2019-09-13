package com.uet.procon.worker.service;

import com.uet.procon.common.entity.MapImpl;
import com.uet.procon.common.enums.ApplyTypes;
import com.uet.procon.common.enums.MoveMsgs;
import com.uet.procon.common.enums.MoveTypes;
import com.uet.procon.common.model.*;
import com.uet.procon.common.util.CompareUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class GameplayService {

    @Autowired
    private ScoringService scoringService;

    private boolean isPlaying = false;

    private int cntRegisterMove = 0, remainedTurn = 0;

    private Map<Integer, Boolean> teamAlreadyMoved = new HashMap<>();

    private Map<Integer, Boolean> agentAlreadyMoved = new HashMap<>();

    private Map<Integer, Integer> agentIdToTeamId = new HashMap<>();

    private Map<Integer, AgentModel> agentIdToAgent = new HashMap<>();

    private ClientActionListModel localActions = new ClientActionListModel();

    private MapImpl map = new MapImpl();

    public MapImpl getMap() {
        return this.map;
    }

    public int getRemainedTurn() {
        return remainedTurn;
    }

    public void clearGame() {
        isPlaying = false;
        cntRegisterMove = remainedTurn = 0;
        agentIdToTeamId = new HashMap<>();
        agentIdToAgent = new HashMap<>();
        teamAlreadyMoved = new HashMap<>();
        agentAlreadyMoved = new HashMap<>();
        localActions = new ClientActionListModel();
        localActions.setActions(new ArrayList<>());
        this.map = new MapImpl();
    }

    public void setGame(MapImpl map, int turn) {
        clearGame();
        isPlaying = true;
        remainedTurn = turn;
        this.map = map;
        for (TeamModel team : map.getTeams()) {
            teamAlreadyMoved.put(team.getTeamID(), false);
            for (AgentModel agent : team.getAgents()) {
                agentAlreadyMoved.put(agent.getAgentID(), false);
                agentIdToTeamId.put(agent.getAgentID(), team.getTeamID());
                agentIdToAgent.put(agent.getAgentID(), agent);
            }
        }
    }

    public String registerMove(int teamId, ClientActionListModel actions) {
        String msg = isValidation(teamId, actions);
        if (!msg.equals(MoveMsgs.ACCEPTED))
            return msg;
        teamAlreadyMoved.put(teamId, true);
        localActions.getActions().addAll(actions.getActions());
        cntRegisterMove++;
        if (cntRegisterMove == 2) {
            move();
            remainedTurn--;
            if (remainedTurn == 0)
                isPlaying = false;
        }
        return msg;
    }

    private String isValidation(int teamId, ClientActionListModel actions) {
        if (!isPlaying)
            return MoveMsgs.FINISHED;
        // Check already moved
        if (!CompareUtil.isEqualsNull(teamAlreadyMoved.get(teamId)) && teamAlreadyMoved.get(teamId))
            return MoveMsgs.WAITING;
        // Check true agents
        for (ClientActionModel action : actions.getActions())
            if (agentIdToTeamId.get(action.getAgentID()) != teamId)
                return MoveMsgs.INVALID_AGENT;
        // Check boundary cases
        for (ClientActionModel action : actions.getActions()) {
            if (!action.getType().equals(MoveTypes.MOVE) && !action.getType().equals(MoveTypes.REMOVE) && !action.getType().equals(MoveTypes.STAY))
                return MoveMsgs.BOUNDARY;
            if (action.getDx() < -1 || action.getDx() > 1 || action.getDy() < -1 || action.getDy() > 1)
                return MoveMsgs.INVALID_DIRECTION;
        }
        return MoveMsgs.ACCEPTED;
    }

    private boolean canMove(ClientActionModel clientAction) {
        ActionModel action = new ActionModel();
        action.setAgentID(clientAction.getAgentID());
        action.setType(clientAction.getType());
        action.setDx(clientAction.getDx());
        action.setDy(clientAction.getDy());
        action.setTurn(map.getTurn());
        int oldX = agentIdToAgent.get(clientAction.getAgentID()).getX();
        int oldY = agentIdToAgent.get(clientAction.getAgentID()).getY();
        int newX = oldX + clientAction.getDx();
        int newY = oldY + clientAction.getDy();
        if (clientAction.getType().equals(MoveTypes.MOVE)) {
            // Check boundary case
            if (newX <= 0 || newX > map.getWidth() || newY <= 0 || newY > map.getHeight()) {
                action.setApply(ApplyTypes.INVALID);
                map.getActions().add(action);
                return true;
            }
            // Check tile
            if (map.getTile(newX, newY) != 0 && map.getTile(newX, newY) != agentIdToTeamId.get(clientAction.getAgentID())) {
                action.setApply(ApplyTypes.INVALID);
                map.getActions().add(action);
                return true;
            }
            // Check obstacle
            for (ClientActionModel tempClientAction : localActions.getActions())
                if (agentIdToAgent.get(tempClientAction.getAgentID()).getX() == newX
                        && agentIdToAgent.get(tempClientAction.getAgentID()).getY() == newY)
                    return false;
            // Check conflict
            for (ClientActionModel tempClientAction : localActions.getActions())
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
            map.setTile(newX, newY, agentIdToTeamId.get(clientAction.getAgentID()));
            for (TeamModel team : map.getTeams())
                if (team.getTeamID() == agentIdToTeamId.get(clientAction.getAgentID())) {
                    for (AgentModel agent : team.getAgents())
                        if (agent.getAgentID() == clientAction.getAgentID()) {
                            agent.setX(newX);
                            agent.setY(newY);
                            break;
                        }
                    break;
                }
            action.setApply(ApplyTypes.VALID);
            map.getActions().add(action);
            return true;
        } else if (clientAction.getType().equals(MoveTypes.REMOVE)) {
            // Check boundary case
            if (newX <= 0 || newX > map.getWidth() || newY <= 0 || newY > map.getHeight()) {
                action.setApply(ApplyTypes.INVALID);
                map.getActions().add(action);
                return true;
            }
            // Check tile
            if (map.getTile(newX, newY) != 0 && map.getTile(newX, newY) == agentIdToTeamId.get(clientAction.getAgentID())) {
                action.setApply(ApplyTypes.INVALID);
                map.getActions().add(action);
                return true;
            }
            // Check obstacle
            for (ClientActionModel tempClientAction : localActions.getActions())
                if (agentIdToAgent.get(tempClientAction.getAgentID()).getX() == newX
                        && agentIdToAgent.get(tempClientAction.getAgentID()).getY() == newY)
                    return false;
            // Check conflict
            for (ClientActionModel tempClientAction : localActions.getActions())
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
        for (ClientActionModel clientAction : localActions.getActions())
            agentAlreadyMoved.put(clientAction.getAgentID(), false);
        boolean isHasStep = true;
        while (isHasStep) {
            isHasStep = false;
            for (ClientActionModel clientAction : localActions.getActions()) {
                if (agentAlreadyMoved.get(clientAction.getAgentID()))
                    continue;
                if (canMove(clientAction)) {
                    agentAlreadyMoved.put(clientAction.getAgentID(), true);
                    isHasStep = true;
                }
            }
        }
        // Check remained unmoved agents
        for (ClientActionModel clientAction : localActions.getActions())
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
        // Re-scoring point
        scoringService.setMap(map);
        for (TeamModel team : map.getTeams()) {
            team.setTilePoint(scoringService.scoringTilePoint(team.getTeamID()));
            team.setAreaPoint(scoringService.scoringAreaPoint(team.getTeamID()));
        }
        // Reset move
        cntRegisterMove = 0;
        for (TeamModel team : map.getTeams())
            teamAlreadyMoved.put(team.getTeamID(), false);
        localActions.getActions().clear();
    }
}
