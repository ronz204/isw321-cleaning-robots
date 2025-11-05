package com.isw.app.presentation.components;

import javax.swing.JPanel;
import com.isw.app.models.Sector;

public class SectorBlock extends JPanel {
  public SectorBlock(Sector sector) {
    this.setBackground(sector.getType().getColor());
  }
}
