package com.isw.app.data;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class Rocket {
  public static void testReading() {
    try (FileReader fileReader = new FileReader("src/com/isw/app/data/rooms.txt");
        BufferedReader bufferedReader = new BufferedReader(fileReader)) {

      String line;
      while ((line = bufferedReader.readLine()) != null) {
        System.out.println(line);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void testWriting() {
    try (FileWriter fileWriter = new FileWriter("src/com/isw/app/data/rooms.txt", true);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

      bufferedWriter.newLine();
      bufferedWriter.write("==================");
      bufferedWriter.newLine();
      bufferedWriter.write("$ROO-3456");
      bufferedWriter.newLine();
      bufferedWriter.write("L L L L L L L L");
      bufferedWriter.newLine();
      bufferedWriter.write("L S O T R L L L");
      bufferedWriter.newLine();
      bufferedWriter.write("L L L S L L O L");
      bufferedWriter.newLine();
      bufferedWriter.write("==================");

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
