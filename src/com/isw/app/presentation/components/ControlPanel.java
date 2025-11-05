package com.isw.app.presentation.components;

import java.awt.Font;
import java.awt.Color;
import java.lang.Runnable;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;

public class ControlPanel extends JPanel {

  private JLabel titleLabel;
  private JButton exitButton;
  private JButton placeButton;
  private JButton simulateButton;
  private JButton generateButton;
  private JPanel buttonPanel;

  private Runnable onGenerateCall;

  public ControlPanel() {
    buildContainer();
    buildButtonPanel();
    buildTitleLabel();
    buildGenerateButton();
    buildPlaceButton();
    buildSimulateButton();
    buildExitButton();
  }

  private void buildContainer() {
    this.setLayout(new BorderLayout());
  }

  private void buildButtonPanel() {
    buttonPanel = new JPanel();
    buttonPanel.setBackground(Color.LIGHT_GRAY);
    buttonPanel.setLayout(new GridLayout(5, 1, 0, 10));
    this.add(buttonPanel, BorderLayout.CENTER);
  }

  private void buildTitleLabel() {
    titleLabel = new JLabel("Cleaning Robots");
    titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
    titleLabel.setForeground(Color.DARK_GRAY);
    buttonPanel.add(titleLabel);
  }

  private void buildGenerateButton() {
    generateButton = new JButton("Generar Tablero");
    generateButton.setFocusPainted(false);
    generateButton.addActionListener(this::onGenerate);
    generateButton.setPreferredSize(new Dimension(140, 30));
    buttonPanel.add(generateButton);
  }

  private void buildPlaceButton() {
    placeButton = new JButton("Colocar Robots");
    placeButton.setFocusPainted(false);
    placeButton.setPreferredSize(new Dimension(140, 30));
    buttonPanel.add(placeButton);
  }

  private void buildSimulateButton() {
    simulateButton = new JButton("Simular Limpieza");
    simulateButton.setFocusPainted(false);
    simulateButton.setPreferredSize(new Dimension(140, 30));
    buttonPanel.add(simulateButton);
  }

  private void buildExitButton() {
    exitButton = new JButton("Salir");
    exitButton.setFocusPainted(false);
    exitButton.addActionListener(this::onExit);
    exitButton.setPreferredSize(new Dimension(140, 30));
    buttonPanel.add(exitButton);
  }

  public void setOnGenerate(Runnable callback) {
    this.onGenerateCall = callback;
  }

  private void onGenerate(ActionEvent e) {
    if (this.onGenerateCall != null) {
      this.onGenerateCall.run();
    }
  }

  private void onExit(ActionEvent e) {
    System.exit(0);
  }
}
