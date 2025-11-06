package com.isw.app.presentation.views;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import com.isw.app.models.Room;
import com.isw.app.services.RoomService;
import com.isw.app.presentation.components.BoardRoom;
import com.isw.app.presentation.components.ControlPanel;

public class SimulatorView extends BaseView {
  private final RoomService roomService = new RoomService();

  public SimulatorView() {
    super(SimulatorView.class.getName());
  }

  private JFrame frame;
  private JPanel leftPanel;
  private JPanel centerPanel;
  private JPanel rightPanel;
  private BoardRoom boardRoom;
  private ControlPanel controlPanel;

  public void display() {
    buildFrame();
    buildLeftPanel();
    buildCenterPanel();
    buildRightPanel();
    buildBoardRoom();
    buildControlPanel();
    frame.setVisible(true);
  }

  private void buildFrame() {
    frame = new JFrame("Simulator");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1000, 600);
    frame.setLocationRelativeTo(null);
    frame.setLayout(new BorderLayout());
  }

  private void buildLeftPanel() {
    leftPanel = new JPanel();
    leftPanel.setBackground(Color.LIGHT_GRAY);
    leftPanel.setPreferredSize(new Dimension(200, 0));
    frame.add(leftPanel, BorderLayout.WEST);
  }

  private void buildCenterPanel() {
    centerPanel = new JPanel();
    centerPanel.setBackground(Color.WHITE);
    centerPanel.setLayout(new BorderLayout());
    frame.add(centerPanel, BorderLayout.CENTER);
  }

  private void buildRightPanel() {
    rightPanel = new JPanel();
    rightPanel.setBackground(Color.LIGHT_GRAY);
    rightPanel.setPreferredSize(new Dimension(250, 0));
    frame.add(rightPanel, BorderLayout.EAST);
  }

  private void buildBoardRoom() {
    boardRoom = new BoardRoom();
    centerPanel.add(boardRoom, BorderLayout.CENTER);
  }

  private void buildControlPanel() {
    controlPanel = new ControlPanel();
    controlPanel.setOnGenerate(this::onGenerateBoard);
    leftPanel.add(controlPanel, BorderLayout.CENTER);
  }

  private void onGenerateBoard() {
    Room room = roomService.generate();
    boardRoom.onUpdateRoom(room);
  }
}
