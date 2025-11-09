package com.isw.app.services;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import com.isw.app.models.Room;
import com.isw.app.models.Robot;
import com.isw.app.models.Coord;
import java.util.stream.Collectors;
import com.isw.app.enums.SectorType;
import com.isw.app.models.TargetPair;

public class AssignmentService {
  private final PathfindingService pathfindingService;
  private static final int OPTIMIZATION_THRESHOLD = 5;

  public AssignmentService(PathfindingService pathfindingService) {
    this.pathfindingService = pathfindingService;
  }

  public Map<Robot, Coord> assignObjectives(List<Robot> robots, Room room) {
    List<Coord> availableTargets = room.getCoordsByType(SectorType.DIRTY);

    List<Robot> robotsForCleaning = robots.stream()
        .filter(robot -> !robot.needsRecharge() || robot.justRecharged())
        .collect(Collectors.toList());

    return shouldOptimizeForFewTargets(robotsForCleaning.size(), availableTargets.size())
        ? assignOptimizedObjectives(robotsForCleaning, availableTargets, room)
        : assignNormalObjectives(robotsForCleaning, availableTargets, room);
  }

  private boolean shouldOptimizeForFewTargets(int robotCount, int targetCount) {
    return targetCount <= robotCount || targetCount <= OPTIMIZATION_THRESHOLD;
  }

  private Map<Robot, Coord> assignNormalObjectives(List<Robot> robots, List<Coord> targets, Room room) {
    Map<Robot, Coord> assignments = new HashMap<>();
    List<Coord> availableTargets = new ArrayList<>(targets);

    for (Robot robot : robots) {
      if (availableTargets.isEmpty()) break;

      Coord bestTarget = findBestTargetForRobot(robot, availableTargets, room);
      if (bestTarget != null) {
        assignments.put(robot, bestTarget);
        availableTargets.remove(bestTarget);
      }
    }

    return assignments;
  }

  private Map<Robot, Coord> assignOptimizedObjectives(List<Robot> robots, List<Coord> targets, Room room) {
    if (targets.isEmpty()) return new HashMap<>();

    List<TargetPair> allPairs = robots.stream()
        .flatMap(robot -> targets.stream()
            .map(target -> createTargetPair(robot, target, room))
            .filter(pair -> pair.getDistance() != Integer.MAX_VALUE))
        .sorted(Comparator.comparing(TargetPair::getDistance))
        .collect(Collectors.toList());

    return selectOptimalPairs(allPairs, targets.size());
  }

  private TargetPair createTargetPair(Robot robot, Coord target, Room room) {
    List<Coord> path = pathfindingService.findShortestPath(robot.getCoord(), target, room);
    int distance = path != null ? path.size() : Integer.MAX_VALUE;
    return new TargetPair(robot, target, distance);
  }

  private Map<Robot, Coord> selectOptimalPairs(List<TargetPair> allPairs, int maxTargets) {
    Map<Robot, Coord> assignments = new HashMap<>();
    List<Robot> assignedRobots = new ArrayList<>();
    List<Coord> assignedTargets = new ArrayList<>();

    for (TargetPair pair : allPairs) {
      if (!assignedRobots.contains(pair.getRobot()) && !assignedTargets.contains(pair.getTarget())) {
        assignments.put(pair.getRobot(), pair.getTarget());
        assignedRobots.add(pair.getRobot());
        assignedTargets.add(pair.getTarget());

        if (assignedTargets.size() >= maxTargets) break;
      }
    }

    return assignments;
  }

  private Coord findBestTargetForRobot(Robot robot, List<Coord> availableTargets, Room room) {
    return availableTargets.stream()
        .map(target -> createTargetPair(robot, target, room))
        .filter(pair -> pair.getDistance() != Integer.MAX_VALUE)
        .min(Comparator.comparing(TargetPair::getDistance))
        .map(TargetPair::getTarget)
        .orElse(null);
  }
}
