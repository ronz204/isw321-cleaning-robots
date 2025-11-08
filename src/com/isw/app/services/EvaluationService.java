package com.isw.app.services;

import java.util.List;
import java.util.Map;
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

    // Immediate high-value targets
    if (targetSector.getType() == SectorType.DIRTY) {
      return 5000;
    }

    if (targetSector.getType() == SectorType.RECHARGE && robot.needsRecharge()) {
      return 10000;
    }

    double score = calculatePathScore(targetCoord, room, robot, robotObjectives);
    score += calculateRobotInteractionScore(robot, targetCoord, allRobots, room);

    return score;
  }

  private double calculatePathScore(Coord targetCoord, Room room, Robot robot, 
      Map<Robot, Coord> robotObjectives) {
    
    // Si el robot tiene un objetivo asignado, priorizar moverse hacia él
    Coord assignedObjective = robotObjectives.get(robot);
    if (assignedObjective != null) {
      List<Coord> pathToObjective = pathfindingService.findShortestPath(targetCoord, assignedObjective, room);
      if (pathToObjective != null) {
        // Mayor score si nos acerca al objetivo asignado
        return 2000.0 / (pathToObjective.size() + 1);
      }
    }

    // Fallback al comportamiento original si no hay objetivo asignado
    List<Coord> dirtySectors = getDirtySectors(room);
    if (dirtySectors.isEmpty())
      return 0;

    double totalScore = 0;
    int validPaths = 0;

    for (Coord dirty : dirtySectors) {
      List<Coord> path = pathfindingService.findShortestPath(targetCoord, dirty, room);
      if (path != null && !path.isEmpty()) {
        totalScore += 1000.0 / (path.size() + 1);
        validPaths++;
      }
    }

    return validPaths > 0 ? totalScore / validPaths : 0;
  }

  private double calculateRobotInteractionScore(Robot robot, Coord targetCoord, List<Robot> allRobots, Room room) {
    double score = 0;

    // Encourage moving away after recharging
    if (robot.justRecharged()) {
      List<Coord> rechargeStations = room.getRechargeCoords();
      if (!rechargeStations.isEmpty()) {
        Coord nearestRecharge = findNearestCoord(targetCoord, rechargeStations);
        score += targetCoord.distanceTo(nearestRecharge) * 200;
      }
    }

    // Penalize nearby robots
    int nearbyRobots = countNearbyRobots(targetCoord, allRobots);
    score -= nearbyRobots * 300;

    return score;
  }

  private Coord findNearestCoord(Coord target, List<Coord> coords) {
    return coords.stream()
        .min((c1, c2) -> Integer.compare(target.distanceTo(c1), target.distanceTo(c2)))
        .orElse(null);
  }

  private int countNearbyRobots(Coord center, List<Robot> robots) {
    return (int) robots.stream()
        .filter(robot -> center.distanceTo(robot.getCoord()) <= 1)
        .count();
  }

  private List<Coord> getDirtySectors(Room room) {
    return room.getAllCoords().stream()
        .filter(coord -> room.getSectorAt(coord).getType() == SectorType.DIRTY)
        .toList();
  }
}
