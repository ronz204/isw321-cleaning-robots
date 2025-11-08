package com.isw.app.services;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import com.isw.app.models.Room;
import com.isw.app.models.Robot;
import com.isw.app.models.Coord;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import com.isw.app.models.Decision;
import com.isw.app.repositories.RobotRepository;

public class RobotService {
  private final RobotRepository repository;
  private final MovementService movementService;
  private final CalculationService calculationService;

  public RobotService() {
    this.repository = new RobotRepository();
    this.movementService = new MovementService();
    this.calculationService = new CalculationService();
  }

  public List<Robot> generate(Room room) {
    try {
      int robotCount = calculationService.calculateOptimalRobotCount(room);
      List<Coord> availablePositions = calculationService.getValidInitialPositions(room);

      if (availablePositions.isEmpty()) {
        return new ArrayList<>();
      }

      List<Coord> bestPositions = calculationService.sortPositionsByQuality(availablePositions, room);
      return createRobots(robotCount, bestPositions, room);

    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  private List<Robot> createRobots(int robotCount, List<Coord> positions, Room room) throws IOException {
    return IntStream.range(0, Math.min(robotCount, positions.size()))
        .mapToObj(i -> createSingleRobot(positions.get(i), room))
        .filter(robot -> robot != null)
        .collect(Collectors.toList());
  }

  private Robot createSingleRobot(Coord position, Room room) {
    try {
      Robot robot = new Robot(position);
      repository.save(robot);
      room.setSectorOccupied(position, true);
      return robot;
    } catch (IOException e) {
      return null;
    }
  }

  public List<Decision> calculateMovements(List<Robot> robots, Room room) {
    return movementService.calculateMovements(robots, room);
  }

  public void executeMovements(List<Decision> decisions, Room room) {
    decisions.stream()
        .filter(Decision::hasValidMovement)
        .forEach(decision -> moveRobot(decision.getRobot(), decision.getTargetCoord(), room));
  }

  private void moveRobot(Robot robot, Coord newCoord, Room room) {
    room.setSectorOccupied(robot.getCoord(), false);
    robot.setCoord(newCoord);
    room.setSectorOccupied(newCoord, true);
  }
}
