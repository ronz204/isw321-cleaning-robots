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
      writeDelimiter(writer);
      writeField(writer, room.getUuid());
      writeSectorsData(writer, room);
      writeDelimiter(writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void writeSectorsData(BufferedWriter writer, Room room) throws IOException {
    Sector[][] sectors = room.getSectors();

    for (int row = 0; row < room.getRows(); row++) {
      String sectorRow = buildSectorRow(sectors[row], room.getCols());
      writeField(writer, sectorRow);
    }
  }

  private String buildSectorRow(Sector[] sectorRow, int cols) {
    StringBuilder builder = new StringBuilder();

    for (int col = 0; col < cols; col++) {
      builder.append(sectorRow[col].getType().getPrefix());
      if (col < cols - 1) {
        builder.append(" ");
      }
    }

    return builder.toString();
  }
}
