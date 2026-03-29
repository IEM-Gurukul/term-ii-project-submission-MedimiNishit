package store;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class BookingStore {

    private final LinkedHashMap<Integer, Map<String, String>> bookingTable = new LinkedHashMap<>();
    private static final String[] COLUMNS   = { "guestId", "roomId", "checkIn", "checkOut", "status", "totalAmount" };
    private static final String   DATA_FILE  = "data/bookings.csv";

    public BookingStore() {
        ensureDataDir();
        readFromDisk();
    }

    private void ensureDataDir() {
        try { Files.createDirectories(Paths.get("data")); }
        catch (IOException ignored) {}
    }

    private void readFromDisk() {
        bookingTable.clear();
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String row;
            while ((row = br.readLine()) != null) {
                row = row.trim();
                if (!row.isEmpty()) parseAndStore(row);
            }
        } catch (IOException e) {
            System.out.println("Warning: Could not read bookings data.");
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
            bookingTable.put(key, record);
        } catch (NumberFormatException e) {
            System.out.println("Skipping malformed booking row.");
        }
    }

    public Map<String, String> getBookingById(int bookingId) {
        Map<String, String> found = bookingTable.get(bookingId);
        if (found == null) return null;
        Map<String, String> copy = new HashMap<>(found);
        copy.put("id", String.valueOf(bookingId));
        return copy;
    }

    private int nextAvailableId() {
        return bookingTable.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
    }

    public int insertBooking(String guestId, String roomId,
                             String checkIn, String checkOut, String totalAmt) {
        int newId = nextAvailableId();
        Map<String, String> entry = new LinkedHashMap<>();
        entry.put("guestId",     guestId);
        entry.put("roomId",      roomId);
        entry.put("checkIn",     checkIn);
        entry.put("checkOut",    checkOut);
        entry.put("status",      "active");
        entry.put("totalAmount", totalAmt);
        bookingTable.put(newId, entry);
        persistToDisk();
        return newId;
    }

    public boolean markCheckedOut(int bookingId) {
        if (!bookingTable.containsKey(bookingId)) return false;
        bookingTable.get(bookingId).put("status", "checkedout");
        return persistToDisk();
    }

    public boolean markCancelled(int bookingId) {
        if (!bookingTable.containsKey(bookingId)) return false;
        bookingTable.get(bookingId).put("status", "cancelled");
        return persistToDisk();
    }

    public List<Map<String, String>> getBookingsForGuest(int guestId) {
        String gid = String.valueOf(guestId);
        return bookingTable.entrySet().stream()
            .filter(e -> gid.equals(e.getValue().getOrDefault("guestId", "")))
            .map(e -> {
                Map<String, String> b = new HashMap<>(e.getValue());
                b.put("id", String.valueOf(e.getKey()));
                return b;
            })
            .collect(Collectors.toList());
    }

    public List<Map<String, String>> getActiveBookings() {
        return bookingTable.entrySet().stream()
            .filter(e -> "active".equals(e.getValue().getOrDefault("status", "")))
            .map(e -> {
                Map<String, String> b = new HashMap<>(e.getValue());
                b.put("id", String.valueOf(e.getKey()));
                return b;
            })
            .collect(Collectors.toList());
    }

    public List<Map<String, String>> getAllBookings() {
        return bookingTable.entrySet().stream()
            .map(e -> {
                Map<String, String> b = new HashMap<>(e.getValue());
                b.put("id", String.valueOf(e.getKey()));
                return b;
            })
            .collect(Collectors.toList());
    }

    private boolean persistToDisk() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Map.Entry<Integer, Map<String, String>> row : bookingTable.entrySet()) {
                Map<String, String> b = row.getValue();
                bw.write(String.join(",",
                    String.valueOf(row.getKey()),
                    b.getOrDefault("guestId",     ""),
                    b.getOrDefault("roomId",      ""),
                    b.getOrDefault("checkIn",     ""),
                    b.getOrDefault("checkOut",    ""),
                    b.getOrDefault("status",      ""),
                    b.getOrDefault("totalAmount", "")
                ));
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error: Unable to write bookings data.");
            return false;
        }
    }
}
