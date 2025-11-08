package com.isw.app.models;

import com.isw.app.enums.RobotState;
import com.isw.app.helpers.IdentifierHelper;

public class Robot {
  private final String PREFIX = "ROB";

  private String uuid;
  private Coord coord;
  private int battery;
  private RobotState state;

  public Robot(Coord coord) {
    this.uuid = IdentifierHelper.generate(PREFIX);
    this.state = RobotState.ACTIVE;
    this.coord = coord;
    this.battery = 10;
  }

  public int getBattery() {
    return battery;
  }

  public String getUuid() {
    return uuid;
  }

  public Coord getCoord() {
    return coord;
  }

  public RobotState getState() {
    return state;
  }

  public void setCoord(Coord coord) {
    this.coord = coord;
  }

  public void setState(RobotState state) {
    this.state = state;
  }

  public void consumeBattery(int amount) {
    this.battery = Math.max(0, this.battery - amount);
  }
}
