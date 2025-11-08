package com.isw.app.enums;

public enum RobotState {
  ACTIVE("Activo"),
  WAITING("Esperando"),
  INACTIVE("Inactivo"),
  CHARGING("Cargando"),
  CLEANING("Limpiando");

  private final String label;

  RobotState(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
