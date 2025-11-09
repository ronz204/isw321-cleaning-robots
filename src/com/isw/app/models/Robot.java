package com.isw.app.models;

import com.isw.app.enums.RobotState;
import com.isw.app.helpers.IdentifierHelper;

public class Robot {
  private final String PREFIX = "ROB";
  private static final int MAX_BATTERY = 20;
  private static final int INITIAL_BATTERY = 10;
  private static final int RECHARGE_AMOUNT = 10;

  private String uuid;
  private Coord coord;
  private int battery;
  private RobotState state;
  private boolean needsRecharge;
  private Coord lastRechargePosition;

  public Robot(Coord coord) {
    this.uuid = IdentifierHelper.generate(PREFIX);
    this.battery = INITIAL_BATTERY;
    this.state = RobotState.ACTIVE;
    this.needsRecharge = false;
    this.lastRechargePosition = null;
    this.coord = coord;
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
    return lastRechargePosition != null && lastRechargePosition.equals(this.coord);
  }

  public void clearRechargePosition() {
    this.lastRechargePosition = null;
  }

  public boolean shouldSeekRecharge(int distanceToNearestRecharge) {
    // Si tiene muy poca batería, buscar recarga urgentemente
    if (this.battery <= 2) return true;
    
    // Si no puede completar el viaje de ida y vuelta a la recarga más cercana
    return this.battery <= distanceToNearestRecharge + 2;
  }

  public boolean canCompleteTrip(int distance) {
    return this.battery >= distance + 2;
  }
}
