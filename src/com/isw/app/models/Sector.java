package com.isw.app.models;

import com.isw.app.enums.SectorType;
import com.isw.app.helpers.IdentifierHelper;

public class Sector {
  private final String PREFIX = "SEC";

  private String uuid;
  private Coord coord;
  private SectorType type;
  private boolean isEmpty;

  public Sector(Coord coord, SectorType type) {
    this.uuid = IdentifierHelper.generate(PREFIX);
    this.isEmpty = true;
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

  public void setType(SectorType type) {
    this.type = type;
  }

  public void setIsEmpty(boolean isEmpty) {
    this.isEmpty = isEmpty;
  }

  public boolean isEmpty() {
    return isEmpty;
  }

  public boolean isNavigable() {
    return type != SectorType.OBSTRUCTED &&
        type != SectorType.TEMPORARY;
  }

  public boolean clean() {
    if (type == SectorType.DIRTY) {
      type = SectorType.CLEAN;
      return true;
    }
    return false;
  }
}
