package com.isw.app.models;

public class Decision {
  private Robot robot;
  private Movement movement;

  public Decision(Robot robot, Movement movement) {
    this.robot = robot;
    this.movement = movement;
  }

  public Robot getRobot() {
    return robot;
  }

  public Movement getMovement() {
    return movement;
  }

  public boolean hasValidMovement() {
    return movement != null;
  }

  public Coord getTargetCoord() {
    return hasValidMovement()
        ? movement.getCoord()
        : robot.getCoord();
  }
}
