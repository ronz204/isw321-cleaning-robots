package com.isw.app.services;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import com.isw.app.models.Room;
import com.isw.app.models.Robot;
import com.isw.app.models.Coord;
import com.isw.app.models.Sector;
import com.isw.app.enums.SectorType;
import com.isw.app.models.Movement;
import com.isw.app.models.Decision;

public class MovementService {
  private final PathfindingService pathfindingService;
  private final EvaluationService evaluationService;

  public MovementService() {
    this.pathfindingService = new PathfindingService();
    this.evaluationService = new EvaluationService(pathfindingService);
  }

  public List<Decision> calculateMovements(List<Robot> robots, Room room) {
    List<Decision> decisions = new ArrayList<>();

    for (Robot robot : prioritizeRobots(robots, room)) {
      evaluateRechargeNeeds(robot, room);
      decisions.add(calculateBestMovement(robot, robots, room));
    }

    return decisions;
  }

  private void evaluateRechargeNeeds(Robot robot, Room room) {
    int distanceToRecharge = room.getDistanceToNearestRecharge(robot.getCoord());
    robot.setNeedsRecharge(robot.shouldSeekRecharge(distanceToRecharge));
  }

  private Decision calculateBestMovement(Robot robot, List<Robot> allRobots, Room room) {
    List<Coord> adjacentCoords = getAdjacentCoords(robot.getCoord(), room);

    if (robot.needsRecharge() && !robot.justRecharged()) {
      Movement rechargeMove = findBestRechargeMove(robot, adjacentCoords, room);
      if (rechargeMove != null) {
        return new Decision(robot, rechargeMove);
      }
    }

    return new Decision(robot, findBestMove(robot, adjacentCoords, allRobots, room));
  }

  private Movement findBestRechargeMove(Robot robot, List<Coord> adjacentCoords, Room room) {
    List<Coord> rechargeStations = room.getRechargeCoords();
    if (rechargeStations.isEmpty())
      return null;

    Movement bestMove = null;
    double bestScore = Double.NEGATIVE_INFINITY;

    for (Coord adjacent : adjacentCoords) {
      if (!isValidMove(adjacent, room))
        continue;

      if (room.getSectorAt(adjacent).getType() == SectorType.RECHARGE) {
        return new Movement(adjacent, 10000);
      }

      List<Coord> path = pathfindingService.findShortestPath(adjacent, rechargeStations.get(0), room);
      if (path != null) {
        double score = 1000.0 / (path.size() + 1);
        if (score > bestScore) {
          bestScore = score;
          bestMove = new Movement(adjacent, score);
        }
      }
    }

    return bestMove;
  }

  private Movement findBestMove(Robot robot, List<Coord> adjacentCoords, List<Robot> allRobots, Room room) {
    Movement bestMove = null;
    double bestScore = Double.NEGATIVE_INFINITY;

    for (Coord adjacent : adjacentCoords) {
      if (!isValidMove(adjacent, room))
        continue;

      double score = evaluationService.calculateMoveScore(robot, adjacent, allRobots, room);
      if (score > bestScore) {
        bestScore = score;
        bestMove = new Movement(adjacent, score);
      }
    }

    return bestMove;
  }

  private List<Coord> getAdjacentCoords(Coord current, Room room) {
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

  private boolean isValidMove(Coord coord, Room room) {
    Sector sector = room.getSectorAt(coord);
    return (sector.getType() == SectorType.CLEAN ||
        sector.getType() == SectorType.DIRTY ||
        sector.getType() == SectorType.RECHARGE) &&
        sector.isEmpty();
  }

  private List<Robot> prioritizeRobots(List<Robot> robots, Room room) {
    return robots.stream()
        .sorted(Comparator.comparing((Robot robot) -> robot.needsRecharge() && !robot.justRecharged() ? 0 : 1)
            .thenComparing((Robot robot) -> robot.justRecharged() ? 0 : 1)
            .thenComparing((Robot robot) -> -robot.getBattery()))
        .toList();
  }
}
