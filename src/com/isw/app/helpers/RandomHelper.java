package com.isw.app.helpers;

import java.util.Random;

public class RandomHelper {
  private static final Random random = new Random();

  public static int getRandomInt(int min, int max) {
    return random.nextInt(max - min + 1) + min;
  }
}
