package com.isw.app.services;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.isw.app.models.Room;
import com.isw.app.models.Robot;
import com.isw.app.models.Coord;
import com.isw.app.models.Decision;
import com.isw.app.models.Movement;
import com.isw.app.enums.SectorType;

public class MovementService {
  private final PathfindingService pathfindingService;
  private final AssignmentService assignmentService;

  public MovementService() {
    this.pathfindingService = new PathfindingService();
    this.assignmentService = new AssignmentService(pathfindingService);
  }

  public List<Decision> calculateMovements(List<Robot> robots, Room room) {
    List<Decision> decisions = new ArrayList<>();
    List<Coord> reservedCoords = new ArrayList<>();
    
    // Asignar objetivos únicos a cada robot
    Map<Robot, Coord> assignments = assignmentService.assignObjectives(robots, room);

    for (Robot robot : robots) {
      Decision decision = calculateSingleRobotMovement(robot, room, reservedCoords, assignments);
      decisions.add(decision);
      
      if (decision.hasValidMovement()) {
        reservedCoords.add(decision.getTargetCoord());
      }
    }

    return decisions;
  }

  private Decision calculateSingleRobotMovement(Robot robot, Room room, List<Coord> reservedCoords, 
                                                Map<Robot, Coord> assignments) {
    Coord currentPos = robot.getCoord();
    SectorType currentType = room.getSectorAt(currentPos).getType();
    
    // Si está en una posición de recarga y acaba de llegar, salir inmediatamente
    if (currentType == SectorType.RECHARGE && robot.isAtRechargePosition()) {
      robot.clearRechargePosition();
      Coord exitMove = findExitFromRecharge(robot, room, reservedCoords, assignments);
      if (exitMove != null) {
        return new Decision(robot, new Movement(exitMove, 1000));
      }
    }

    // Obtener el objetivo asignado a este robot
    Coord assignedTarget = assignments.get(robot);
    
    // Si no tiene objetivo asignado, quedarse quieto
    if (assignedTarget == null) {
      return new Decision(robot, null);
    }

    // Moverse hacia el objetivo asignado
    Coord nextMove = findPathToTarget(robot, assignedTarget, room, reservedCoords);
    if (nextMove != null) {
      return new Decision(robot, new Movement(nextMove, 1000));
    }

    // Si no hay movimiento válido, quedarse quieto
    return new Decision(robot, null);
  }

  private Coord findExitFromRecharge(Robot robot, Room room, List<Coord> reservedCoords, 
                                     Map<Robot, Coord> assignments) {
    List<Coord> adjacentCoords = getValidAdjacentCoords(robot.getCoord(), room);
    
    // Si tiene un objetivo asignado, salir hacia él
    Coord assignedTarget = assignments.get(robot);
    if (assignedTarget != null) {
      return adjacentCoords.stream()
          .filter(coord -> !reservedCoords.contains(coord))
          .filter(coord -> room.getSectorAt(coord).isEmpty())
          .filter(coord -> room.getSectorAt(coord).getType() != SectorType.RECHARGE)
          .min((c1, c2) -> Integer.compare(c1.distanceTo(assignedTarget), c2.distanceTo(assignedTarget)))
          .orElse(null);
    }
    
    // Si no hay objetivo, salir a cualquier espacio válido
    return adjacentCoords.stream()
        .filter(coord -> !reservedCoords.contains(coord))
        .filter(coord -> room.getSectorAt(coord).isEmpty())
        .filter(coord -> room.getSectorAt(coord).getType() != SectorType.RECHARGE)
        .findFirst()
        .orElse(null);
  }

  private Coord findPathToTarget(Robot robot, Coord target, Room room, List<Coord> reservedCoords) {
    List<Coord> path = pathfindingService.findShortestPath(robot.getCoord(), target, room);
    
    if (path == null || path.isEmpty()) return null;

    Coord nextStep = path.get(0);
    
    // Si el siguiente paso está ocupado o reservado, buscar alternativa
    if (reservedCoords.contains(nextStep) || !room.getSectorAt(nextStep).isEmpty()) {
      return findAlternativeMove(robot, room, reservedCoords, target);
    }

    return nextStep;
  }

  private Coord findAlternativeMove(Robot robot, Room room, List<Coord> reservedCoords, Coord target) {
    List<Coord> adjacentCoords = getValidAdjacentCoords(robot.getCoord(), room);
    
    return adjacentCoords.stream()
        .filter(coord -> !reservedCoords.contains(coord))
        .filter(coord -> room.getSectorAt(coord).isEmpty())
        .min((c1, c2) -> Integer.compare(c1.distanceTo(target), c2.distanceTo(target)))
        .orElse(null);
  }

  private List<Coord> getValidAdjacentCoords(Coord current, Room room) {
    List<Coord> adjacent = new ArrayList<>();
    int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

    for (int[] dir : directions) {
      Coord newCoord = new Coord(current.getRow() + dir[0], current.getCol() + dir[1]);
      
      if (room.isValidCoord(newCoord)) {
        SectorType type = room.getSectorAt(newCoord).getType();
        if (type != SectorType.OBSTRUCTED && type != SectorType.TEMPORARY) {
          adjacent.add(newCoord);
        }
      }
    }

    return adjacent;
  }
}
