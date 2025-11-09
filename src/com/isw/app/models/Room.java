package com.isw.app.models;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import com.isw.app.enums.SectorType;
import com.isw.app.helpers.RandomHelper;
import com.isw.app.helpers.IdentifierHelper;

public class Room {
  private final String PREFIX = "ROO";
  private static final int MAX_RECHARGE = 4;

  private final int ROWS = RandomHelper.getRandomInt(3, 10);
  private final int COLS = RandomHelper.getRandomInt(3, 10);

  private String uuid;
  private Sector[][] sectors = new Sector[ROWS][COLS];
  private Map<SectorType, Integer> counter = new HashMap<>();

  public Room() {
    this.uuid = IdentifierHelper.generate(PREFIX);
    setupSectorCounter();
    setupSectorBoard();
  }

  public int getRows() {
    return ROWS;
  }

  public int getCols() {
    return COLS;
  }

  public String getUuid() {
    return uuid;
  }

  public Sector[][] getSectors() {
    return sectors;
  }

  public int getTotalSectors() {
    return ROWS * COLS;
  }

  public Sector getSectorAt(Coord coord) {
    return sectors[coord.getRow()][coord.getCol()];
  }

  public Map<SectorType, Integer> getSectorCounter() {
    return new HashMap<>(counter);
  }

  private void setupSectorCounter() {
    for (SectorType type : SectorType.values()) {
      counter.put(type, 0);
    }
  }

  private void setupSectorBoard() {
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        SectorType type = determineValidSectorType();
        sectors[row][col] = new Sector(new Coord(row, col), type);
        counter.merge(type, 1, Integer::sum);
      }
    }
  }

  private SectorType determineValidSectorType() {
    SectorType type = SectorType.getRandomType();
    
    if (type == SectorType.RECHARGE && counter.getOrDefault(SectorType.RECHARGE, 0) >= MAX_RECHARGE) {
      return SectorType.CLEAN;
    }
    
    return type;
  }

  public boolean isValidCoord(Coord coord) {
    return coord.isValidIn(ROWS, COLS);
  }

  public List<Coord> getAllCoords() {
    return IntStream.range(0, ROWS)
        .boxed()
        .flatMap(row -> IntStream.range(0, COLS)
            .mapToObj(col -> new Coord(row, col)))
        .collect(Collectors.toList());
  }

  public List<Coord> getCoordsByType(SectorType type) {
    return getAllCoords().stream()
        .filter(coord -> getSectorAt(coord).getType() == type)
        .collect(Collectors.toList());
  }

  public List<Coord> getEmptyCoords() {
    return getAllCoords().stream()
        .filter(coord -> {
          Sector sector = getSectorAt(coord);
          return sector.isEmpty() && sector.isNavigable();
        })
        .collect(Collectors.toList());
  }

  public boolean hasDirtySectorsNearby(Coord center, int radius) {
    int minRow = Math.max(0, center.getRow() - radius);
    int maxRow = Math.min(ROWS - 1, center.getRow() + radius);
    int minCol = Math.max(0, center.getCol() - radius);
    int maxCol = Math.min(COLS - 1, center.getCol() + radius);

    return IntStream.rangeClosed(minRow, maxRow)
        .anyMatch(row -> IntStream.rangeClosed(minCol, maxCol)
            .anyMatch(col -> getSectorAt(new Coord(row, col)).getType() == SectorType.DIRTY));
  }

  public void setSectorOccupied(Coord coord, boolean occupied) {
    getSectorAt(coord).setIsEmpty(!occupied);
  }

  public void decrementSectorCount(SectorType type) {
    counter.computeIfPresent(type, (k, v) -> Math.max(0, v - 1));
  }

  public void incrementSectorCount(SectorType type) {
    counter.merge(type, 1, Integer::sum);
  }

  public List<Coord> getRechargeCoords() {
    return getCoordsByType(SectorType.RECHARGE);
  }

  public int getDistanceToNearestRecharge(Coord coord) {
    return getRechargeCoords().stream()
        .mapToInt(coord::distanceTo)
        .min()
        .orElse(Integer.MAX_VALUE);
  }

  public int getDistanceToNearestDirty(Coord coord) {
    return getCoordsByType(SectorType.DIRTY).stream()
        .mapToInt(coord::distanceTo)
        .min()
        .orElse(Integer.MAX_VALUE);
  }

  public void startTemporaryTimers() {
    forEachSector(sector -> {
      if (sector.getType() == SectorType.TEMPORARY) {
        sector.startTemporaryTimer();
      }
    });
  }

  public List<Coord> updateTemporaryTimers() {
    List<Coord> changedSectors = new ArrayList<>();

    forEachSector(sector -> {
      if (sector.updateTemporaryTimer()) {
        changedSectors.add(sector.getCoord());
        decrementSectorCount(SectorType.TEMPORARY);
        incrementSectorCount(SectorType.CLEAN);
      }
    });

    return changedSectors;
  }

  private void forEachSector(Consumer<Sector> action) {
    for (int row = 0; row < ROWS; row++) {
      for (int col = 0; col < COLS; col++) {
        action.accept(sectors[row][col]);
      }
    }
  }
}
