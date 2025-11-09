package com.isw.app.services;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import com.isw.app.models.Room;
import com.isw.app.models.Coord;

public class CoordinateService {

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

  public Coord findNearestCoord(Coord from, List<Coord> targets) {
    return targets.stream()
        .min(Comparator.comparing(from::distanceTo))
        .orElse(null);
  }

  public int calculateDistance(Coord from, Coord to) {
    return from.distanceTo(to);
  }

  public boolean areAdjacent(Coord coord1, Coord coord2) {
    return Math.abs(coord1.getRow() - coord2.getRow()) +
        Math.abs(coord1.getCol() - coord2.getCol()) == 1;
  }
}
