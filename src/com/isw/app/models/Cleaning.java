package com.isw.app.models;

import java.util.List;
import com.isw.app.helpers.IdentifierHelper;

public class Cleaning {
  private final String PREFIX = "CLN";

  private String uuid;
  private Room room;
  private boolean isActive;
  private List<Robot> robots;

  private int totalSteps;
  private int sectorsCleanedTotal;

  public Cleaning(Room room, List<Robot> robots) {
    this.uuid = IdentifierHelper.generate(PREFIX);
    this.room = room;
    this.robots = robots;
    this.isActive = false;

    this.totalSteps = 0;
    this.sectorsCleanedTotal = 0;
  }

  public String getUuid() {
    return uuid;
  }

  public Room getRoom() {
    return room;
  }

  public List<Robot> getRobots() {
    return robots;
  }

  public boolean isActive() {
    return isActive;
  }

  public int getTotalSteps() {
    return totalSteps;
  }

  public int getSectorsCleanedTotal() {
    return sectorsCleanedTotal;
  }

  public void setActive(boolean active) {
    this.isActive = active;
  }

  public void incrementSteps() {
    this.totalSteps++;
  }

  public void addCleanedSectors(int sectors) {
    this.sectorsCleanedTotal += sectors;
  }

  public boolean isValid() {
    return room != null && robots != null && !robots.isEmpty();
  }

  public double getCompletionPercentage() {
    int totalSectors = room.getTotalSectors();
    return totalSectors > 0 ? ((double) sectorsCleanedTotal / totalSectors) * 100 : 0;
  }
}
