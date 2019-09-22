package com.uet.procon.application;

import com.uet.procon.common.enums.MoveMsgs;
import com.uet.procon.common.exception.InvalidMoveException;
import com.uet.procon.common.exception.ResourceNotFoundException;
import com.uet.procon.common.model.*;
import com.uet.procon.common.util.JsonUtil;
import com.uet.procon.common.util.LoggingUtil;
import com.uet.procon.worker.service.GameplayService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@CrossOrigin(origins = "*")
@RestController
public class ProconController {

    private Logger logger = LoggingUtil.createLogger(ProconController.class);

    @Autowired
    public GameplayService gameplayService;

    private int playingMap = 0;

    private boolean isWaiting = false;

    // Get match information and start game
    @GetMapping("/matches")
    public GameInfo[] getMatches() throws Exception {
        logger.info("PROCON:GET:/matches");
        return JsonUtil.readGameInfosFromFile();
    }

    // Start game
    @GetMapping("/matches/start/{id}")
    public MapModel startGame(@PathVariable("id") int mapId) throws Exception {
        logger.info("PROCON:GET:/matches/start/" + mapId);
        if (mapId <= 0 || mapId > JsonUtil.readGameInfosFromFile().length)
            throw new ResourceNotFoundException("PROCON:GET:/matches/start/" + mapId + ":NOT_FOUND");
        gameplayService.setMap(mapId);
        playingMap = mapId;
        return JsonUtil.readMapFromFile(mapId);
    }

    // Get game map
    @GetMapping("/matches/{id}")
    public MapModel getMap(@PathVariable("id") int mapId) throws Exception {
        logger.info("PROCON:GET:/matches/" + mapId);
        if (mapId != playingMap)
            throw new ResourceNotFoundException("PROCON:GET:/matches/" + mapId + ":NOT_FOUND");
        return gameplayService.getMap();
    }

    // Player's move
    @PostMapping("/matches/{id}/action")
    public synchronized ResponseActionListModel move(@PathVariable("id") int teamId, @RequestBody RequestActionListModel actions) throws Exception {
        logger.info("PROCON:POST:/matches/" + teamId + "/action");
        // Set response
        ResponseActionListModel responseActions = new ResponseActionListModel();
        responseActions.setActions(new ArrayList<>());
        for (RequestActionModel requestAction : actions.getActions()) {
            ResponseActionModel responseAction = new ResponseActionModel();
            responseAction.setAgentID(requestAction.getAgentID());
            responseAction.setType(requestAction.getType());
            responseAction.setDx(requestAction.getDx());
            responseAction.setDy(requestAction.getDy());
            responseAction.setTurn(gameplayService.getMap().getTurn() + 1);
            responseActions.getActions().add(responseAction);
        }
        String msg = gameplayService.registerMove(teamId, actions);
        if (msg.equals(MoveMsgs.WAITING)) {
            isWaiting = true;
            try {
                while (isWaiting)
                    wait();
            } catch (Exception e) {
                logger.info("PROCON:TIMEOUT:ERROR:" + e.getMessage());
            }
            logger.info("PROCON:MOVE:SUCCESSFUL:TEAM_" + teamId);
            return responseActions;
        }
        if (msg.equals(MoveMsgs.ACCEPTED)) {
            isWaiting = false;
            notify();
            logger.info("PROCON:MOVE:SUCCESSFUL:TEAM_" + teamId);
            return responseActions;
        }
        throw new InvalidMoveException("PROCON:POST:/matches/" + teamId + "/move:" + msg);
    }
}
