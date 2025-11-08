package com.isw.app.services;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import com.isw.app.models.Room;
import com.isw.app.models.Robot;
import com.isw.app.models.Coord;
import com.isw.app.models.Decision;
import com.isw.app.models.Cleaning;
import com.isw.app.models.StepResult;
import com.isw.app.models.Sector;
import com.isw.app.enums.SectorType;
import com.isw.app.enums.RobotState;
import com.isw.app.repositories.RoomRepository;
import com.isw.app.repositories.RobotRepository;

public class CleaningService {
  private final RoomRepository roomRepository;
  private final RobotRepository robotRepository;
  private final MovementService movementService;
  private final CalculationService calculationService;

  public CleaningService() {
    this.roomRepository = new RoomRepository();
    this.robotRepository = new RobotRepository();
    this.movementService = new MovementService();
    this.calculationService = new CalculationService();
  }

  public Room generateRoom() {
    try {
      Room room = new Room();
      roomRepository.save(room);
      return room;
    } catch (Exception e) {
      return null;
    }
  }

  public List<Robot> generateRobots(Room room) {
    try {
      int robotCount = calculationService.calculateOptimalRobotCount(room);
      List<Coord> availablePositions = calculationService.getValidInitialPositions(room);
      
      if (availablePositions.isEmpty()) return new ArrayList<>();

      List<Coord> bestPositions = calculationService.sortPositionsByQuality(availablePositions, room);
      return createRobots(robotCount, bestPositions, room);
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  private List<Robot> createRobots(int robotCount, List<Coord> positions, Room room) {
    return IntStream.range(0, Math.min(robotCount, positions.size()))
        .mapToObj(i -> createSingleRobot(positions.get(i), room))
        .filter(robot -> robot != null)
        .collect(Collectors.toList());
  }

  private Robot createSingleRobot(Coord position, Room room) {
    try {
      Robot robot = new Robot(position);
      robotRepository.save(robot);
      room.setSectorOccupied(position, true);
      return robot;
    } catch (IOException e) {
      return null;
    }
  }

  public Cleaning generateCleaning() {
    Room room = generateRoom();
    if (room == null) return null;
    
    List<Robot> robots = generateRobots(room);
    return new Cleaning(room, robots);
  }

  // Método central que maneja todo un paso de simulación
  public StepResult executeStep(Cleaning cleaning) {
    if (!cleaning.isValid()) return null;

    List<Decision> decisions = movementService.calculateMovements(cleaning.getRobots(), cleaning.getRoom());
    List<Robot> processedRobots = processRobotDecisions(decisions, cleaning.getRoom());
    
    int sectorsCleanedThisStep = (int) processedRobots.stream()
        .mapToLong(robot -> robot.getState() == RobotState.CLEANING ? 1 : 0)
        .sum();

    // Actualizar estado de cleaning
    cleaning.incrementSteps();
    cleaning.addCleanedSectors(sectorsCleanedThisStep);

    boolean isComplete = isSimulationComplete(cleaning.getRoom(), cleaning.getRobots());
    if (isComplete) {
      cleaning.setActive(false);
      updateRobotStatesOnEnd(cleaning.getRobots());
    }

    return new StepResult(processedRobots, decisions, sectorsCleanedThisStep, 
                         cleaning.getTotalSteps(), isComplete);
  }

  // Procesa todas las decisiones de robots en un solo método
  private List<Robot> processRobotDecisions(List<Decision> decisions, Room room) {
    return decisions.stream()
        .map(decision -> processRobotDecision(decision, room))
        .collect(Collectors.toList());
  }

  // Procesa la decisión de un robot individual
  private Robot processRobotDecision(Decision decision, Room room) {
    Robot robot = decision.getRobot();

    if (robot.getBattery() <= 0) {
      robot.setState(RobotState.INACTIVE);
      return robot;
    }

    if (!decision.hasValidMovement()) {
      robot.setState(RobotState.WAITING);
      return robot;
    }

    Coord newCoord = decision.getTargetCoord();
    Sector targetSector = room.getSectorAt(newCoord);

    if (!targetSector.isEmpty() && targetSector.getType() != SectorType.RECHARGE) {
      robot.setState(RobotState.WAITING);
      return robot;
    }

    // Ejecutar movimiento
    moveRobot(robot, newCoord, room);

    // Manejar acciones en la nueva posición
    if (targetSector.getType() == SectorType.RECHARGE) {
      robot.rechargeBattery();
      robot.setState(RobotState.ACTIVE);
    } else if (targetSector.clean()) {
      robot.setState(RobotState.CLEANING);
      room.decrementSectorCount(SectorType.DIRTY);
      room.incrementSectorCount(SectorType.CLEAN);
      robot.consumeBattery(1);
    } else {
      robot.setState(RobotState.ACTIVE);
      robot.consumeBattery(1);
    }

    return robot;
  }

  private void moveRobot(Robot robot, Coord newCoord, Room room) {
    room.setSectorOccupied(robot.getCoord(), false);
    robot.setCoord(newCoord);
    room.setSectorOccupied(newCoord, true);
  }

  private boolean isSimulationComplete(Room room, List<Robot> robots) {
    return room.getSectorCounter().getOrDefault(SectorType.DIRTY, 0) == 0 || 
           robots.stream().noneMatch(r -> r.getBattery() > 0);
  }

  private void updateRobotStatesOnEnd(List<Robot> robots) {
    robots.forEach(robot -> robot.setState(
        robot.getBattery() <= 0 ? RobotState.INACTIVE : RobotState.WAITING));
  }

  public void startCleaning(Cleaning cleaning) {
    cleaning.setActive(true);
  }

  public void stopCleaning(Cleaning cleaning) {
    cleaning.setActive(false);
    updateRobotStatesOnEnd(cleaning.getRobots());
  }
}
