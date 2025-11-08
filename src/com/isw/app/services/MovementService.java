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

  public List<Decision> calculateMovements(List<Robot> robots, Room room) {
    List<Decision> decisions = new ArrayList<>();

    List<Robot> prioritizedRobots = prioritizeRobots(robots, room);

    for (Robot robot : prioritizedRobots) {
      Decision decision = calculateBestMovement(robot, robots, room);
      decisions.add(decision);
    }

    return decisions;
  }

  private Decision calculateBestMovement(Robot robot, List<Robot> allRobots, Room room) {
    List<Coord> adjacentCoords = getAdjacentCoords(robot.getCoord(), room);
    Movement bestMovement = null;

    for (Coord targetCoord : adjacentCoords) {
      if (!isValidMove(targetCoord, room)) {
        continue;
      }

      Movement movement = evaluateMovementOption(robot, targetCoord, allRobots, room);

      if (bestMovement == null || movement.getScore() > bestMovement.getScore()) {
        bestMovement = movement;
      }
    }

    return new Decision(robot, bestMovement);
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

  // TO-DO: Review
  private boolean isValidMove(Coord coord, Room room) {
    Sector sector = room.getSectorAt(coord);
    return sector.isNavigable() && sector.isEmpty();
  }

  private Movement evaluateMovementOption(Robot robot, Coord targetCoord, List<Robot> allRobots, Room room) {
    double score = 0;

    Sector targetSector = room.getSectorAt(targetCoord);

    // Factor 1: Preferir celdas sucias (prioridad alta)
    if (targetSector.getType() == SectorType.DIRTY) {
      score += 1000;
    }

    // Factor 2: Sectores de recarga también son buenos
    if (targetSector.getType() == SectorType.RECHARGE) {
      score += 50;
    }

    // Factor 3: Penalizar por obstáculos cercanos
    int nearbyObstacles = countNearbyObstacles(targetCoord, room);
    score -= nearbyObstacles * 10;

    // Factor 4: Penalizar si hay otros robots muy cerca
    int nearbyRobots = countNearbyRobots(targetCoord, allRobots, 1);
    score -= nearbyRobots * 50;

    // Factor 5: Usar hasDirtySectorsNearby() como alternativa más simple
    if (room.hasDirtySectorsNearby(targetCoord, 2)) {
      score += 50; // Bonificación por estar cerca de sectores sucios
    }

    // Factor 6: Distancia a sector sucio más cercano
    int distanceToNearestDirty = getDistanceToNearestDirty(targetCoord, room);
    if (distanceToNearestDirty > 0) {
      score += (10.0 / distanceToNearestDirty);
    }

    return new Movement(targetCoord, score);
  }

  private int countNearbyObstacles(Coord center, Room room) {
    return countNearbySectors(center, room, SectorType.OBSTRUCTED, 1) +
        countNearbySectors(center, room, SectorType.TEMPORARY, 1);
  }

  private int countNearbyRobots(Coord center, List<Robot> robots, int radius) {
    int count = 0;
    for (Robot robot : robots) {
      if (isWithinRadius(center, robot.getCoord(), radius)) {
        count++;
      }
    }
    return count;
  }

  private int countNearbySectors(Coord center, Room room, SectorType type, int radius) {
    int count = 0;

    for (Coord coord : room.getAllCoords()) {
      if (!coord.equals(center) && center.distanceTo(coord) <= radius) {
        if (room.getSectorAt(coord).getType() == type) {
          count++;
        }
      }
    }
    return count;
  }

  private boolean isWithinRadius(Coord center, Coord target, int radius) {
    return center.distanceTo(target) <= radius;
  }

  private int getDistanceToNearestDirty(Coord coord, Room room) {
    return room.getAllCoords().stream()
        .filter(c -> room.getSectorAt(c).getType() == SectorType.DIRTY)
        .mapToInt(coord::distanceTo)
        .min()
        .orElse(0);
  }

  private List<Robot> prioritizeRobots(List<Robot> robots, Room room) {
    return robots.stream()
        .sorted(Comparator.comparing((Robot robot) -> -robot.getBattery())
            .thenComparing(robot -> getDistanceToNearestDirty(robot, room)))
        .toList();
  }

  private int getDistanceToNearestDirty(Robot robot, Room room) {
    return getDistanceToNearestDirty(robot.getCoord(), room);
  }
}
