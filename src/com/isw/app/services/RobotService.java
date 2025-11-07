package com.isw.app.services;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import com.isw.app.models.Room;
import com.isw.app.models.Robot;
import com.isw.app.models.Coord;
import com.isw.app.models.Sector;
import com.isw.app.enums.SectorType;
import com.isw.app.repositories.RobotRepository;

public class RobotService {
  private final RobotRepository repository = new RobotRepository();

  public List<Robot> generate(Room room) {
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
      return null;
    }
  }

  private int calculateOptimalRobotCount(Room room) {
    Map<SectorType, Integer> counter = room.getSectorCounter();
    int totalSectors = room.getTotalSectors();

    int dirtySectors = counter.get(SectorType.DIRTY);
    int obstacles = counter.get(SectorType.OBSTRUCTED) + counter.get(SectorType.TEMPORARY);
    int rechargeSectors = counter.get(SectorType.RECHARGE);
    int cleanSectors = counter.get(SectorType.CLEAN);

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
        Sector sector = room.getSectorAt(coord);
        
        if (sector.getType() == SectorType.CLEAN && sector.checkIsEmpty()) {
          if (room.hasDirtySectorsNearby(coord, 2)) {
            priorityPositions.add(coord);
          } else {
            regularPositions.add(coord);
          }
        }
      }
    }

    priorityPositions.addAll(regularPositions);
    return priorityPositions;
  }
}
