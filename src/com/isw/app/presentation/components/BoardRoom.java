package com.isw.app.presentation.components;

import java.awt.Color;
import java.util.List;
import javax.swing.JPanel;
import java.awt.GridLayout;
import com.isw.app.models.Room;
import com.isw.app.models.Robot;
import com.isw.app.models.Sector;
import javax.swing.border.EmptyBorder;

public class BoardRoom extends JPanel {

  private SectorBlock[][] sectorBlocks;
  private Room currentRoom;

  public BoardRoom() {
    buildContainer();
  }

  private void buildContainer() {
    this.setBackground(Color.DARK_GRAY);
    this.setBorder(new EmptyBorder(20, 20, 20, 20));
  }

  public void onUpdateRoom(Room room) {
    this.currentRoom = room;
    this.removeAll();
    int rows = room.getRows();
    int cols = room.getCols();

    this.setLayout(new GridLayout(rows, cols, 3, 3));
    sectorBlocks = new SectorBlock[rows][cols];
    buildSectorBlocks(room);

    this.revalidate();
    this.repaint();
  }

  private void buildSectorBlocks(Room room) {
    Sector[][] sectors = room.getSectors();
    for (int row = 0; row < room.getRows(); row++) {
      for (int col = 0; col < room.getCols(); col++) {
        SectorBlock block = new SectorBlock(sectors[row][col]);
        sectorBlocks[row][col] = block;
        this.add(block);
      }
    }
  }

  public void onUpdateRobots(List<Robot> robots) {
    if (sectorBlocks == null || currentRoom == null)
      return;

    clearAllRobots();
    refreshSectors();

    for (Robot robot : robots) {
      int row = robot.getCoord().getRow();
      int col = robot.getCoord().getCol();

      if (isValidPosition(row, col) && sectorBlocks[row][col] != null) {
        sectorBlocks[row][col].setRobot(robot);
      }
    }

    this.repaint();
  }

  private void clearAllRobots() {
    for (int row = 0; row < sectorBlocks.length; row++) {
      for (int col = 0; col < sectorBlocks[row].length; col++) {
        sectorBlocks[row][col].removeRobot();
      }
    }
  }

  private boolean isValidPosition(int row, int col) {
    return row >= 0 && row < sectorBlocks.length &&
        col >= 0 && col < sectorBlocks[0].length;
  }

  private void refreshSectors() {
    for (int row = 0; row < sectorBlocks.length; row++) {
      for (int col = 0; col < sectorBlocks[row].length; col++) {
        if (sectorBlocks[row][col] != null) {
          sectorBlocks[row][col].updateSectorType();
        }
      }
    }
  }
}
