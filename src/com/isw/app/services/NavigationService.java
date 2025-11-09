package com.isw.app.services;

import java.util.List;
import com.isw.app.models.Room;
import com.isw.app.models.Coord;
import java.util.stream.Collectors;
import com.isw.app.enums.SectorType;

public class NavigationService {
  private final CoordinateService coordinateService;

  public NavigationService(CoordinateService coordinateService) {
    this.coordinateService = coordinateService;
  }

  public boolean canNavigate(Coord coord, Coord goal, Room room, boolean allowRechargeTraversal) {
    SectorType type = room.getSectorAt(coord).getType();

    if (type == SectorType.CLEAN || type == SectorType.DIRTY) {
      return true;
    }

    if (type == SectorType.RECHARGE) {
      return coord.equals(goal) || allowRechargeTraversal;
    }

    return false;
  }

  public boolean isBlocked(Coord coord, Room room, List<Coord> reservedCoords) {
    return reservedCoords.contains(coord) || !room.getSectorAt(coord).isEmpty();
  }

  public boolean isRechargeOccupied(Room room, Coord target) {
    return room.getSectorAt(target).getType() == SectorType.RECHARGE &&
        !room.getSectorAt(target).isEmpty();
  }

  public List<Coord> getValidAdjacentCoords(Coord current, Room room) {
    return coordinateService.getAdjacentCoords(current, room).stream()
        .filter(coord -> room.getSectorAt(coord).isNavigable())
        .collect(Collectors.toList());
  }

  public List<Coord> getUnblockedAdjacentCoords(Coord current, Room room, List<Coord> reservedCoords) {
    return getValidAdjacentCoords(current, room).stream()
        .filter(coord -> !isBlocked(coord, room, reservedCoords))
        .collect(Collectors.toList());
  }

  public boolean isNavigableBasic(Coord coord, Room room) {
    return room.getSectorAt(coord).isNavigable();
  }
}
