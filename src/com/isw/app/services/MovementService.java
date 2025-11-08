package com.isw.app.services;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import com.isw.app.models.Room;
import com.isw.app.models.Robot;
import com.isw.app.models.Coord;
import com.isw.app.models.Sector;
import com.isw.app.models.Movement;
import com.isw.app.models.Decision;
import com.isw.app.enums.SectorType;

public class MovementService {
  private final PathfindingService pathfindingService;
  private final EvaluationService evaluationService;
  private final AssignmentService assignmentService;

  public MovementService() {
    this.pathfindingService = new PathfindingService();
    this.evaluationService = new EvaluationService(pathfindingService);
    this.assignmentService = new AssignmentService(pathfindingService);
  }

  public List<Decision> calculateMovements(List<Robot> robots, Room room) {
    List<Decision> decisions = new ArrayList<>();
    List<Coord> reservedCoords = new ArrayList<>();

    Map<Robot, Coord> robotObjectives = assignmentService.assignObjectives(robots, room);

    for (Robot robot : prioritizeRobots(robots, room)) {
      evaluateRechargeNeeds(robot, room);
      Decision decision = calculateBestMovement(robot, robots, room, reservedCoords, robotObjectives);
      decisions.add(decision);

      if (decision.hasValidMovement()) {
        reservedCoords.add(decision.getTargetCoord());
      }
    }

    return decisions;
  }

  private void evaluateRechargeNeeds(Robot robot, Room room) {
    int distanceToRecharge = room.getDistanceToNearestRecharge(robot.getCoord());
    robot.setNeedsRecharge(robot.shouldSeekRecharge(distanceToRecharge));
  }

  private Decision calculateBestMovement(Robot robot, List<Robot> allRobots, Room room,
      List<Coord> reservedCoords, Map<Robot, Coord> robotObjectives) {

    List<Coord> adjacentCoords = getValidAdjacentCoords(robot.getCoord(), room);

    if (robot.needsRecharge() && !robot.justRecharged()) {
      Movement rechargeMove = findBestRechargeMove(robot, adjacentCoords, room, reservedCoords);
      if (rechargeMove != null)
        return new Decision(robot, rechargeMove);
    }

    if (!robotObjectives.containsKey(robot)) {
      return new Decision(robot, null);
    }

    Movement bestMove = findBestMove(robot, adjacentCoords, allRobots, room, reservedCoords, robotObjectives);
    return new Decision(robot, bestMove);
  }

  private List<Coord> getValidAdjacentCoords(Coord current, Room room) {
    List<Coord> adjacent = new ArrayList<>();
    int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

    for (int[] dir : directions) {
      Coord newCoord = new Coord(current.getRow() + dir[0], current.getCol() + dir[1]);
      if (room.isValidCoord(newCoord)) {
        adjacent.add(newCoord);
      }
    }
    return adjacent;
  }

  private Movement findBestRechargeMove(Robot robot, List<Coord> adjacentCoords, Room room,
      List<Coord> reservedCoords) {
    List<Coord> rechargeStations = room.getRechargeCoords();
    if (rechargeStations.isEmpty())
      return null;

    Movement bestMove = null;
    double bestScore = Double.NEGATIVE_INFINITY;

    for (Coord adjacent : adjacentCoords) {
      if (!isValidMove(adjacent, room, reservedCoords))
        continue;

      if (room.getSectorAt(adjacent).getType() == SectorType.RECHARGE) {
        return new Movement(adjacent, 10000);
      }

      Coord closestStation = rechargeStations.stream()
          .min((s1, s2) -> Integer.compare(adjacent.distanceTo(s1), adjacent.distanceTo(s2)))
          .orElse(null);

      if (closestStation != null) {
        List<Coord> path = pathfindingService.findShortestPath(adjacent, closestStation, room);
        if (path != null) {
          double score = 1000.0 / (path.size() + 1);
          if (score > bestScore) {
            bestScore = score;
            bestMove = new Movement(adjacent, score);
          }
        }
      }
    }

    return bestMove;
  }

  private Movement findBestMove(Robot robot, List<Coord> adjacentCoords, List<Robot> allRobots,
      Room room, List<Coord> reservedCoords, Map<Robot, Coord> robotObjectives) {

    return adjacentCoords.stream()
        .filter(coord -> isValidMove(coord, room, reservedCoords))
        .map(coord -> {
          double score = evaluationService.calculateMoveScore(robot, coord, allRobots, room, robotObjectives);
          return new Movement(coord, score);
        })
        .max((m1, m2) -> Double.compare(m1.getScore(), m2.getScore()))
        .orElse(null);
  }

  private boolean isValidMove(Coord coord, Room room, List<Coord> reservedCoords) {
    Sector sector = room.getSectorAt(coord);
    return sector.isEmpty() && sector.isNavigable() && !reservedCoords.contains(coord);
  }

  private List<Robot> prioritizeRobots(List<Robot> robots, Room room) {
    return robots.stream()
        .sorted(Comparator.comparing((Robot robot) -> robot.needsRecharge() && !robot.justRecharged() ? 0 : 1)
            .thenComparing((Robot robot) -> robot.justRecharged() ? 0 : 1)
            .thenComparing((Robot robot) -> -robot.getBattery()))
        .toList();
  }
}
