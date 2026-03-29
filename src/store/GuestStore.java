package store;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class GuestStore {

    private final LinkedHashMap<Integer, Map<String, String>> guestTable = new LinkedHashMap<>();
    private static final String[] COLUMNS   = { "name", "phone" };
    private static final String   DATA_FILE  = "data/guests.csv";

    public GuestStore() {
        ensureDataDir();
        readFromDisk();
    }

    private void ensureDataDir() {
        try { Files.createDirectories(Paths.get("data")); }
        catch (IOException ignored) {}
    }

    private void readFromDisk() {
        guestTable.clear();
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String row;
            while ((row = br.readLine()) != null) {
                row = row.trim();
                if (!row.isEmpty()) parseAndStore(row);
            }
        } catch (IOException e) {
            System.out.println("Warning: Could not read guests data.");
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
            guestTable.put(key, record);
        } catch (NumberFormatException e) {
            System.out.println("Skipping malformed guest row.");
        }
    }

    public LinkedHashMap<Integer, Map<String, String>> getGuests() {
        return guestTable;
    }

    public Map<String, String> getGuestById(int guestId) {
        Map<String, String> found = guestTable.get(guestId);
        if (found == null) return null;
        Map<String, String> copy = new HashMap<>(found);
        copy.put("id", String.valueOf(guestId));
        return copy;
    }

    public List<Map<String, String>> lookupByName(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return guestTable.entrySet().stream()
            .filter(e -> e.getValue().getOrDefault("name","").toLowerCase().contains(lowerKeyword))
            .map(e -> {
                Map<String, String> match = new HashMap<>(e.getValue());
                match.put("id", String.valueOf(e.getKey()));
                return match;
            })
            .collect(Collectors.toList());
    }

    private int nextAvailableId() {
        return guestTable.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
    }

    public int insertGuest(String fullName, String contactPhone) {
        int newId = nextAvailableId();
        Map<String, String> entry = new HashMap<>();
        entry.put("name",  fullName);
        entry.put("phone", contactPhone);
        guestTable.put(newId, entry);
        persistToDisk();
        return newId;
    }

    public boolean modifyGuest(int guestId, String fullName, String contactPhone) {
        if (!guestTable.containsKey(guestId)) return false;
        Map<String, String> record = guestTable.get(guestId);
        record.put("name",  fullName);
        record.put("phone", contactPhone);
        return persistToDisk();
    }

    private boolean persistToDisk() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Map.Entry<Integer, Map<String, String>> row : guestTable.entrySet()) {
                Map<String, String> g = row.getValue();
                bw.write(String.join(",",
                    String.valueOf(row.getKey()),
                    g.getOrDefault("name",  ""),
                    g.getOrDefault("phone", "")
                ));
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error: Unable to write guests data.");
            return false;
        }
    }
}
