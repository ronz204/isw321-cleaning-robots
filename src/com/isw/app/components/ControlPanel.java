package com.isw.app.components;

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;

public class ControlPanel extends JPanel {
  
  private JButton startButton;
  private JButton exitButton;
  private JPanel buttonPanel;
  private JLabel titleLabel;
  
  public ControlPanel() {
    buildContainer();
    buildButtonPanel();
    buildTitleLabel();
    buildStartButton();
    buildExitButton();
  }
  
  private void buildContainer() {
    this.setLayout(new BorderLayout());
  }
  
  private void buildButtonPanel() {
    buttonPanel = new JPanel();
    buttonPanel.setBackground(Color.LIGHT_GRAY);
    buttonPanel.setLayout(new GridLayout(3, 1, 0, 10));
    this.add(buttonPanel, BorderLayout.CENTER);
  }
  
  private void buildTitleLabel() {
    titleLabel = new JLabel("Cleaning Robots");
    titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
    titleLabel.setForeground(Color.DARK_GRAY);
    buttonPanel.add(titleLabel);
  }
  
  private void buildStartButton() {
    startButton = new JButton("Comenzar");
    startButton.setFocusPainted(false);
    startButton.setPreferredSize(new Dimension(120, 30));
    buttonPanel.add(startButton);
  }
  
  private void buildExitButton() {
    exitButton = new JButton("Salir");
    exitButton.setFocusPainted(false);
    exitButton.addActionListener(this::onExit);
    exitButton.setPreferredSize(new Dimension(120, 30));
    buttonPanel.add(exitButton);
  }

  private void onExit(ActionEvent e) {
    System.exit(0);
  }
}
