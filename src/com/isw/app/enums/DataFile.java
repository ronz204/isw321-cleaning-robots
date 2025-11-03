package com.isw.app.enums;

public enum DataFile {
  ROOMS("rooms.txt"),
  REPORTS("reports.txt"),
  HISTORY("history.txt");

  private final String path;
  private final String base = "src/com/isw/app/data/";

  DataFile(String path) {
    this.path = base + path;
  }

  public String getPath() {
    return path;
  }
}
