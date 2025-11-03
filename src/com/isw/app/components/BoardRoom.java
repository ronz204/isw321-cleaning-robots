package com.isw.app.components;

import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridLayout;

public class BoardRoom extends JPanel {
  
  public BoardRoom() {
    buildContainer();
  }
  
  private void buildContainer() {
    this.setLayout(new GridLayout(10, 10));
    this.setBackground(Color.DARK_GRAY);
    
    JLabel titleLabel = new JLabel("Board Room", JLabel.CENTER);
    titleLabel.setForeground(Color.WHITE);
    titleLabel.setOpaque(true);
    titleLabel.setBackground(Color.GRAY);
    
    this.add(titleLabel);
  }
}
