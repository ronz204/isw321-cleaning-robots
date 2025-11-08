package com.isw.app.models;

public class TargetPair {
  public final Robot robot;
  public final Coord target;
  public final int distance;

  public TargetPair(Robot robot, Coord target, int distance) {
    this.robot = robot;
    this.target = target;
    this.distance = distance;
  }

  public Robot getRobot() {
    return robot;
  }

  public Coord getTarget() {
    return target;
  }

  public int getDistance() {
    return distance;
  }
}
