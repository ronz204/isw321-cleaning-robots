package com.isw.app.repositories;

import java.io.IOException;
import java.io.BufferedWriter;
import com.isw.app.models.Robot;
import com.isw.app.enums.DataFile;
import com.isw.app.models.Decision;
import com.isw.app.helpers.BufferedHelper;
import com.isw.app.helpers.TxtQueryHelper;

public class HistoryRepository {
  private final DataFile file = DataFile.HISTORY;

  public void logStep(int stepNumber, Decision decision) {
    try (BufferedWriter writer = BufferedHelper.getWriter(file)) {
      Robot robot = decision.getRobot();

      TxtQueryHelper.writeDelimiter(writer);
      TxtQueryHelper.writeField(writer, "Paso #" + stepNumber);
      TxtQueryHelper.writeField(writer, robot.getUuid());

      if (decision.hasValidMovement()) {
        TxtQueryHelper.writeField(writer, robot.getCoord().getRow() + "," + robot.getCoord().getCol());
      } else {
        TxtQueryHelper.writeField(writer, "Sin movimiento");
      }

      TxtQueryHelper.writeField(writer, robot.getState().getLabel());
      TxtQueryHelper.writeField(writer, "Bateria: " + robot.getBattery() * 5 + "%");
      TxtQueryHelper.writeDelimiter(writer);
    } catch (IOException e) {
      System.err.println("Error al registrar paso: " + e.getMessage());
    }
  }
}
