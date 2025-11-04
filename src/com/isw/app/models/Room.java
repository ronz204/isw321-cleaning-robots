package com.isw.app.models;

import java.util.Map;
import java.util.HashMap;
import com.isw.app.enums.SectorType;
import com.isw.app.helpers.RandomHelper;
import com.isw.app.helpers.IdentifierHelper;

public class Room {
  private final String PREFIX = "ROO";

  private int ROWS = RandomHelper.getRandomInt(1, 10);
  private int COLS = RandomHelper.getRandomInt(1, 10);

  private String uuid;
  private Sector[][] sectors = new Sector[ROWS][COLS];
  private Map<SectorType, Integer> counter = new HashMap<>();

  public Room() {
    setupSectorBoard();
    setupSectorCounter();
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
}
