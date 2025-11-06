package com.isw.app.models;

import com.isw.app.helpers.IdentifierHelper;

public class Robot {
  private final String PREFIX = "ROB";

  private String uuid;
  private Coord coord;
  private int battery;
  private boolean isActive;

  public Robot(Coord coord) {
    this.uuid = IdentifierHelper.generate(PREFIX);
    this.isActive = true;
    this.coord = coord;
    this.battery = 10;
  }

  public String getUuid() {
    return uuid;
  }

  public Coord getCoord() {
    return coord;
  }

  public void setCoord(Coord coord) {
    this.coord = coord;
  }

  public int getBattery() {
    return battery;
  }

  public void setBattery(int battery) {
    this.battery = battery;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }
}
