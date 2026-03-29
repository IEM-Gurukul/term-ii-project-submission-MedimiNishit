package controllers;

import store.RoomStore;
import java.util.*;

public class RoomController {

    private final RoomStore store;

    public RoomController(RoomStore store) {
        this.store = store;
    }

    public Map<Integer, Map<String, String>> fetchAllRooms() {
        return store.getRooms();
    }

    public Map<String, String> fetchRoomById(int roomId) {
        return store.getRoomById(roomId);
    }

    public List<Map<String, String>> fetchAvailableRooms() {
        List<Map<String, String>> result = new ArrayList<>();
        for (Map.Entry<Integer, Map<String, String>> entry : store.getRooms().entrySet()) {
            String currentStatus = entry.getValue().getOrDefault("status", "");
            if ("available".equalsIgnoreCase(currentStatus)) {
                Map<String, String> roomCopy = new HashMap<>(entry.getValue());
                roomCopy.put("id", String.valueOf(entry.getKey()));
                result.add(roomCopy);
            }
        }
        return result;
    }

    public boolean checkRoomAvailability(int roomId) {
        Map<String, String> room = store.getRoomById(roomId);
        return room != null && "available".equalsIgnoreCase(room.getOrDefault("status", ""));
    }

    public boolean reserveRoom(int roomId) {
        return store.updateStatus(roomId, "booked");
    }

    public boolean releaseRoom(int roomId) {
        return store.updateStatus(roomId, "available");
    }

    public boolean createRoom(String category, String nightlyRate) {
        return store.insertRoom(category, nightlyRate);
    }

    public boolean deleteRoom(int roomId) {
        return store.dropRoom(roomId);
    }

    public int getNightlyRate(int roomId) {
        Map<String, String> room = store.getRoomById(roomId);
        if (room == null) return 0;
        try {
            return Integer.parseInt(room.getOrDefault("price", "0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
