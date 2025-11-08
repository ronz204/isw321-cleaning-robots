package com.isw.app.services;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.isw.app.models.Room;
import com.isw.app.models.Robot;
import com.isw.app.models.Coord;
import com.isw.app.models.Sector;
import java.util.stream.Collectors;
import com.isw.app.enums.SectorType;
import com.isw.app.models.Decision;
import com.isw.app.repositories.RobotRepository;

public class RobotService {
  private final RobotRepository repository = new RobotRepository();
  private final MovementService movementService = new MovementService();

  public List<Robot> generate(Room room) {
    List<Coord> emptyCoords = room.getEmptyCoords();
    emptyCoords = emptyCoords.stream()
        .filter(coord -> {
          Sector sector = room.getSectorAt(coord);
          return sector.isNavigable();
        })
        .collect(Collectors.toList());

    if (emptyCoords.isEmpty()) {
      return new ArrayList<>();
    }

    try {
      List<Robot> robots = new ArrayList<>();
      int robotCount = calculateOptimalRobotCount(room);
      List<Coord> availablePositions = getValidInitialPositions(room);

      for (int i = 0; i < robotCount && i < availablePositions.size(); i++) {
        Coord position = availablePositions.get(i);
        Robot robot = new Robot(position);
        robots.add(robot);
        repository.save(robot);
        room.setSectorOccupied(position, true);
      }

      return robots;
    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList<>(); // Return empty list instead of null
    }
  }

  private int calculateOptimalRobotCount(Room room) {
    Map<SectorType, Integer> counter = room.getSectorCounter();
    int totalSectors = room.getTotalSectors();

    int dirtySectors = counter.getOrDefault(SectorType.DIRTY, 0);
    int obstacles = counter.getOrDefault(SectorType.OBSTRUCTED, 0) +
        counter.getOrDefault(SectorType.TEMPORARY, 0);
    int rechargeSectors = counter.getOrDefault(SectorType.RECHARGE, 0);
    int cleanSectors = counter.getOrDefault(SectorType.CLEAN, 0);

    if (dirtySectors == 0)
      return 1; // At least one robot

    double baseFactor = Math.ceil(dirtySectors / 12.0);
    double complexityFactor = 1 + (obstacles * 0.15 / totalSectors);
    double rechargeFactor = rechargeSectors == 0 ? 1.5 : Math.max(1, 3.0 / rechargeSectors);

    int robotCount = (int) Math.ceil(baseFactor * complexityFactor * rechargeFactor);

    int maxRobots = Math.min(Math.min(totalSectors / 4, dirtySectors), cleanSectors);
    int minRobots = Math.max(1, dirtySectors / 20);

    return Math.max(minRobots, Math.min(maxRobots, robotCount));
  }

  private List<Coord> getValidInitialPositions(Room room) {
    List<Coord> priorityPositions = new ArrayList<>();
    List<Coord> regularPositions = new ArrayList<>();

    for (int row = 0; row < room.getRows(); row++) {
      for (int col = 0; col < room.getCols(); col++) {
        Coord coord = new Coord(row, col);

        if (room.isValidCoord(coord)) {
          Sector sector = room.getSectorAt(coord);

          if (sector.getType() == SectorType.CLEAN && sector.isEmpty()) {
            if (hasDirtySectorsNearby(room, coord, 2)) {
              priorityPositions.add(coord);
            } else {
              regularPositions.add(coord);
            }
          }
        }
      }
    }

    priorityPositions.addAll(regularPositions);
    return priorityPositions;
  }

  // Add this helper method
  private boolean hasDirtySectorsNearby(Room room, Coord center, int radius) {
    for (int row = 0; row < room.getRows(); row++) {
      for (int col = 0; col < room.getCols(); col++) {
        Coord coord = new Coord(row, col);
        if (room.isValidCoord(coord) && center.distanceTo(coord) <= radius) {
          if (room.getSectorAt(coord).getType() == SectorType.DIRTY) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public List<Decision> calculateMovements(List<Robot> robots, Room room) {
    return movementService.calculateMovements(robots, room);
  }

  public void executeMovements(List<Decision> decisions, Room room) {
    for (Decision decision : decisions) {
      if (decision.hasValidMovement()) {
        Robot robot = decision.getRobot();
        moveRobot(robot, decision.getTargetCoord(), room);
      }
    }
  }

  private void moveRobot(Robot robot, Coord newCoord, Room room) {
    room.setSectorOccupied(robot.getCoord(), false);
    robot.setCoord(newCoord);
    room.setSectorOccupied(newCoord, true);
  }
}
