package com.isw.app.repositories;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import com.isw.app.models.Room;
import com.isw.app.models.Sector;
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

  public void saveRoom(Room room) {
    try (BufferedWriter writer = this.getAppendWriter()) {
      writer.write("$");
      writer.newLine();
      
      writer.write(room.getUuid());
      writer.newLine();
      
      Sector[][] sectors = room.getSectors();
      for (int row = 0; row < room.getRows(); row++) {
        StringBuilder builder = new StringBuilder();

        for (int col = 0; col < room.getCols(); col++) {
          builder.append(sectors[row][col].getType().getPrefix());
          if (col < room.getCols() - 1) {
            builder.append(" ");
          }
        }

        writer.write(builder.toString());
        writer.newLine();
      }
      
      writer.write("$");
      writer.newLine();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
