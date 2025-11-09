package com.isw.app.services;

import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import com.isw.app.models.Room;
import com.isw.app.models.Coord;
import com.isw.app.models.Robot;
import java.util.stream.Collectors;
import com.isw.app.models.Decision;
import com.isw.app.models.Movement;
import com.isw.app.enums.SectorType;

public class MovementService {
  private static final int WAIT_DISTANCE = 2;
  private static final double DEFAULT_SCORE = 1000.0;
  
  private final PathfindingService pathfindingService;
  private final AssignmentService assignmentService;

  public MovementService() {
    this.pathfindingService = new PathfindingService();
    this.assignmentService = new AssignmentService(pathfindingService);
  }

  public List<Decision> calculateMovements(List<Robot> robots, Room room) {
    List<Coord> reservedCoords = new ArrayList<>();
    Map<Robot, Coord> assignments = assignmentService.assignObjectives(robots, room);

    return robots.stream()
        .map(robot -> {
          Decision decision = calculateSingleRobotMovement(robot, room, reservedCoords, assignments);
          if (decision.hasValidMovement()) {
            reservedCoords.add(decision.getTargetCoord());
          }
          return decision;
        })
        .collect(Collectors.toList());
  }

  private Decision calculateSingleRobotMovement(Robot robot, Room room, List<Coord> reservedCoords, 
                                                Map<Robot, Coord> assignments) {
    Coord currentPos = robot.getCoord();
    SectorType currentType = room.getSectorAt(currentPos).getType();
    
    // Salir de recarga si acaba de recargar
    if (currentType == SectorType.RECHARGE && robot.isAtRechargePosition()) {
      return createExitDecision(robot, room, reservedCoords, assignments);
    }

    Coord assignedTarget = assignments.get(robot);
    if (assignedTarget == null) {
      return new Decision(robot, null);
    }

    // Esperar si la estación de recarga está ocupada
    if (shouldWaitForRecharge(robot, room, assignedTarget, currentPos)) {
      return new Decision(robot, null);
    }

    // Moverse hacia el objetivo
    Coord nextMove = findPathToTarget(robot, assignedTarget, room, reservedCoords);
    return createMovementDecision(robot, nextMove);
  }

  private Decision createExitDecision(Robot robot, Room room, List<Coord> reservedCoords, 
                                      Map<Robot, Coord> assignments) {
    robot.clearRechargePosition();
    Coord exitMove = findExitFromRecharge(robot, room, reservedCoords, assignments);
    return createMovementDecision(robot, exitMove);
  }

  private boolean shouldWaitForRecharge(Robot robot, Room room, Coord target, Coord currentPos) {
    return robot.needsRecharge() && 
           room.getSectorAt(target).getType() == SectorType.RECHARGE &&
           !room.getSectorAt(target).isEmpty() &&
           currentPos.distanceTo(target) <= WAIT_DISTANCE;
  }

  private Decision createMovementDecision(Robot robot, Coord nextMove) {
    return new Decision(robot, nextMove != null ? new Movement(nextMove, DEFAULT_SCORE) : null);
  }

  private Coord findExitFromRecharge(Robot robot, Room room, List<Coord> reservedCoords, 
                                     Map<Robot, Coord> assignments) {
    List<Coord> adjacentCoords = getValidAdjacentCoords(robot.getCoord(), room);
    Coord assignedTarget = assignments.get(robot);
    
    return adjacentCoords.stream()
        .filter(coord -> !reservedCoords.contains(coord))
        .filter(coord -> room.getSectorAt(coord).isEmpty())
        .filter(coord -> room.getSectorAt(coord).getType() != SectorType.RECHARGE)
        .min((c1, c2) -> compareByTarget(c1, c2, assignedTarget))
        .orElse(null);
  }

  private int compareByTarget(Coord c1, Coord c2, Coord target) {
    return target != null ? 
           Integer.compare(c1.distanceTo(target), c2.distanceTo(target)) : 0;
  }

  private Coord findPathToTarget(Robot robot, Coord target, Room room, List<Coord> reservedCoords) {
    List<Coord> path = pathfindingService.findShortestPath(robot.getCoord(), target, room);
    if (path == null || path.isEmpty()) return null;

    Coord nextStep = path.get(0);
    
    if (isBlocked(nextStep, room, reservedCoords)) {
      return findAlternativeMove(robot, room, reservedCoords, target);
    }

    return nextStep;
  }

  private boolean isBlocked(Coord coord, Room room, List<Coord> reservedCoords) {
    return reservedCoords.contains(coord) || !room.getSectorAt(coord).isEmpty();
  }

  private Coord findAlternativeMove(Robot robot, Room room, List<Coord> reservedCoords, Coord target) {
    return getValidAdjacentCoords(robot.getCoord(), room).stream()
        .filter(coord -> !reservedCoords.contains(coord))
        .filter(coord -> room.getSectorAt(coord).isEmpty())
        .min(Comparator.comparing(c -> c.distanceTo(target)))
        .orElse(null);
  }

  private List<Coord> getValidAdjacentCoords(Coord current, Room room) {
    int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
    
    return Arrays.stream(directions)
        .map(dir -> new Coord(current.getRow() + dir[0], current.getCol() + dir[1]))
        .filter(room::isValidCoord)
        .filter(coord -> {
          SectorType type = room.getSectorAt(coord).getType();
          return type != SectorType.OBSTRUCTED && type != SectorType.TEMPORARY;
        })
        .collect(Collectors.toList());
  }
}
