package com.isw.app.models;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import com.isw.app.enums.SectorType;
import com.isw.app.helpers.RandomHelper;
import com.isw.app.helpers.IdentifierHelper;

public class Room {
  private final String PREFIX = "ROO";

  private int ROWS = RandomHelper.getRandomInt(3, 10);
  private int COLS = RandomHelper.getRandomInt(3, 10);

  private String uuid;
  private Sector[][] sectors = new Sector[ROWS][COLS];
  private Map<SectorType, Integer> counter = new HashMap<>();

  public Room() {
    setupSectorCounter();
    setupSectorBoard();
    this.uuid = IdentifierHelper.generate(PREFIX);
  }

  public int getRows() {
    return ROWS;
  }

  public int getCols() {
    return COLS;
  }

  public String getUuid() {
    return uuid;
  }

  public Sector[][] getSectors() {
    return sectors;
  }

  public int getTotalSectors() {
    return ROWS * COLS;
  }

  public Sector getSectorAt(Coord coord) {
    return sectors[coord.getRow()][coord.getCol()];
  }

  public Map<SectorType, Integer> getSectorCounter() {
    return new HashMap<>(counter);
  }

  private void setupSectorCounter() {
    for (SectorType type : SectorType.values()) {
      counter.put(type, 0);
    }
  }

  private void setupSectorBoard() {
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        SectorType type = SectorType.getRandomType();
        sectors[row][col] = new Sector(new Coord(row, col), type);
        counter.put(type, counter.get(type) + 1);
      }
    }
  }

  public boolean isValidCoord(Coord coord) {
    return coord.isValidIn(ROWS, COLS);
  }

  public List<Coord> getEmptyCoords() {
    List<Coord> emptyCoords = new ArrayList<>();
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        Coord coord = new Coord(row, col);
        Sector sector = getSectorAt(coord);
        if (sector.checkIsEmpty() && sector.getType() != SectorType.OBSTRUCTED) {
          emptyCoords.add(coord);
        }
      }
    }
    return emptyCoords;
  }

  public boolean hasDirtySectorsNearby(Coord center, int radius) {
    for (int row = Math.max(0, center.getRow() - radius); row <= Math.min(ROWS - 1, center.getRow() + radius); row++) {
      for (int col = Math.max(0, center.getCol() - radius); col <= Math.min(COLS - 1,center.getCol() + radius); col++) {
        Coord coord = new Coord(row, col);
        if (getSectorAt(coord).getType() == SectorType.DIRTY) {
          return true;
        }
      }
    }
    return false;
  }

  public void setSectorOccupied(Coord coord, boolean occupied) {
    getSectorAt(coord).setIsEmpty(!occupied);
  }
}
