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
import com.isw.app.models.Sector;
import com.isw.app.enums.SectorType;

public class PathfindingService {

  public List<Coord> findShortestPath(Coord start, Coord goal, Room room) {
    if (start.equals(goal)) {
      return new ArrayList<>();
    }

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
        if (!visited.contains(neighbor) && isNavigable(neighbor, room)) {
          visited.add(neighbor);
          parent.put(neighbor, current);
          queue.offer(neighbor);
        }
      }
    }

    return null; // No path found
  }

  public List<Coord> findClosestTargets(Coord start, List<Coord> targets, Room room) {
    List<Coord> reachableTargets = new ArrayList<>();

    for (Coord target : targets) {
      List<Coord> path = findShortestPath(start, target, room);
      if (path != null) {
        reachableTargets.add(target);
      }
    }

    // Sort by distance
    reachableTargets.sort((a, b) -> Integer.compare(start.distanceTo(a), start.distanceTo(b)));

    return reachableTargets;
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

  private boolean isNavigable(Coord coord, Room room) {
    Sector sector = room.getSectorAt(coord);
    return sector.getType() == SectorType.CLEAN ||
        sector.getType() == SectorType.DIRTY ||
        sector.getType() == SectorType.RECHARGE;
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
