package com.isw.app.services;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import com.isw.app.models.Room;
import com.isw.app.models.Robot;
import com.isw.app.models.Coord;
import com.isw.app.models.Sector;
import com.isw.app.enums.SectorType;

public class EvaluationService {
  private final PathfindingService pathfindingService;

  public EvaluationService(PathfindingService pathfindingService) {
    this.pathfindingService = pathfindingService;
  }

  public double calculateMoveScore(Robot robot, Coord targetCoord, List<Robot> allRobots, Room room) {
    return calculateMoveScore(robot, targetCoord, allRobots, room, new HashMap<>());
  }

  public double calculateMoveScore(Robot robot, Coord targetCoord, List<Robot> allRobots,
      Room room, Map<Robot, Coord> robotObjectives) {
    Sector targetSector = room.getSectorAt(targetCoord);

    if (targetSector.getType() == SectorType.DIRTY)
      return 5000;
    if (targetSector.getType() == SectorType.RECHARGE && robot.needsRecharge())
      return 10000;

    return calculatePathScore(targetCoord, room, robot, robotObjectives) +
        calculateRobotInteractionScore(robot, targetCoord, allRobots, room);
  }

  private double calculatePathScore(Coord targetCoord, Room room, Robot robot, Map<Robot, Coord> robotObjectives) {
    Coord assignedObjective = robotObjectives.get(robot);

    if (assignedObjective != null) {
      List<Coord> pathToObjective = pathfindingService.findShortestPath(targetCoord, assignedObjective, room);
      return pathToObjective != null ? 2000.0 / (pathToObjective.size() + 1) : -100;
    }

    List<Coord> dirtySectors = room.getCoordsByType(SectorType.DIRTY);
    if (dirtySectors.isEmpty())
      return 0;

    return dirtySectors.stream()
        .mapToDouble(dirty -> {
          List<Coord> path = pathfindingService.findShortestPath(targetCoord, dirty, room);
          return path != null && !path.isEmpty() ? 1000.0 / (path.size() + 1) : 0;
        })
        .average()
        .orElse(0);
  }

  private double calculateRobotInteractionScore(Robot robot, Coord targetCoord, List<Robot> allRobots, Room room) {
    double score = 0;

    if (robot.justRecharged()) {
      List<Coord> rechargeStations = room.getRechargeCoords();
      if (!rechargeStations.isEmpty()) {
        Coord nearestRecharge = findNearestCoord(targetCoord, rechargeStations);
        score += targetCoord.distanceTo(nearestRecharge) * 200;
      }
    }

    long nearbyRobots = allRobots.stream()
        .filter(r -> targetCoord.distanceTo(r.getCoord()) <= 1)
        .count();
    score -= nearbyRobots * 300;

    return score;
  }

  private Coord findNearestCoord(Coord target, List<Coord> coords) {
    return coords.stream()
        .min((c1, c2) -> Integer.compare(target.distanceTo(c1), target.distanceTo(c2)))
        .orElse(null);
  }
}
