package com.isw.app.models;

import com.isw.app.enums.SectorType;
import com.isw.app.helpers.IdentifierHelper;
import com.isw.app.helpers.RandomHelper;

public class Sector {
  private static final String PREFIX = "SEC";
  private static final int MIN_TEMP_TIME = 2;
  private static final int MAX_TEMP_TIME = 6;

  private final String uuid;
  private final Coord coord;
  private SectorType type;
  private boolean isEmpty;
  private Integer temporaryTimer;
  private long temporaryStartTime;

  public Sector(Coord coord, SectorType type) {
    this.uuid = IdentifierHelper.generate(PREFIX);
    this.coord = coord;
    this.type = type;
    this.isEmpty = true;

    if (type == SectorType.TEMPORARY) {
      this.temporaryTimer = RandomHelper.getRandomInt(MIN_TEMP_TIME, MAX_TEMP_TIME);
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

  public boolean isEmpty() {
    return isEmpty;
  }

  public Integer getTemporaryTimer() {
    return temporaryTimer;
  }

  public void setType(SectorType type) {
    this.type = type;
  }

  public void setIsEmpty(boolean isEmpty) {
    this.isEmpty = isEmpty;
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

  public void startTemporaryTimer() {
    if (type == SectorType.TEMPORARY && temporaryStartTime == 0) {
      temporaryStartTime = System.currentTimeMillis();
    }
  }

  public boolean updateTemporaryTimer() {
    if (!isTemporary() || temporaryStartTime == 0) {
      if (isTemporary())
        startTemporaryTimer();
      return false;
    }

    if (getElapsedSeconds() >= temporaryTimer) {
      convertToClean();
      return true;
    }

    return false;
  }

  public int getRemainingTime() {
    if (!isTemporary() || temporaryStartTime == 0)
      return 0;
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
