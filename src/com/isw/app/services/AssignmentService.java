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
      if (availableTargets.isEmpty())
        break;

      Coord bestTarget = findBestTargetForRobot(robot, availableTargets, room);
      if (bestTarget != null) {
        assignments.put(robot, bestTarget);
        availableTargets.remove(bestTarget);
      }
    }

    return assignments;
  }

  private Map<Robot, Coord> assignOptimizedObjectives(List<Robot> robots, List<Coord> targets, Room room) {
    if (targets.isEmpty())
      return new HashMap<>();

    Map<Robot, Map<Coord, Integer>> robotDistances = calculateAllDistances(robots, targets, room);
    List<TargetPair> optimalPairs = findOptimalAssignments(robotDistances, targets);

    return optimalPairs.stream()
        .limit(Math.min(targets.size(), robots.size()))
        .collect(Collectors.toMap(
            TargetPair::getRobot,
            TargetPair::getTarget,
            (existing, replacement) -> existing
        ));
  }

  private Map<Robot, Map<Coord, Integer>> calculateAllDistances(List<Robot> robots, List<Coord> targets, Room room) {
    return robots.stream()
        .collect(Collectors.toMap(
            robot -> robot,
            robot -> targets.stream()
                .collect(Collectors.toMap(
                    target -> target,
                    target -> {
                      List<Coord> path = pathfindingService.findShortestPath(robot.getCoord(), target, room);
                      return path != null ? path.size() : Integer.MAX_VALUE;
                    }))));
  }

  private List<TargetPair> findOptimalAssignments(Map<Robot, Map<Coord, Integer>> robotDistances, List<Coord> targets) {
    List<TargetPair> allPairs = robotDistances.entrySet().stream()
        .flatMap(robotEntry -> robotEntry.getValue().entrySet().stream()
            .filter(distanceEntry -> distanceEntry.getValue() != Integer.MAX_VALUE)
            .map(distanceEntry -> new TargetPair(
                robotEntry.getKey(),
                distanceEntry.getKey(),
                distanceEntry.getValue())))
        .sorted(Comparator.comparing(TargetPair::getDistance))
        .collect(Collectors.toList());

    List<TargetPair> selectedPairs = new ArrayList<>();
    List<Robot> assignedRobots = new ArrayList<>();
    List<Coord> assignedTargets = new ArrayList<>();

    for (TargetPair pair : allPairs) {
      if (!assignedRobots.contains(pair.getRobot()) && !assignedTargets.contains(pair.getTarget())) {
        selectedPairs.add(pair);
        assignedRobots.add(pair.getRobot());
        assignedTargets.add(pair.getTarget());

        if (assignedTargets.size() >= targets.size())
          break;
      }
    }

    return selectedPairs;
  }

  private Coord findBestTargetForRobot(Robot robot, List<Coord> availableTargets, Room room) {
    return availableTargets.stream()
        .map(target -> {
          List<Coord> path = pathfindingService.findShortestPath(robot.getCoord(), target, room);
          return new TargetPair(robot, target, path != null ? path.size() : Integer.MAX_VALUE);
        })
        .filter(pair -> pair.getDistance() != Integer.MAX_VALUE)
        .min(Comparator.comparing(TargetPair::getDistance))
        .map(TargetPair::getTarget)
        .orElse(null);
  }
}
