package com.isw.app.enums;

import java.nio.file.Paths;

public enum DataFile {
  ROOMS("rooms.txt"),
  REPORTS("reports.txt"),
  HISTORY("history.txt");

  private final String filename;

  DataFile(String filename) {
    this.filename = filename;
  }

  public String getPath() {
    return Paths.get("src", "com", "isw", "app", "data", filename).toString();
  }
}
