package com.isw.app.services;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Queue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedList;
import com.isw.app.models.Room;
import com.isw.app.models.Coord;
import com.isw.app.enums.SectorType;
import java.util.stream.Collectors;

public class PathfindingService {

  public List<Coord> findShortestPath(Coord start, Coord goal, Room room) {
    return findShortestPath(start, goal, room, false);
  }

  public List<Coord> findShortestPath(Coord start, Coord goal, Room room, boolean ignoreTemporary) {
    if (start.equals(goal)) return new ArrayList<>();

    Queue<Coord> queue = new LinkedList<>();
    Map<Coord, Coord> parent = new HashMap<>();
    Set<Coord> visited = new HashSet<>();

    queue.offer(start);
    visited.add(start);
    parent.put(start, null);

    while (!queue.isEmpty()) {
      Coord current = queue.poll();

      if (current.equals(goal)) {
        return reconstructPath(parent, goal);
      }

      for (Coord neighbor : getAdjacentCoords(current, room)) {
        if (!visited.contains(neighbor) && isNavigable(neighbor, room, ignoreTemporary)) {
          visited.add(neighbor);
          parent.put(neighbor, current);
          queue.offer(neighbor);
        }
      }
    }

    return null;
  }

  private boolean isNavigable(Coord coord, Room room, boolean ignoreTemporary) {
    SectorType type = room.getSectorAt(coord).getType();
    
    if (ignoreTemporary && type == SectorType.TEMPORARY) {
      return true;
    }
    
    return type == SectorType.CLEAN || type == SectorType.DIRTY || type == SectorType.RECHARGE;
  }

  public boolean hasTemporaryInPath(Coord start, Coord goal, Room room) {
    List<Coord> path = findShortestPath(start, goal, room, true);
    if (path == null) return false;
    
    return path.stream()
        .anyMatch(coord -> room.getSectorAt(coord).getType() == SectorType.TEMPORARY);
  }

  public int getMaxTemporaryTimeInPath(Coord start, Coord goal, Room room) {
    List<Coord> path = findShortestPath(start, goal, room, true);
    if (path == null) return 0;
    
    return path.stream()
        .filter(coord -> room.getSectorAt(coord).getType() == SectorType.TEMPORARY)
        .mapToInt(coord -> room.getSectorAt(coord).getRemainingTime())
        .max()
        .orElse(0);
  }

  public List<Coord> getAdjacentCoords(Coord current, Room room) {
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

  public List<Coord> getValidAdjacentCoords(Coord current, Room room) {
    return getAdjacentCoords(current, room).stream()
        .filter(coord -> {
          SectorType type = room.getSectorAt(coord).getType();
          return type != SectorType.OBSTRUCTED && type != SectorType.TEMPORARY;
        })
        .collect(Collectors.toList());
  }

  private List<Coord> reconstructPath(Map<Coord, Coord> parent, Coord goal) {
    List<Coord> path = new ArrayList<>();
    Coord current = goal;

    while (parent.get(current) != null) {
      path.add(0, current);
      current = parent.get(current);
    }

    return path;
  }
}
