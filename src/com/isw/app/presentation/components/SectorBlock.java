package com.isw.app.presentation.components;

import java.awt.Font;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import java.awt.GridBagLayout;
import com.isw.app.models.Robot;
import com.isw.app.models.Sector;
import java.awt.GridBagConstraints;
import javax.swing.border.LineBorder;

public class SectorBlock extends JPanel {
  private Sector sector;
  private JPanel robotPanel;
  private JLabel robotLabel;
  private JLabel robotIdLabel;

  public SectorBlock(Sector sector) {
    this.sector = sector;
    buildContainer();
    buildRobotPanel();
  }

  private void buildContainer() {
    this.setBackground(sector.getType().getColor());
    this.setBorder(new LineBorder(Color.BLACK, 1));
    this.setPreferredSize(new Dimension(40, 40));
    this.setLayout(new GridBagLayout());
  }

  private void buildRobotPanel() {
    robotPanel = new JPanel();
    robotPanel.setLayout(new BoxLayout(robotPanel, BoxLayout.Y_AXIS));
    robotPanel.setOpaque(false);
    
    robotLabel = new JLabel();
    robotLabel.setAlignmentX(CENTER_ALIGNMENT);
    robotLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
    
    robotIdLabel = new JLabel();
    robotIdLabel.setAlignmentX(CENTER_ALIGNMENT);
    robotIdLabel.setFont(new Font("Arial", Font.BOLD, 8));
    robotIdLabel.setForeground(Color.BLACK);
    
    robotPanel.add(robotLabel);
    robotPanel.add(robotIdLabel);
    robotPanel.setVisible(false);
    
    GridBagConstraints gbc = new GridBagConstraints();
    this.add(robotPanel, gbc);
  }

  public void setRobot(Robot robot) {
    if (robot != null) {
      robotLabel.setText("🤖");
      robotIdLabel.setText(robot.getUuid());
      robotPanel.setVisible(true);
    } else {
      robotLabel.setText("");
      robotIdLabel.setText("");
      robotPanel.setVisible(false);
    }
    this.repaint();
  }

  public void removeRobot() {
    setRobot(null);
  }

  public void updateSectorType() {
    this.setBackground(sector.getType().getColor());
  }
}
