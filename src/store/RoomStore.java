package store;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RoomStore {

    private final LinkedHashMap<Integer, Map<String, String>> roomTable = new LinkedHashMap<>();
    private static final String[] COLUMNS   = { "type", "price", "status" };
    private static final String   DATA_FILE  = "data/rooms.csv";

    public RoomStore() {
        ensureDataDir();
        readFromDisk();
    }

    private void ensureDataDir() {
        try { Files.createDirectories(Paths.get("data")); }
        catch (IOException ignored) {}
    }

    private void readFromDisk() {
        roomTable.clear();
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String row;
            while ((row = br.readLine()) != null) {
                row = row.trim();
                if (!row.isEmpty()) parseAndStore(row);
            }
        } catch (IOException e) {
            System.out.println("Warning: Could not read rooms data.");
        }
    }

    private void parseAndStore(String csvRow) {
        String[] parts = csvRow.split(",", -1);
        if (parts.length < 1) return;
        try {
            int key = Integer.parseInt(parts[0].trim());
            Map<String, String> record = new HashMap<>();
            for (int col = 0; col < COLUMNS.length && col + 1 < parts.length; col++)
                record.put(COLUMNS[col], parts[col + 1].trim());
            roomTable.put(key, record);
        } catch (NumberFormatException e) {
            System.out.println("Skipping malformed room row.");
        }
    }

    public LinkedHashMap<Integer, Map<String, String>> getRooms() {
        return roomTable;
    }

    public Map<String, String> getRoomById(int roomId) {
        Map<String, String> found = roomTable.get(roomId);
        if (found == null) return null;
        Map<String, String> copy = new HashMap<>(found);
        copy.put("id", String.valueOf(roomId));
        return copy;
    }

    private int nextAvailableId() {
        return roomTable.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
    }

    public boolean insertRoom(String category, String ratePerNight) {
        int newId = nextAvailableId();
        Map<String, String> entry = new HashMap<>();
        entry.put("type",   category);
        entry.put("price",  ratePerNight);
        entry.put("status", "available");
        roomTable.put(newId, entry);
        return persistToDisk();
    }

    public boolean dropRoom(int roomId) {
        if (!roomTable.containsKey(roomId)) return false;
        roomTable.remove(roomId);
        return persistToDisk();
    }

    public boolean updateStatus(int roomId, String newStatus) {
        if (!roomTable.containsKey(roomId)) return false;
        roomTable.get(roomId).put("status", newStatus);
        return persistToDisk();
    }

    private boolean persistToDisk() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Map.Entry<Integer, Map<String, String>> row : roomTable.entrySet()) {
                Map<String, String> r = row.getValue();
                bw.write(String.join(",",
                    String.valueOf(row.getKey()),
                    r.getOrDefault("type",   ""),
                    r.getOrDefault("price",  ""),
                    r.getOrDefault("status", "")
                ));
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error: Unable to write rooms data.");
            return false;
        }
    }
}
