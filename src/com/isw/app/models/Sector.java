package com.isw.app.models;

import com.isw.app.enums.SectorType;
import com.isw.app.helpers.IdentifierHelper;

public class Sector {
  private final String PREFIX = "SEC";

  private String uuid;
  private Coord coord;
  private SectorType type;
  private boolean isOccupied;

  public Sector(Coord coord, SectorType type) {
    this.uuid = IdentifierHelper.generate(PREFIX);
    this.isOccupied = false;
    this.coord = coord;
    this.type = type;
  }

  public String getUuid() {
    return uuid;
  }

  public Coord getCoord() {
    return coord;
  }

  public SectorType getType() {
    return type;
  }

  public void flagOccupied() {
    this.isOccupied = !this.isOccupied;
  }

  public boolean checkOccupied() {
    return isOccupied;
  }
}
