package com.isw.app.services;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
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
  private final AssignmentService assignmentService; // Nuevo servicio

  public MovementService() {
    this.pathfindingService = new PathfindingService();
    this.evaluationService = new EvaluationService(pathfindingService);
    this.assignmentService = new AssignmentService(pathfindingService); // Inicializar
  }

  public List<Decision> calculateMovements(List<Robot> robots, Room room) {
    List<Decision> decisions = new ArrayList<>();
    List<Coord> reservedCoords = new ArrayList<>();
    
    // Asignar objetivos únicos a cada robot
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
    List<Coord> adjacentCoords = getAdjacentCoords(robot.getCoord(), room);

    if (robot.needsRecharge() && !robot.justRecharged()) {
      Movement rechargeMove = findBestRechargeMove(robot, adjacentCoords, room, reservedCoords);
      if (rechargeMove != null) {
        return new Decision(robot, rechargeMove);
      }
    }

    // Si el robot no tiene objetivo asignado, quedarse quieto (optimización)
    if (!robotObjectives.containsKey(robot)) {
      return new Decision(robot, null); // No movement
    }

    Movement bestMove = findBestMove(robot, adjacentCoords, allRobots, room, reservedCoords, robotObjectives);
    return new Decision(robot, bestMove);
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

      // Find closest recharge station
      Coord closestStation = findClosestRechargeStation(adjacent, rechargeStations);
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

  private Coord findClosestRechargeStation(Coord from, List<Coord> rechargeStations) {
    return rechargeStations.stream()
        .min((s1, s2) -> Integer.compare(from.distanceTo(s1), from.distanceTo(s2)))
        .orElse(null);
  }

  private Movement findBestMove(Robot robot, List<Coord> adjacentCoords, List<Robot> allRobots, 
      Room room, List<Coord> reservedCoords, Map<Robot, Coord> robotObjectives) {
    Movement bestMove = null;
    double bestScore = Double.NEGATIVE_INFINITY;

    for (Coord adjacent : adjacentCoords) {
      if (!isValidMove(adjacent, room, reservedCoords))
        continue;

      double score = evaluationService.calculateMoveScore(robot, adjacent, allRobots, room, robotObjectives);
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

  private boolean isValidMove(Coord coord, Room room, List<Coord> reservedCoords) {
    Sector sector = room.getSectorAt(coord);

    // Check if sector is navigable and empty
    boolean sectorValid = (sector.getType() == SectorType.CLEAN ||
        sector.getType() == SectorType.DIRTY ||
        sector.getType() == SectorType.RECHARGE) &&
        sector.isEmpty();

    // Check if coordinate is not already reserved by another robot in this turn
    boolean notReserved = !reservedCoords.contains(coord);

    return sectorValid && notReserved;
  }

  private List<Robot> prioritizeRobots(List<Robot> robots, Room room) {
    return robots.stream()
        .sorted(Comparator.comparing((Robot robot) -> robot.needsRecharge() && !robot.justRecharged() ? 0 : 1)
            .thenComparing((Robot robot) -> robot.justRecharged() ? 0 : 1)
            .thenComparing((Robot robot) -> -robot.getBattery()))
        .toList();
  }
}
