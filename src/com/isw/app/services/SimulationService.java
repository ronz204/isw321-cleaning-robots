package com.isw.app.services;

import java.util.List;
import java.util.ArrayList;
import com.isw.app.models.Room;
import com.isw.app.models.Robot;
import com.isw.app.models.Coord;
import com.isw.app.models.Sector;
import com.isw.app.enums.SectorType;
import com.isw.app.enums.RobotState;
import com.isw.app.models.Decision;

public class SimulationService {
  private MovementService movementService;
  private boolean isRunning;

  public SimulationService() {
    this.movementService = new MovementService();
    this.isRunning = false;
  }

  public boolean isRunning() {
    return isRunning;
  }

  public void startSimulation() {
    this.isRunning = true;
  }

  public void stopSimulation() {
    this.isRunning = false;
  }

  private void moveRobot(Robot robot, Coord newCoord, Room room) {
    room.setSectorOccupied(robot.getCoord(), false);
    robot.setCoord(newCoord);
    room.setSectorOccupied(newCoord, true);
  }

  private boolean hasActiveBots(List<Robot> robots) {
    return robots.stream().anyMatch(r -> r.getBattery() > 0);
  }

  private boolean hasMoreDirtySectors(Room room) {
    return room.getSectorCounter().getOrDefault(SectorType.DIRTY, 0) > 0;
  }

  private void updateRobotStatesOnSimulationEnd(List<Robot> robots) {
    for (Robot robot : robots) {
      if (robot.getBattery() <= 0) {
        robot.setState(RobotState.INACTIVE);
      } else {
        robot.setState(RobotState.WAITING);
      }
    }
  }

  public SimulationStep executeStep(List<Robot> robots, Room room) {
    if (!isRunning)
      return null;

    List<Decision> decisions = movementService.calculateMovements(robots, room);

    List<Robot> movedRobots = new ArrayList<>();
    int sectorsCleanedThisStep = 0;

    for (Decision decision : decisions) {
      Robot robot = decision.getRobot();

      if (robot.getBattery() <= 0) {
        robot.setState(RobotState.INACTIVE);
        movedRobots.add(robot);
        continue;
      }

      if (decision.hasValidMovement()) {
        Coord newCoord = decision.getTargetCoord();

        moveRobot(robot, newCoord, room);
        robot.setState(RobotState.ACTIVE);

        // Usar el método clean() del modelo Sector
        Sector targetSector = room.getSectorAt(newCoord);
        if (targetSector.clean()) {
          robot.setState(RobotState.CLEANING);
          sectorsCleanedThisStep++;
          
          room.decrementSectorCount(SectorType.DIRTY);
          room.incrementSectorCount(SectorType.CLEAN);
        }

        robot.consumeBattery(1);
      } else {
        robot.setState(RobotState.WAITING);
      }

      movedRobots.add(robot);
    }

    if (!hasMoreDirtySectors(room) || !hasActiveBots(robots)) {
      stopSimulation();
      updateRobotStatesOnSimulationEnd(robots);
    }

    return new SimulationStep(movedRobots, sectorsCleanedThisStep, !isRunning);
  }

  public static class SimulationStep {
    private List<Robot> robots;
    private int sectorsCleanedThisStep;
    private boolean isComplete;

    public SimulationStep(List<Robot> robots, int sectorsCleanedThisStep, boolean isComplete) {
      this.robots = robots;
      this.sectorsCleanedThisStep = sectorsCleanedThisStep;
      this.isComplete = isComplete;
    }

    public List<Robot> getRobots() {
      return robots;
    }

    public int getSectorsCleanedThisStep() {
      return sectorsCleanedThisStep;
    }

    public boolean isComplete() {
      return isComplete;
    }
  }
}