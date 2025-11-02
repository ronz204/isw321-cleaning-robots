package com.isw.app.views;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.event.ActionEvent;

public class SimulatorView extends BaseView {
  public SimulatorView() {
    super(SimulatorView.class.getName());
  }

  private JFrame frame;
  private JButton stopButton;
  private JButton startButton;

  public void display() {
    buildFrame();
    buildStopButton();
    buildStartButton();
    frame.setVisible(true);
  }

  private void buildFrame() {
    frame = new JFrame("Simulator");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(800, 600);
    frame.setLocationRelativeTo(null);
    frame.setLayout(null);
  }

  private void buildStartButton() {
    startButton = new JButton("Start");
    startButton.setBounds(50, 50, 100, 30);
    startButton.addActionListener(this::onStart);
    frame.add(startButton);
  }

  private void buildStopButton() {
    stopButton = new JButton("Stop");
    stopButton.setBounds(200, 50, 100, 30);
    stopButton.addActionListener(this::onStop);
    frame.add(stopButton);
  }

  private void onStart(ActionEvent e) {
    logger.info("Simulation started.");
  }

  private void onStop(ActionEvent e) {
    logger.info("Simulation stopped.");
  }
}
