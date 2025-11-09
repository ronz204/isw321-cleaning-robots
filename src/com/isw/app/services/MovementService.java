package com.isw.app.services;

import java.util.Map;
import java.util.List;
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
  private static final double DEFAULT_SCORE = 1000.0;

  private final PathfindingService pathfindingService;
  private final AssignmentService assignmentService;
  private final NavigationService navigationService;
  private final CoordinateService coordinateService;

  public MovementService() {
    this.coordinateService = new CoordinateService();
    this.navigationService = new NavigationService(coordinateService);
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

    if (currentType == SectorType.RECHARGE && robot.isAtRechargePosition()) {
      return createExitDecision(robot, room, reservedCoords, assignments);
    }

    Coord assignedTarget = assignments.get(robot);
    if (assignedTarget == null) {
      return new Decision(robot, null);
    }

    if (navigationService.isRechargeOccupied(room, assignedTarget)) {
      return new Decision(robot, null);
    }

    Coord nextMove = findNextMove(robot, assignedTarget, room, reservedCoords);
    return new Decision(robot, nextMove != null ? new Movement(nextMove, DEFAULT_SCORE) : null);
  }

  private Decision createExitDecision(Robot robot, Room room, List<Coord> reservedCoords,
      Map<Robot, Coord> assignments) {
    robot.clearRechargePosition();
    Coord exitMove = findExitMove(robot, room, reservedCoords, assignments.get(robot));
    return new Decision(robot, exitMove != null ? new Movement(exitMove, DEFAULT_SCORE) : null);
  }

  private Coord findExitMove(Robot robot, Room room, List<Coord> reservedCoords, Coord target) {
    return navigationService.getUnblockedAdjacentCoords(robot.getCoord(), room, reservedCoords).stream()
        .filter(coord -> room.getSectorAt(coord).getType() != SectorType.RECHARGE)
        .min(Comparator.comparing(c -> target != null ? coordinateService.calculateDistance(c, target) : 0))
        .orElse(null);
  }

  private Coord findNextMove(Robot robot, Coord target, Room room, List<Coord> reservedCoords) {
    boolean isGoingToDirty = room.getSectorAt(target).getType() == SectorType.DIRTY;
    List<Coord> path = pathfindingService.findShortestPath(
        robot.getCoord(), target, room, isGoingToDirty);

    if (path == null || path.isEmpty())
      return null;

    Coord nextStep = path.get(0);

    if (navigationService.isBlocked(nextStep, room, reservedCoords)) {
      return findAlternativeMove(robot, target, room, reservedCoords, isGoingToDirty);
    }

    return nextStep;
  }

  private Coord findAlternativeMove(Robot robot, Coord target, Room room,
      List<Coord> reservedCoords, boolean allowRechargeTraversal) {
    return navigationService.getUnblockedAdjacentCoords(robot.getCoord(), room, reservedCoords).stream()
        .min(Comparator.comparingInt(coord -> {
          List<Coord> pathFromCoord = pathfindingService.findShortestPath(
              coord, target, room, allowRechargeTraversal);
          return pathFromCoord != null ? pathFromCoord.size() + 1 : Integer.MAX_VALUE;
        }))
        .orElse(null);
  }
}
