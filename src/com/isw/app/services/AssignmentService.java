package com.isw.app.services;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Comparator;
import com.isw.app.models.Room;
import com.isw.app.models.Robot;
import com.isw.app.models.Coord;
import com.isw.app.enums.SectorType;
import com.isw.app.models.TargetPair;

public class AssignmentService {
  private final PathfindingService pathfindingService;

  public AssignmentService(PathfindingService pathfindingService) {
    this.pathfindingService = pathfindingService;
  }

  public Map<Robot, Coord> assignObjectives(List<Robot> robots, Room room) {
    Map<Robot, Coord> assignments = new HashMap<>();

    // Obtener todos los sectores sucios disponibles
    List<Coord> dirtyCoords = getDirtyCoords(room);
    List<Coord> availableTargets = new ArrayList<>(dirtyCoords);

    // Filtrar robots que necesitan recarga - no les asignamos objetivos de limpieza
    List<Robot> robotsForCleaning = robots.stream()
        .filter(robot -> !robot.needsRecharge() || robot.justRecharged())
        .collect(Collectors.toList());

    // Si quedan pocos objetivos, optimizar asignaciones
    if (shouldOptimizeForFewTargets(robotsForCleaning, availableTargets)) {
      return assignOptimizedObjectives(robotsForCleaning, availableTargets, room);
    }

    // Asignación normal para muchos objetivos
    for (Robot robot : robotsForCleaning) {
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

  private boolean shouldOptimizeForFewTargets(List<Robot> robots, List<Coord> targets) {
    // Optimizar cuando hay más robots que objetivos o cuando quedan pocos objetivos
    return targets.size() <= robots.size() || targets.size() <= 5;
  }

  private Map<Robot, Coord> assignOptimizedObjectives(List<Robot> robots, List<Coord> targets, Room room) {
    Map<Robot, Coord> assignments = new HashMap<>();

    if (targets.isEmpty()) {
      return assignments;
    }

    // Calcular distancias de todos los robots a todos los objetivos
    Map<Robot, Map<Coord, Integer>> robotDistances = calculateAllDistances(robots, targets, room);

    // Seleccionar los robots más eficientes para cada objetivo
    List<TargetPair> optimalPairs = findOptimalAssignments(robotDistances, targets);

    // Limitar a los robots más cercanos necesarios
    int robotsNeeded = Math.min(targets.size(), robots.size());
    for (int i = 0; i < robotsNeeded && i < optimalPairs.size(); i++) {
      TargetPair pair = optimalPairs.get(i);
      assignments.put(pair.getRobot(), pair.getTarget());
    }

    return assignments;
  }

  private Map<Robot, Map<Coord, Integer>> calculateAllDistances(List<Robot> robots, List<Coord> targets, Room room) {
    Map<Robot, Map<Coord, Integer>> distances = new HashMap<>();

    for (Robot robot : robots) {
      Map<Coord, Integer> robotDistances = new HashMap<>();
      for (Coord target : targets) {
        List<Coord> path = pathfindingService.findShortestPath(robot.getCoord(), target, room);
        int distance = path != null ? path.size() : Integer.MAX_VALUE;
        robotDistances.put(target, distance);
      }
      distances.put(robot, robotDistances);
    }

    return distances;
  }

  private List<TargetPair> findOptimalAssignments(Map<Robot, Map<Coord, Integer>> robotDistances,
      List<Coord> targets) {
    List<TargetPair> allPairs = new ArrayList<>();

    // Crear todas las combinaciones robot-objetivo posibles
    for (Robot robot : robotDistances.keySet()) {
      for (Coord target : targets) {
        int distance = robotDistances.get(robot).get(target);
        if (distance != Integer.MAX_VALUE) {
          allPairs.add(new TargetPair(robot, target, distance));
        }
      }
    }

    // Ordenar por distancia (los más cercanos primero)
    allPairs.sort(Comparator.comparing(TargetPair::getDistance));

    // Asignación greedy: tomar las mejores asignaciones sin repetir robots ni
    // objetivos
    List<TargetPair> selectedPairs = new ArrayList<>();
    List<Robot> assignedRobots = new ArrayList<>();
    List<Coord> assignedTargets = new ArrayList<>();

    for (TargetPair pair : allPairs) {
      if (!assignedRobots.contains(pair.getRobot()) && !assignedTargets.contains(pair.getTarget())) {
        selectedPairs.add(pair);
        assignedRobots.add(pair.getRobot());
        assignedTargets.add(pair.getTarget());

        // Si ya asignamos todos los objetivos, terminamos
        if (assignedTargets.size() >= targets.size()) {
          break;
        }
      }
    }

    return selectedPairs;
  }

  private Coord findBestTargetForRobot(Robot robot, List<Coord> availableTargets, Room room) {
    Coord bestTarget = null;
    int shortestDistance = Integer.MAX_VALUE;

    for (Coord target : availableTargets) {
      List<Coord> path = pathfindingService.findShortestPath(robot.getCoord(), target, room);
      if (path != null) {
        int distance = path.size();
        if (distance < shortestDistance) {
          shortestDistance = distance;
          bestTarget = target;
        }
      }
    }

    return bestTarget;
  }

  private List<Coord> getDirtyCoords(Room room) {
    return room.getAllCoords().stream()
        .filter(coord -> room.getSectorAt(coord).getType() == SectorType.DIRTY)
        .collect(Collectors.toList());
  }
}
