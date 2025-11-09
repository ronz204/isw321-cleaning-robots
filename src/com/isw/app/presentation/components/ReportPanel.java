package com.isw.app.presentation.components;

import java.awt.Font;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.SwingConstants;
import com.isw.app.models.Cleaning;
import javax.swing.border.EmptyBorder;
import com.isw.app.services.CleaningService;

public class ReportPanel extends JPanel {

  private JLabel titleLabel;
  private JPanel contentPanel;
  private JLabel totalDirtySectorsLabel;
  private JLabel totalDirtySectorsValue;
  private JLabel cleanedSectorsLabel;
  private JLabel cleanedSectorsValue;
  private JLabel cleaningPercentageLabel;
  private JLabel cleaningPercentageValue;
  private JLabel missionStatusLabel;
  private JLabel missionStatusValue;

  public ReportPanel() {
    buildContainer();
    buildTitleLabel();
    buildContentPanel();
    buildStatisticItems();
  }

  private void buildContainer() {
    this.setLayout(new BorderLayout());
    this.setBackground(Color.LIGHT_GRAY);
    this.setBorder(new EmptyBorder(10, 10, 10, 10));
  }

  private void buildTitleLabel() {
    titleLabel = new JLabel("Reporte de Limpieza");
    titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
    titleLabel.setForeground(Color.DARK_GRAY);
    this.add(titleLabel, BorderLayout.NORTH);
  }

  private void buildContentPanel() {
    contentPanel = new JPanel();
    contentPanel.setBackground(Color.LIGHT_GRAY);
    contentPanel.setLayout(new GridLayout(4, 1, 0, 15));
    contentPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
    this.add(contentPanel, BorderLayout.CENTER);
  }

  private void buildStatisticItems() {
    buildTotalDirtySectorsItem();
    buildCleanedSectorsItem();
    buildCleaningPercentageItem();
    buildMissionStatusItem();
  }

  private void buildTotalDirtySectorsItem() {
    JPanel itemPanel = createStatisticItemPanel();
    
    totalDirtySectorsLabel = new JLabel("Espacios sucios totales:");
    totalDirtySectorsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
    totalDirtySectorsLabel.setForeground(Color.DARK_GRAY);
    
    totalDirtySectorsValue = new JLabel("0");
    totalDirtySectorsValue.setFont(new Font("Arial", Font.BOLD, 12));
    totalDirtySectorsValue.setForeground(Color.BLUE);
    
    itemPanel.add(totalDirtySectorsLabel);
    itemPanel.add(totalDirtySectorsValue);
    contentPanel.add(itemPanel);
  }

  private void buildCleanedSectorsItem() {
    JPanel itemPanel = createStatisticItemPanel();
    
    cleanedSectorsLabel = new JLabel("Espacios limpiados:");
    cleanedSectorsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
    cleanedSectorsLabel.setForeground(Color.DARK_GRAY);
    
    cleanedSectorsValue = new JLabel("0");
    cleanedSectorsValue.setFont(new Font("Arial", Font.BOLD, 12));
    cleanedSectorsValue.setForeground(Color.GREEN);
    
    itemPanel.add(cleanedSectorsLabel);
    itemPanel.add(cleanedSectorsValue);
    contentPanel.add(itemPanel);
  }

  private void buildCleaningPercentageItem() {
    JPanel itemPanel = createStatisticItemPanel();
    
    cleaningPercentageLabel = new JLabel("Porcentaje de limpieza:");
    cleaningPercentageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
    cleaningPercentageLabel.setForeground(Color.DARK_GRAY);
    
    cleaningPercentageValue = new JLabel("0%");
    cleaningPercentageValue.setFont(new Font("Arial", Font.BOLD, 12));
    cleaningPercentageValue.setForeground(Color.RED);
    
    itemPanel.add(cleaningPercentageLabel);
    itemPanel.add(cleaningPercentageValue);
    contentPanel.add(itemPanel);
  }

  private void buildMissionStatusItem() {
    JPanel itemPanel = createStatisticItemPanel();
    
    missionStatusLabel = new JLabel("Estado de la misión:");
    missionStatusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
    missionStatusLabel.setForeground(Color.DARK_GRAY);
    
    missionStatusValue = new JLabel("Pendiente");
    missionStatusValue.setFont(new Font("Arial", Font.BOLD, 12));
    missionStatusValue.setForeground(Color.GRAY);
    
    itemPanel.add(missionStatusLabel);
    itemPanel.add(missionStatusValue);
    contentPanel.add(itemPanel);
  }

  private JPanel createStatisticItemPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    panel.setBackground(Color.LIGHT_GRAY);
    return panel;
  }

  public void setTotalDirtySectors(int total) {
    totalDirtySectorsValue.setText(String.valueOf(total));
  }

  public void setCleanedSectors(int cleaned) {
    cleanedSectorsValue.setText(String.valueOf(cleaned));
  }

  public void setCleaningPercentage(double percentage) {
    cleaningPercentageValue.setText(String.format("%.1f%%", percentage));
    updatePercentageColor(percentage);
  }

  public void setMissionStatus(String status) {
    missionStatusValue.setText(status);
    updateStatusColor(status);
  }

  public void updateReport(int totalDirty, int cleaned, double percentage, String status) {
    setTotalDirtySectors(totalDirty);
    setCleanedSectors(cleaned);
    setCleaningPercentage(percentage);
    setMissionStatus(status);
  }

  public void updateFromCleaning(Cleaning cleaning, CleaningService service) {
    if (cleaning == null || service == null) {
      resetReport();
      return;
    }

    int totalDirty = cleaning.getInitialDirtySectors();
    int cleaned = service.getCleanedSectors(cleaning);
    double percentage = service.getCleaningPercentage(cleaning);
    String status = service.getMissionStatus(cleaning);

    updateReport(totalDirty, cleaned, percentage, status);
  }

  private void updatePercentageColor(double percentage) {
    if (percentage >= 80.0) {
      cleaningPercentageValue.setForeground(Color.GREEN);
    } else if (percentage >= 50.0) {
      cleaningPercentageValue.setForeground(Color.ORANGE);
    } else {
      cleaningPercentageValue.setForeground(Color.RED);
    }
  }

  private void updateStatusColor(String status) {
    switch (status.toLowerCase()) {
      case "completada":
        missionStatusValue.setForeground(Color.GREEN);
        break;
      case "aceptable":
        missionStatusValue.setForeground(Color.ORANGE);
        break;
      case "fallida":
        missionStatusValue.setForeground(Color.RED);
        break;
      case "en progreso":
        missionStatusValue.setForeground(Color.BLUE);
        break;
      default:
        missionStatusValue.setForeground(Color.GRAY);
        break;
    }
  }

  public void resetReport() {
    setTotalDirtySectors(0);
    setCleanedSectors(0);
    setCleaningPercentage(0.0);
    setMissionStatus("Pendiente");
  }
}
