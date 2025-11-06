package com.isw.app.services;

import com.isw.app.models.Room;
import com.isw.app.repositories.RoomRepository;

public class RoomService {
  private final RoomRepository repository = new RoomRepository();

  public Room generate() {
    try {
      Room room = new Room();
      repository.save(room);
      return room;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
