package com.isw.app.models;

import java.util.List;

public class StepResult {
  private final List<Robot> robots;
  private final List<Decision> decisions;
  private final int sectorsCleanedThisStep;
  private final int totalSteps;
  private final boolean isComplete;

  public StepResult(List<Robot> robots, List<Decision> decisions,
      int sectorsCleanedThisStep, int totalSteps, boolean isComplete) {
    this.robots = robots;
    this.decisions = decisions;
    this.sectorsCleanedThisStep = sectorsCleanedThisStep;
    this.totalSteps = totalSteps;
    this.isComplete = isComplete;
  }

  public List<Robot> getRobots() {
    return robots;
  }

  public List<Decision> getDecisions() {
    return decisions;
  }

  public int getSectorsCleanedThisStep() {
    return sectorsCleanedThisStep;
  }

  public int getTotalSteps() {
    return totalSteps;
  }

  public boolean isComplete() {
    return isComplete;
  }

  public int getValidMovements() {
    return (int) decisions.stream()
        .filter(Decision::hasValidMovement)
        .count();
  }

  public int getActiveRobots() {
    return (int) robots.stream()
        .filter(robot -> robot.getBattery() > 0)
        .count();
  }
}
