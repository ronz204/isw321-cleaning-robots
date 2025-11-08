package com.isw.app.presentation.components;

import java.awt.Font;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.BorderLayout;
import com.isw.app.models.Robot;
import com.isw.app.models.Sector;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class SectorBlock extends JPanel {
  private Sector sector;
  private JLabel robotLabel;

  public SectorBlock(Sector sector) {
    this.sector = sector;
    buildContainer();
    buildRobotLabel();
  }

  private void buildContainer() {
    this.setBackground(sector.getType().getColor());
    this.setBorder(new LineBorder(Color.BLACK, 1));
    this.setPreferredSize(new Dimension(40, 40));
    this.setLayout(new BorderLayout());
  }

  private void buildRobotLabel() {
    robotLabel = new JLabel();
    robotLabel.setHorizontalAlignment(SwingConstants.CENTER);
    robotLabel.setVerticalAlignment(SwingConstants.CENTER);
    robotLabel.setFont(new Font("Arial", Font.BOLD, 12));
    robotLabel.setForeground(Color.RED);
    robotLabel.setVisible(false);
    this.add(robotLabel, BorderLayout.CENTER);
  }

  public void setRobot(Robot robot) {
    if (robot != null) {
      robotLabel.setText("R");
      robotLabel.setVisible(true);
    } else {
      robotLabel.setText("");
      robotLabel.setVisible(false);
    }
    this.repaint();
  }

  public void removeRobot() {
    setRobot(null);
  }
}
