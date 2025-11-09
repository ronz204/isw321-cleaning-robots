package com.isw.app.models;

import com.isw.app.enums.RobotState;
import com.isw.app.helpers.IdentifierHelper;

public class Robot {
  private static final String PREFIX = "ROB";
  private static final int MAX_BATTERY = 20;
  private static final int INITIAL_BATTERY = 10;
  private static final int RECHARGE_AMOUNT = 10;
  private static final int CRITICAL_BATTERY = 2;

  private final String uuid;
  private Coord coord;
  private int battery;
  private RobotState state;
  private boolean needsRecharge;
  private Coord lastRechargePosition;

  public Robot(Coord coord) {
    this.uuid = IdentifierHelper.generate(PREFIX);
    this.coord = coord;
    this.battery = INITIAL_BATTERY;
    this.state = RobotState.ACTIVE;
    this.needsRecharge = false;
  }

  public String getUuid() {
    return uuid;
  }

  public Coord getCoord() {
    return coord;
  }

  public int getBattery() {
    return battery;
  }

  public RobotState getState() {
    return state;
  }

  public boolean needsRecharge() {
    return needsRecharge;
  }

  public void setCoord(Coord coord) {
    this.coord = coord;
  }

  public void setState(RobotState state) {
    this.state = state;
  }

  public void setNeedsRecharge(boolean needsRecharge) {
    this.needsRecharge = needsRecharge;
  }

  public void consumeBattery(int amount) {
    this.battery = Math.max(0, this.battery - amount);
  }

  public void rechargeBattery() {
    this.battery = Math.min(MAX_BATTERY, this.battery + RECHARGE_AMOUNT);
    this.needsRecharge = false;
    this.lastRechargePosition = this.coord;
  }

  public boolean isAtRechargePosition() {
    return lastRechargePosition != null && lastRechargePosition.equals(coord);
  }

  public void clearRechargePosition() {
    this.lastRechargePosition = null;
  }

  public boolean shouldSeekRecharge(int distanceToNearestRecharge) {
    return battery <= CRITICAL_BATTERY || battery <= distanceToNearestRecharge + CRITICAL_BATTERY;
  }
}
