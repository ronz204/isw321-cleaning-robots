package com.isw.app.models;

import com.isw.app.enums.SectorType;
import com.isw.app.helpers.IdentifierHelper;
import com.isw.app.helpers.RandomHelper;

public class Sector {
  private final String PREFIX = "SEC";

  private String uuid;
  private Coord coord;
  private SectorType type;
  private boolean isEmpty;
  private Integer temporaryTimer;
  private long temporaryStartTime;

  public Sector(Coord coord, SectorType type) {
    this.uuid = IdentifierHelper.generate(PREFIX);
    this.isEmpty = true;
    this.coord = coord;
    this.type = type;
    
    if (type == SectorType.TEMPORARY) {
      this.temporaryTimer = RandomHelper.getRandomInt(2, 6);
    }
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
    return type != SectorType.OBSTRUCTED && type != SectorType.TEMPORARY;
  }

  public boolean clean() {
    if (type == SectorType.DIRTY) {
      type = SectorType.CLEAN;
      return true;
    }
    return false;
  }

  public Integer getTemporaryTimer() {
    return temporaryTimer;
  }

  public void startTemporaryTimer() {
    if (type == SectorType.TEMPORARY && temporaryStartTime == 0) {
      temporaryStartTime = System.currentTimeMillis();
    }
  }

  public boolean updateTemporaryTimer() {
    if (!isTemporary() || temporaryStartTime == 0) {
      if (isTemporary()) startTemporaryTimer();
      return false;
    }

    int elapsed = getElapsedSeconds();
    
    if (elapsed >= temporaryTimer) {
      convertToClean();
      return true;
    }
    
    return false;
  }

  public int getRemainingTime() {
    if (!isTemporary() || temporaryStartTime == 0) {
      return 0;
    }
    
    return Math.max(0, temporaryTimer - getElapsedSeconds());
  }

  private boolean isTemporary() {
    return type == SectorType.TEMPORARY && temporaryTimer != null;
  }

  private int getElapsedSeconds() {
    return (int) ((System.currentTimeMillis() - temporaryStartTime) / 1000);
  }

  private void convertToClean() {
    type = SectorType.CLEAN;
    temporaryTimer = null;
    temporaryStartTime = 0;
  }
}
