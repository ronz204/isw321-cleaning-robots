package com.isw.app.presentation.components;

import java.awt.Color;
import javax.swing.JPanel;
import java.awt.GridLayout;
import com.isw.app.models.Room;
import com.isw.app.models.Sector;

public class BoardRoom extends JPanel {

  public BoardRoom() {
    buildContainer();
  }

  private void buildContainer() {
    this.setBackground(Color.DARK_GRAY);
  }

  public void onUpdateRoom(Room room) {
    this.removeAll();
    int rows = room.getRows();
    int cols = room.getCols();

    this.setLayout(new GridLayout(rows, cols, 1, 1));
    buildSectorBlocks(room);

    this.revalidate();
    this.repaint();
  }

  private void buildSectorBlocks(Room room) {
    Sector[][] sectors = room.getSectors();
    for (int row = 0; row < room.getRows(); row++) {
      for (int col = 0; col < room.getCols(); col++) {
        this.add(new SectorBlock(sectors[row][col]));
      }
    }
  }
}
