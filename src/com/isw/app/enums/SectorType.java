package com.isw.app.enums;

import com.isw.app.helpers.RandomHelper;

public enum SectorType {
  DIRTY("Sucia", 30),
  CLEAN("Limpia", 50),
  RECHARGE("Recarga", 10),
  TEMPORARY("Temporal", 10),
  OBSTRUCTED("Obstruida", 10);

  private final String label;
  private final char prefix;
  private final int weight;

  SectorType(String label, int weight) {
    this.label = label;
    this.weight = weight;
    this.prefix = label.charAt(0);
  }

  public int getWeight() {
    return weight;
  }

  public char getPrefix() {
    return prefix;
  }

  public String getLabel() {
    return label;
  }

  public static SectorType getRandomType() {
    int value = RandomHelper.getRandomInt(1, 100);
    int cumulative = 0;

    for (SectorType type : SectorType.values()) {
      cumulative += type.getWeight();
      if (value <= cumulative) return type;
    }

    return CLEAN;
  }
}
