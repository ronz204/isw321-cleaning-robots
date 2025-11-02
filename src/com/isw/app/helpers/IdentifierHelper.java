package com.isw.app.helpers;

public class IdentifierHelper {
  public static String generate(String prefix) {
    int number = RandomHelper.getRandomInt(1000, 9999);
    return String.format("%s-%04d", prefix, number);
  }
}
