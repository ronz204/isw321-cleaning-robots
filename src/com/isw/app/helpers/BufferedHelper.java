package com.isw.app.helpers;

import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import com.isw.app.enums.DataFile;

public class BufferedHelper {

  public static BufferedReader getReader(DataFile file) throws IOException {
    FileReader reader = new FileReader(file.getPath());
    return new BufferedReader(reader);
  }

  public static BufferedWriter getWriter(DataFile file) throws IOException {
    FileWriter writer = new FileWriter(file.getPath(), true);
    return new BufferedWriter(writer);
  }
}
