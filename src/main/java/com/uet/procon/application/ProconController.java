package com.uet.procon.application;

import com.uet.procon.common.entity.MapImpl;
import com.uet.procon.common.enums.MoveMsgs;
import com.uet.procon.common.exception.InvalidMoveException;
import com.uet.procon.common.exception.ResourceNotFoundException;
import com.uet.procon.common.model.*;
import com.uet.procon.common.util.LoggingUtil;
import com.uet.procon.worker.repository.MapRepository;
import com.uet.procon.worker.service.GameplayService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/api/")
public class ProconController {

    private Logger logger = LoggingUtil.createLogger(ProconController.class);

    @Autowired
    public MapRepository mapRepository;

    @Autowired
    public GameplayService gameplayService;

    @Value("${com.uet.procon.game.mapid}")
    private String MAP_ID;

    @Value("${com.uet.procon.game.turn}")
    private int TURN;

    // Get maps
    @GetMapping("/maps")
    public List<MapImpl> getMaps() {
        logger.info("PROCON:GET:/maps");
        return mapRepository.findAll();
    }

    // Get map ids
    @GetMapping("maps/mapIds")
    public List<String> getMapIds() {
        logger.info("PROCON:GET:/maps/mapIds");
        List<MapImpl> maps = mapRepository.findAll();
        List<String> mapIDs = new ArrayList<>();
        for (MapImpl map : maps)
            mapIDs.add(map.getId());
        return mapIDs;
    }

    // Get map with id
    @GetMapping("/maps/{mapId}")
    public MapImpl getMap(@PathVariable(value = "mapId") String mapId) throws Exception {
        logger.info("PROCON:GET:/maps/" + mapId);
        MapImpl map = mapRepository.findById(mapId)
                .orElseThrow(() -> new ResourceNotFoundException("PROCON:GET:/maps/" + mapId + ":NOT_FOUND"));
        return map;
    }

    // Create map
    @PostMapping("/maps/createMap")
    public MapImpl createMap(@RequestBody MapImpl map) {
        logger.info("PROCON:POST:/maps/createMap");
        return mapRepository.save(map);
    }

    // Start game
    @GetMapping("/game/startGame")
    public MapImpl startGame() throws Exception {
        logger.info("PROCON:GET:/game/startGame");
        MapImpl map = getMap(MAP_ID);
        gameplayService.setGame(map, TURN);
        return map;
    }

    // Finish game
    @GetMapping("/game/finishGame")
    public MapImpl finishedGame() throws Exception {
        logger.info("PROCON:GET:/game/finishedGame");
        gameplayService.clearGame();
        return gameplayService.getMap();
    }

    // Get game's map
    @GetMapping("/game")
    public MapImpl getGameMap() throws Exception {
        logger.info("PROCON:GET:/game");
        return gameplayService.getMap();
    }

    // Player's move
    @PostMapping("/game/move/{teamId}")
    public MapImpl move(@PathVariable("teamId") int teamId, @RequestBody ClientActionListModel actions) throws Exception {
        logger.info("PROCON:POST:/game/move/" + teamId);
        String msg = gameplayService.registerMove(teamId, actions);
        if (!msg.equals(MoveMsgs.ACCEPTED))
            throw new InvalidMoveException("PROCON:POST:/game/move/" + teamId + ":" + msg);
        return gameplayService.getMap();
    }
}
