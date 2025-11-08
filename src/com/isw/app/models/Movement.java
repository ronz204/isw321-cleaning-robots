package com.isw.app.models;

public class Movement {
  private Coord coord;
  private double score;

  public Movement(Coord coord, double score) {
    this.coord = coord;
    this.score = score;
  }

  public Coord getCoord() {
    return coord;
  }

  public double getScore() {
    return score;
  }
}
