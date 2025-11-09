package com.isw.app.repositories;

import com.isw.app.models.Coord;
import com.isw.app.models.Robot;
import com.isw.app.enums.DataFile;
import com.isw.app.helpers.BufferedHelper;
import com.isw.app.helpers.TxtQueryHelper;
import java.io.BufferedWriter;
import java.io.IOException;

public class RobotRepository {
  private final DataFile file = DataFile.ROBOTS;

  public void save(Robot robot) throws IOException {
    try (BufferedWriter writer = BufferedHelper.getWriter(file)) {
      TxtQueryHelper.writeDelimiter(writer);
      TxtQueryHelper.writeField(writer, "UUID: " + robot.getUuid());
      Coord coord = robot.getCoord();
      TxtQueryHelper.writeField(writer, "Posicion: " + coord.getRow() + "," + coord.getCol());
      TxtQueryHelper.writeField(writer, "Bateria: " + robot.getBattery() * 5 + "%");
      TxtQueryHelper.writeField(writer, "Estado: " + robot.getState().getLabel());
      TxtQueryHelper.writeDelimiter(writer);
    }
  }
}
