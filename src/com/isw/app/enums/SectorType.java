package com.isw.app.enums;

import java.awt.Color;
import com.isw.app.helpers.RandomHelper;

public enum SectorType {
  DIRTY("Sucia", 30, Color.ORANGE),
  CLEAN("Limpia", 50, Color.WHITE),
  RECHARGE("Recarga", 10, Color.GREEN),
  TEMPORARY("Temporal", 10, Color.GRAY),
  OBSTRUCTED("Obstruida", 10, Color.BLACK);

  private final String label;
  private final char prefix;
  private final Color color;
  private final int weight;

  SectorType(String label, int weight, Color color) {
    this.label = label;
    this.color = color;
    this.weight = weight;
    this.prefix = label.charAt(0);
  }

  public int getWeight() {
    return weight;
  }

  public char getPrefix() {
    return prefix;
  }

  public Color getColor() {
    return color;
  }

  public String getLabel() {
    return label;
  }

  public static SectorType getRandomType() {
    int totalWeight = 0;
    for (SectorType type : SectorType.values()) {
      totalWeight += type.getWeight();
    }
    
    int value = RandomHelper.getRandomInt(1, totalWeight);
    int cumulative = 0;

    for (SectorType type : SectorType.values()) {
      cumulative += type.getWeight();
      if (value <= cumulative) return type;
    }

    return CLEAN;
  }
}
