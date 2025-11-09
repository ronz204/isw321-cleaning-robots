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

public class PathfindingService {
  private final CoordinateService coordinateService;
  private final NavigationService navigationService;

  public PathfindingService() {
    this.coordinateService = new CoordinateService();
    this.navigationService = new NavigationService(coordinateService);
  }

  public List<Coord> findShortestPath(Coord start, Coord goal, Room room) {
    return findShortestPath(start, goal, room, false);
  }

  public List<Coord> findShortestPath(Coord start, Coord goal, Room room, boolean allowRechargeTraversal) {
    if (start.equals(goal))
      return new ArrayList<>();

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

      for (Coord neighbor : coordinateService.getAdjacentCoords(current, room)) {
        if (!visited.contains(neighbor) &&
            navigationService.canNavigate(neighbor, goal, room, allowRechargeTraversal)) {
          visited.add(neighbor);
          parent.put(neighbor, current);
          queue.offer(neighbor);
        }
      }
    }

    return null;
  }

  public List<Coord> findShortestPathAvoidingRobots(Coord start, Coord goal, Room room,
      boolean allowRechargeTraversal) {
    if (start.equals(goal))
      return new ArrayList<>();

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

      for (Coord neighbor : coordinateService.getAdjacentCoords(current, room)) {
        if (!visited.contains(neighbor) &&
            navigationService.canNavigate(neighbor, goal, room, allowRechargeTraversal) &&
            room.getSectorAt(neighbor).isEmpty()) {
          visited.add(neighbor);
          parent.put(neighbor, current);
          queue.offer(neighbor);
        }
      }
    }

    return null;
  }

  public List<Coord> getValidAdjacentCoords(Coord current, Room room) {
    return navigationService.getValidAdjacentCoords(current, room);
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
