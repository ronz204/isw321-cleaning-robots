package com.isw.app.enums;

import com.isw.app.helpers.RandomHelper;

public enum SectorType {
  DIRTY("Sucia"),
  CLEAN("Limpia"),
  RECHARGE("Recarga"),
  TEMPORARY("Temporal"),
  OBSTRUCTED("Obstruida");

  private final String label;
  private final char prefix;

  SectorType(String label) {
    this.label = label;
    this.prefix = label.charAt(0);
  }

  public String getLabel() {
    return label;
  }

  public char getPrefix() {
    return prefix;
  }

  public static SectorType getRandomType() {
    SectorType[] values = SectorType.values();
    return values[RandomHelper.getRandomInt(0, values.length - 1)];
  }
}
