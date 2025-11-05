package com.isw.app.repositories;

import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import com.isw.app.enums.DataFile;

public class BaseRepository {
  protected final String DELIMITER = "$";
  protected DataFile file;

  public BaseRepository(DataFile file) {
    this.file = file;
  }

  protected BufferedReader getReader() throws IOException {
    FileReader reader = new FileReader(this.file.getPath());
    return new BufferedReader(reader);
  }

  protected BufferedWriter getWriter() throws IOException {
    FileWriter writer = new FileWriter(this.file.getPath());
    return new BufferedWriter(writer);
  }

  protected BufferedWriter getAppendWriter() throws IOException {
    FileWriter writer = new FileWriter(this.file.getPath(), true);
    return new BufferedWriter(writer);
  }

  protected void writeDelimiter(BufferedWriter writer) throws IOException {
    writer.write(DELIMITER);
    writer.newLine();
  }

  protected void writeField(BufferedWriter writer, String value) throws IOException {
    writer.write(value);
    writer.newLine();
  }
}
