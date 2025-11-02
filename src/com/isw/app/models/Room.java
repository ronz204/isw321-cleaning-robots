package com.isw.app.models;

import com.isw.app.enums.SectorType;
import com.isw.app.helpers.RandomHelper;
import com.isw.app.helpers.IdentifierHelper;

public class Room {
  private final String PREFIX = "ROO";

  private int ROWS = RandomHelper.getRandomInt(1, 10);
  private int COLS = RandomHelper.getRandomInt(1, 10);

  private Sector[][] sectors = new Sector[ROWS][COLS];
  private String uuid;

  public Room() {
    this.uuid = IdentifierHelper.generate(PREFIX);

    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        Coord coord = new Coord(row, col);
        SectorType type = SectorType.getRandomType();
        sectors[row][col] = new Sector(coord, type);
      }
    }
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
}
