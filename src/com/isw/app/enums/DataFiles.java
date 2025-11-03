package com.isw.app.enums;

public enum DataFiles {
  ROOMS("rooms.txt"),
  HISTORY("history.txt");

  private final String path;
  private final String base = "src/com/isw/app/data/";

  DataFiles(String path) {
    this.path = base + path;
  }

  public String getPath() {
    return path;
  }
}
