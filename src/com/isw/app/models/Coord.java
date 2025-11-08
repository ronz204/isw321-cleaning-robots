package com.isw.app.models;

public class Coord {
  private int row, col;
  
  public Coord(int row, int col) {
    this.row = row;
    this.col = col;
  }

  public int getRow() {
    return row;
  }

  public int getCol() {
    return col;
  }
  
  public int distanceTo(Coord other) {
    return Math.abs(this.row - other.row) + Math.abs(this.col - other.col);
  }

  public boolean isValidIn(int maxRows, int maxCols) {
    return row >= 0 && row < maxRows && col >= 0 && col < maxCols;
  }
}
