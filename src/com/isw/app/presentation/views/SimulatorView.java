package com.isw.app.presentation.views;

import java.util.List;
import java.awt.Color;
import javax.swing.Timer;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JOptionPane;
import com.isw.app.models.Room;
import com.isw.app.models.Robot;
import com.isw.app.models.Cleaning;
import com.isw.app.models.StepResult;
import com.isw.app.services.CleaningService;
import com.isw.app.presentation.components.BoardRoom;
import com.isw.app.presentation.components.ControlPanel;

public class SimulatorView extends BaseView {
  private final CleaningService cleaningService = new CleaningService();

  private Cleaning cleaning;
  private Timer simulationTimer;

  private JFrame frame;
  private JPanel leftPanel;
  private JPanel rightPanel;
  private JPanel centerPanel;

  private BoardRoom boardRoom;
  private ControlPanel controlPanel;

  public SimulatorView() {
    super(SimulatorView.class.getName());
  }

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
    controlPanel.setOnPlaced(this::onPlaceRobots);
    controlPanel.setOnSimulate(this::onStartSimulation);
    leftPanel.add(controlPanel, BorderLayout.CENTER);
  }

  private void onGenerateBoard() {
    Room room = cleaningService.generateRoom();
    if (room != null) {
      cleaning = new Cleaning(room, null);
      boardRoom.onUpdateRoom(room);
    }
  }

  private void onPlaceRobots() {
    if (cleaning == null || cleaning.getRoom() == null) {
      return;
    }

    List<Robot> robots = cleaningService.generateRobots(cleaning.getRoom());
    cleaning = new Cleaning(cleaning.getRoom(), robots);
    boardRoom.onUpdateRobots(robots);
  }

  private void onStartSimulation() {
    if (cleaning == null || !cleaning.isValid()) {
      return;
    }

    if (cleaning.isActive()) {
      stopSimulation();
    } else {
      startSimulation();
    }
  }

  private void startSimulation() {
    cleaningService.startCleaning(cleaning);

    simulationTimer = new Timer(500, e -> {
      StepResult result = cleaningService.executeStep(cleaning);

      if (result != null) {
        boardRoom.onUpdateRobots(result.getRobots());

        if (result.isComplete()) {
          stopSimulation();
          showSimulationResults();
        }
      }
    });

    simulationTimer.start();
  }

  private void showSimulationResults() {
    List<Robot> robots = cleaning.getRobots();
    Room room = cleaning.getRoom();

    int cleanSectors = room.getSectorCounter().getOrDefault(com.isw.app.enums.SectorType.CLEAN, 0);

    String message = String.format(
        "🤖 SIMULACIÓN COMPLETADA 🤖\n\n" +
            "📊 Estadísticas:\n" +
            "═══════════════════════════\n" +
            "🤖 Robots totales: %d\n" +
            "🟢 Robots activos: %d\n" +
            "🧽 Sectores limpiados: %d/%d\n" +
            "⏱️ Total de pasos: %d\n" +
            "✨ Progreso total: %.1f%%",
        robots.size(),
        (int) robots.stream().filter(r -> r.getBattery() > 0).count(),
        cleanSectors,
        room.getTotalSectors(),
        cleaning.getTotalSteps(),
        cleaning.getCompletionPercentage());

    JOptionPane.showMessageDialog(frame, message, "Resultados de la Simulación", JOptionPane.INFORMATION_MESSAGE);
  }

  private void stopSimulation() {
    if (simulationTimer != null) {
      simulationTimer.stop();
    }
    if (cleaning != null) {
      cleaningService.stopCleaning(cleaning);
    }
  }
}
