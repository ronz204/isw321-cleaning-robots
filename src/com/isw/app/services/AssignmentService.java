package com.isw.app.services;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Comparator;
import com.isw.app.models.Room;
import com.isw.app.models.Coord;
import com.isw.app.models.Robot;
import java.util.stream.Collectors;
import com.isw.app.enums.SectorType;
import com.isw.app.models.TargetPair;

public class AssignmentService {
  private final PathfindingService pathfindingService;

  public AssignmentService(PathfindingService pathfindingService) {
    this.pathfindingService = pathfindingService;
  }

  public Map<Robot, Coord> assignObjectives(List<Robot> robots, Room room) {
    Map<Robot, Coord> assignments = new HashMap<>();
    
    Map<Boolean, List<Robot>> partitioned = robots.stream()
        .collect(Collectors.partitioningBy(r -> r.shouldSeekRecharge(
            room.getDistanceToNearestRecharge(r.getCoord()))));
    
    List<Robot> needsRecharge = partitioned.get(true);
    List<Robot> canClean = partitioned.get(false);
    
    needsRecharge.sort(Comparator.comparing(Robot::getBattery));
    
    assignRechargeStations(needsRecharge, room, assignments);
    assignDirtySectors(canClean, room, assignments);
    
    return assignments;
  }

  private void assignRechargeStations(List<Robot> robots, Room room, Map<Robot, Coord> assignments) {
    List<Coord> rechargeStations = room.getRechargeCoords();
    if (rechargeStations.isEmpty()) return;
    
    for (Robot robot : robots) {
      // Asignar SIEMPRE la estación más cercana, sin importar si está ocupada
      Coord nearestRecharge = rechargeStations.stream()
          .min(Comparator.comparing(r -> robot.getCoord().distanceTo(r)))
          .orElse(null);
      
      if (nearestRecharge != null) {
        assignments.put(robot, nearestRecharge);
      }
    }
  }

  private void assignDirtySectors(List<Robot> robots, Room room, Map<Robot, Coord> assignments) {
    List<Coord> availableDirty = new ArrayList<>(room.getCoordsByType(SectorType.DIRTY));
    if (availableDirty.isEmpty()) return;
    
    List<TargetPair> allPairs = createTargetPairs(robots, availableDirty, room);
    allPairs.sort(Comparator.comparing(TargetPair::getDistance));
    
    assignOptimalPairs(allPairs, assignments);
    assignRemainingRobots(robots, availableDirty, assignments);
  }

  private List<TargetPair> createTargetPairs(List<Robot> robots, List<Coord> targets, Room room) {
    List<TargetPair> pairs = new ArrayList<>();
    
    for (Robot robot : robots) {
      for (Coord target : targets) {
        List<Coord> path = pathfindingService.findShortestPath(robot.getCoord(), target, room);
        int distance = path != null ? path.size() : Integer.MAX_VALUE;
        pairs.add(new TargetPair(robot, target, distance));
      }
    }
    
    return pairs;
  }

  private void assignOptimalPairs(List<TargetPair> pairs, Map<Robot, Coord> assignments) {
    Set<Robot> assignedRobots = new HashSet<>();
    Set<Coord> assignedTargets = new HashSet<>();
    
    for (TargetPair pair : pairs) {
      if (pair.distance == Integer.MAX_VALUE) continue;
      
      if (!assignedRobots.contains(pair.robot) && !assignedTargets.contains(pair.target)) {
        assignments.put(pair.robot, pair.target);
        assignedRobots.add(pair.robot);
        assignedTargets.add(pair.target);
      }
    }
  }

  private void assignRemainingRobots(List<Robot> robots, List<Coord> availableDirty, 
                                     Map<Robot, Coord> assignments) {
    robots.stream()
        .filter(robot -> !assignments.containsKey(robot))
        .forEach(robot -> {
          Coord nearestDirty = availableDirty.stream()
              .min(Comparator.comparing(d -> robot.getCoord().distanceTo(d)))
              .orElse(null);
          
          if (nearestDirty != null) {
            assignments.put(robot, nearestDirty);
          }
        });
  }
}
