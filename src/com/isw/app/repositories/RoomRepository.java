package com.isw.app.repositories;

import java.io.IOException;
import java.io.BufferedWriter;
import com.isw.app.models.Room;
import com.isw.app.models.Sector;
import com.isw.app.enums.DataFile;
import com.isw.app.helpers.BufferedHelper;
import com.isw.app.helpers.TxtQueryHelper;

public class RoomRepository {
  private final DataFile file = DataFile.ROOMS;

  public void save(Room room) throws IOException {
    try (BufferedWriter writer = BufferedHelper.getWriter(file)) {
      TxtQueryHelper.writeDelimiter(writer);
      TxtQueryHelper.writeField(writer, "UUID: " + room.getUuid());
      TxtQueryHelper.writeField(writer, "Dimensiones: " + room.getRows() + "x" + room.getCols());
      TxtQueryHelper.writeField(writer, "Sectores:");
      writeSectorsData(writer, room);
      TxtQueryHelper.writeDelimiter(writer);
    }
  }

  private void writeSectorsData(BufferedWriter writer, Room room) throws IOException {
    Sector[][] sectors = room.getSectors();

    for (int row = 0; row < room.getRows(); row++) {
      String sectorRow = buildSectorRow(sectors[row], room.getCols());
      TxtQueryHelper.writeField(writer, "  Fila " + row + ": " + sectorRow);
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
