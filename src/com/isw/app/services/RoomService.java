package com.isw.app.services;

import com.isw.app.models.Room;

public class RoomService {
  public static Room generate() {
    try {
      Room room = new Room();
      return room;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
