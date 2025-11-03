package com.isw.app.repos;

import java.io.IOException;
import java.io.BufferedReader;
import com.isw.app.enums.DataFile;

public class RoomRepository extends BaseRepository {
  public RoomRepository() {
    super(DataFile.ROOMS);
  }

  public void getAllRooms() {
    try (BufferedReader reader = this.getReader()) {
      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
