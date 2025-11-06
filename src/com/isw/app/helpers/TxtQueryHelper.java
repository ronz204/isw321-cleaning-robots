package com.isw.app.helpers;

import java.io.BufferedWriter;
import java.io.IOException;

public class TxtQueryHelper {
  private static final String DELIMITER = "$";

  public static void writeDelimiter(BufferedWriter writer) throws IOException {
    writer.write(DELIMITER);
    writer.newLine();
  }

  public static void writeField(BufferedWriter writer, String value) throws IOException {
    writer.write(value);
    writer.newLine();
  }
}
