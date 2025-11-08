package com.isw.app.services;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import com.isw.app.models.Room;
import com.isw.app.models.Coord;
import com.isw.app.enums.SectorType;

public class CalculationService {

  private static final int MIN_ROBOTS = 1;
  private static final int NEARBY_RADIUS = 2;

  private static final double BASE_SECTORS_PER_ROBOT = 12.0;
  private static final double OBSTACLE_COMPLEXITY_FACTOR = 0.15;
  private static final double NO_RECHARGE_PENALTY = 1.5;
  private static final double RECHARGE_EFFICIENCY_BASE = 3.0;

  public int calculateOptimalRobotCount(Room room) {
    Map<SectorType, Integer> counter = room.getSectorCounter();
    int dirtySectors = counter.getOrDefault(SectorType.DIRTY, 0);

    if (dirtySectors == 0)
      return MIN_ROBOTS;

    int totalSectors = room.getTotalSectors();
    int obstacles = counter.getOrDefault(SectorType.OBSTRUCTED, 0) +
        counter.getOrDefault(SectorType.TEMPORARY, 0);
    int rechargeSectors = counter.getOrDefault(SectorType.RECHARGE, 0);
    int cleanSectors = counter.getOrDefault(SectorType.CLEAN, 0);

    // Cálculo simplificado en una línea
    int robotCount = (int) Math.ceil(
        (dirtySectors / BASE_SECTORS_PER_ROBOT) *
            (1 + obstacles * OBSTACLE_COMPLEXITY_FACTOR / totalSectors) *
            (rechargeSectors == 0 ? NO_RECHARGE_PENALTY : Math.max(1, RECHARGE_EFFICIENCY_BASE / rechargeSectors)));

    // Límites min/max
    int maxRobots = Math.min(totalSectors / 4, Math.min(dirtySectors, cleanSectors));
    int minRobots = Math.max(MIN_ROBOTS, dirtySectors / 20);

    return Math.max(minRobots, Math.min(maxRobots, robotCount));
  }

  public List<Coord> getValidInitialPositions(Room room) {
    List<Coord> cleanEmptyCoords = room.getCoordsByType(SectorType.CLEAN)
        .stream()
        .filter(coord -> room.getSectorAt(coord).isEmpty())
        .collect(Collectors.toList());

    // Separar por prioridad y combinar
    Map<Boolean, List<Coord>> partitioned = cleanEmptyCoords.stream()
        .collect(Collectors.partitioningBy(coord -> room.hasDirtySectorsNearby(coord, NEARBY_RADIUS)));

    List<Coord> result = new ArrayList<>(partitioned.get(true));
    result.addAll(partitioned.get(false));
    return result;
  }

  public List<Coord> sortPositionsByQuality(List<Coord> positions, Room room) {
    return positions.stream()
        .sorted((pos1, pos2) -> Double.compare(
            evaluatePosition(pos2, room),
            evaluatePosition(pos1, room)))
        .collect(Collectors.toList());
  }

  private double evaluatePosition(Coord position, Room room) {
    double score = 0;

    // Sectores cercanos por tipo
    score += countNearbyType(position, room, SectorType.DIRTY, NEARBY_RADIUS) * 100;
    score += countNearbyType(position, room, SectorType.RECHARGE, 3) * 50;
    score -= countNearbyType(position, room, SectorType.OBSTRUCTED, 1) * 25;

    // Accesibilidad (distancia desde bordes)
    int distanceFromEdge = Math.min(
        Math.min(position.getRow(), room.getRows() - 1 - position.getRow()),
        Math.min(position.getCol(), room.getCols() - 1 - position.getCol()));
    score += distanceFromEdge * 10;

    return score;
  }

  private long countNearbyType(Coord center, Room room, SectorType type, int radius) {
    int startRow = Math.max(0, center.getRow() - radius);
    int endRow = Math.min(room.getRows() - 1, center.getRow() + radius);
    
    int startCol = Math.max(0, center.getCol() - radius);
    int endCol = Math.min(room.getCols() - 1, center.getCol() + radius);

    long count = 0;
    for (int row = startRow; row <= endRow; row++) {
      for (int col = startCol; col <= endCol; col++) {
        if (room.getSectorAt(new Coord(row, col)).getType() == type) {
          count++;
        }
      }
    }
    return count;
  }
}
