package com.isw.app.services;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
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

  /**
   * Asigna un objetivo único a cada robot para evitar que varios vayan al mismo DIRTY
   */
  public Map<Robot, Coord> assignObjectives(List<Robot> robots, Room room) {
    Map<Robot, Coord> assignments = new HashMap<>();
    
    // Separar robots por prioridad
    List<Robot> needsRecharge = new ArrayList<>();
    List<Robot> canClean = new ArrayList<>();
    
    for (Robot robot : robots) {
      int distanceToRecharge = room.getDistanceToNearestRecharge(robot.getCoord());
      if (robot.shouldSeekRecharge(distanceToRecharge)) {
        needsRecharge.add(robot);
      } else {
        canClean.add(robot);
      }
    }
    
    // Asignar recargas
    assignRechargeStations(needsRecharge, room, assignments);
    
    // Asignar sectores sucios
    assignDirtySectors(canClean, room, assignments);
    
    return assignments;
  }

  private void assignRechargeStations(List<Robot> robots, Room room, Map<Robot, Coord> assignments) {
    List<Coord> rechargeStations = room.getRechargeCoords();
    if (rechargeStations.isEmpty()) return;
    
    for (Robot robot : robots) {
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
    
    // Crear todas las combinaciones robot-dirty con sus distancias
    List<TargetPair> allPairs = new ArrayList<>();
    for (Robot robot : robots) {
      for (Coord dirty : availableDirty) {
        List<Coord> path = pathfindingService.findShortestPath(robot.getCoord(), dirty, room);
        int distance = path != null ? path.size() : Integer.MAX_VALUE;
        allPairs.add(new TargetPair(robot, dirty, distance));
      }
    }
    
    // Ordenar por distancia (más cercano primero)
    allPairs.sort(Comparator.comparing(TargetPair::getDistance));
    
    // Asignar de manera óptima (un dirty por robot)
    List<Robot> assignedRobots = new ArrayList<>();
    List<Coord> assignedDirty = new ArrayList<>();
    
    for (TargetPair pair : allPairs) {
      if (pair.distance == Integer.MAX_VALUE) continue;
      
      if (!assignedRobots.contains(pair.robot) && !assignedDirty.contains(pair.target)) {
        assignments.put(pair.robot, pair.target);
        assignedRobots.add(pair.robot);
        assignedDirty.add(pair.target);
      }
      
      // Si todos los robots tienen asignación, terminar
      if (assignedRobots.size() >= robots.size()) break;
    }
    
    // Robots sin asignación van al dirty más cercano disponible
    for (Robot robot : robots) {
      if (!assignments.containsKey(robot) && !availableDirty.isEmpty()) {
        Coord nearestDirty = availableDirty.stream()
            .min(Comparator.comparing(d -> robot.getCoord().distanceTo(d)))
            .orElse(null);
        
        if (nearestDirty != null) {
          assignments.put(robot, nearestDirty);
        }
      }
    }
  }
}
