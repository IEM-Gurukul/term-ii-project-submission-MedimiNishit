import controllers.BookingController;
import controllers.GuestController;
import controllers.RoomController;
import store.BookingStore;
import store.GuestStore;
import store.RoomStore;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;

public class Main {

    private static final java.io.BufferedReader reader =
            new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

    private static RoomController    roomCtrl;
    private static GuestController   guestCtrl;
    private static BookingController bookingCtrl;

    public static void main(String[] args) {
        RoomStore    roomStore    = new RoomStore();
        GuestStore   guestStore   = new GuestStore();
        BookingStore bookingStore = new BookingStore();

        roomCtrl    = new RoomController(roomStore);
        guestCtrl   = new GuestController(guestStore);
        bookingCtrl = new BookingController(bookingStore, roomCtrl, guestCtrl);

        mainLoop();
    }

    private static void mainLoop() {
        while (true) {
            printMenu(new String[]{
                "Room Management",
                "Guest Management",
                "Booking Management",
                "Reports Dashboard",
                "Quit"
            });
            int choice = promptInt("Select: ");
            if      (choice == 1) handleRooms();
            else if (choice == 2) handleGuests();
            else if (choice == 3) handleBookings();
            else if (choice == 4) handleReports();
            else if (choice == 5) { System.out.println("Goodbye!"); break; }
            else                    System.out.println("Invalid selection.");
        }
    }

    private static void handleRooms() {
        printMenu(new String[]{
            "List all rooms",
            "List available rooms",
            "Find room by ID",
            "Add new room",
            "Delete room",
            "Back"
        });
        int opt = promptInt("Select: ");
        switch (opt) {
            case 1 -> displayAllRooms();
            case 2 -> displayAvailableRooms();
            case 3 -> displayRoomDetails();
            case 4 -> addNewRoom();
            case 5 -> deleteRoom();
            case 6 -> {}
            default -> System.out.println("Invalid.");
        }
    }

    private static void displayAllRooms() {
        Map<Integer, Map<String, String>> allRooms = roomCtrl.fetchAllRooms();
        if (allRooms.isEmpty()) { System.out.println("No rooms on record."); return; }
        System.out.println("\n--- All Rooms ---");
        for (Map.Entry<Integer, Map<String, String>> entry : allRooms.entrySet()) {
            int rid = entry.getKey();
            Map<String, String> data = entry.getValue();
            System.out.printf("[%d] %-10s | Rs.%-8s | %s%n",
                rid,
                data.getOrDefault("type", "N/A"),
                data.getOrDefault("price", "0"),
                data.getOrDefault("status", "N/A"));
        }
    }

    private static void displayAvailableRooms() {
        List<Map<String, String>> available = roomCtrl.fetchAvailableRooms();
        if (available.isEmpty()) { System.out.println("No rooms available."); return; }
        System.out.println("\n--- Available Rooms ---");
        for (Map<String, String> room : available)
            System.out.printf("[%s] %-10s | Rs.%s%n",
                room.get("id"),
                room.getOrDefault("type", "N/A"),
                room.getOrDefault("price", "0"));
    }

    private static void displayRoomDetails() {
        int rid = promptInt("Enter Room ID: ");
        Map<String, String> room = roomCtrl.fetchRoomById(rid);
        if (room == null) { System.out.println("Room not found."); return; }
        System.out.println("\n--- Room Details ---");
        System.out.println("ID     : " + rid);
        System.out.println("Type   : " + room.getOrDefault("type", "N/A"));
        System.out.println("Price  : Rs." + room.getOrDefault("price", "0"));
        System.out.println("Status : " + room.getOrDefault("status", "N/A"));
    }

    private static void addNewRoom() {
        String type  = promptString("Room type (Single/Double/Suite): ");
        String price = promptString("Price per night (Rs.): ");
        boolean ok = roomCtrl.createRoom(type, price);
        System.out.println(ok ? "Room added successfully." : "Failed to add room.");
    }

    private static void deleteRoom() {
        int rid = promptInt("Enter Room ID to delete: ");
        String confirm = promptString("Are you sure? (yes/no): ");
        if ("yes".equalsIgnoreCase(confirm)) {
            boolean ok = roomCtrl.deleteRoom(rid);
            System.out.println(ok ? "Room deleted." : "Could not delete room.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private static void handleGuests() {
        printMenu(new String[]{
            "List all guests",
            "Find guest by ID",
            "Search guests by name",
            "Register new guest",
            "Edit guest info",
            "Back"
        });
        int opt = promptInt("Select: ");
        switch (opt) {
            case 1 -> displayAllGuests();
            case 2 -> displayGuestDetails();
            case 3 -> searchGuests();
            case 4 -> registerGuest();
            case 5 -> editGuest();
            case 6 -> {}
            default -> System.out.println("Invalid.");
        }
    }

    private static void displayAllGuests() {
        Map<Integer, Map<String, String>> allGuests = guestCtrl.fetchAllGuests();
        if (allGuests.isEmpty()) { System.out.println("No guests registered."); return; }
        System.out.println("\n--- Guest List ---");
        for (Map.Entry<Integer, Map<String, String>> entry : allGuests.entrySet()) {
            Map<String, String> g = entry.getValue();
            System.out.printf("[%d] %-20s | %s%n",
                entry.getKey(),
                g.getOrDefault("name", "N/A"),
                g.getOrDefault("phone", "N/A"));
        }
    }

    private static void displayGuestDetails() {
        int gid = promptInt("Enter Guest ID: ");
        Map<String, String> g = guestCtrl.fetchGuestById(gid);
        if (g == null) { System.out.println("Guest not found."); return; }
        System.out.println("\n--- Guest Details ---");
        System.out.println("ID    : " + gid);
        System.out.println("Name  : " + g.getOrDefault("name", "N/A"));
        System.out.println("Phone : " + g.getOrDefault("phone", "N/A"));
    }

    private static void searchGuests() {
        String keyword = promptString("Enter name to search: ");
        List<Map<String, String>> results = guestCtrl.findGuestsByName(keyword);
        if (results.isEmpty()) { System.out.println("No matching guests."); return; }
        System.out.println("\n--- Search Results ---");
        for (Map<String, String> g : results)
            System.out.printf("[%s] %-20s | %s%n",
                g.get("id"),
                g.getOrDefault("name", "N/A"),
                g.getOrDefault("phone", "N/A"));
    }

    private static void registerGuest() {
        String name  = promptString("Full name: ");
        String phone = promptString("Phone number: ");
        int newId = guestCtrl.addGuest(name, phone);
        System.out.println("Guest registered with ID: " + newId);
    }

    private static void editGuest() {
        int gid = promptInt("Enter Guest ID to edit: ");
        Map<String, String> existing = guestCtrl.fetchGuestById(gid);
        if (existing == null) { System.out.println("Guest not found."); return; }
        String updatedName  = promptStringWithDefault(
            "Name [" + existing.getOrDefault("name","") + "]: ",
            existing.getOrDefault("name",""));
        String updatedPhone = promptStringWithDefault(
            "Phone [" + existing.getOrDefault("phone","") + "]: ",
            existing.getOrDefault("phone",""));
        boolean ok = guestCtrl.modifyGuest(gid, updatedName, updatedPhone);
        System.out.println(ok ? "Guest updated." : "Update failed.");
    }

    private static void handleBookings() {
        printMenu(new String[]{
            "Create booking",
            "Process checkout",
            "Cancel booking",
            "View booking details",
            "Bookings for a guest",
            "Back"
        });
        int opt = promptInt("Select: ");
        switch (opt) {
            case 1 -> createBooking();
            case 2 -> processCheckout();
            case 3 -> cancelBooking();
            case 4 -> viewBooking();
            case 5 -> viewGuestBookings();
            case 6 -> {}
            default -> System.out.println("Invalid.");
        }
    }

    private static void createBooking() {
        int gid  = promptInt("Guest ID: ");
        int rid  = promptInt("Room ID: ");
        String ci = promptString("Check-in date  (YYYY-MM-DD): ");
        String co = promptString("Check-out date (YYYY-MM-DD): ");
        String result = bookingCtrl.makeBooking(gid, rid, ci, co);
        System.out.println(result);
    }

    private static void processCheckout() {
        int bid = promptInt("Booking ID: ");
        System.out.println(bookingCtrl.performCheckout(bid));
    }

    private static void cancelBooking() {
        int bid = promptInt("Booking ID: ");
        System.out.println(bookingCtrl.cancelBooking(bid));
    }

    private static void viewBooking() {
        int bid = promptInt("Booking ID: ");
        Map<String, String> booking = bookingCtrl.fetchBookingById(bid);
        if (booking == null) { System.out.println("Booking not found."); return; }
        renderBooking(bid, booking);
    }

    private static void viewGuestBookings() {
        int gid = promptInt("Guest ID: ");
        List<Map<String, String>> list = bookingCtrl.fetchBookingsByGuest(gid);
        if (list.isEmpty()) { System.out.println("No bookings found for this guest."); return; }
        for (Map<String, String> b : list)
            renderBooking(Integer.parseInt(b.get("id")), b);
    }

    private static void handleReports() {
        printMenu(new String[]{
            "Active bookings report",
            "All bookings report",
            "Available rooms report",
            "Back"
        });
        int opt = promptInt("Select: ");
        switch (opt) {
            case 1 -> {
                List<Map<String, String>> active = bookingCtrl.fetchActiveBookings();
                if (active.isEmpty()) { System.out.println("No active bookings."); break; }
                System.out.println("\n=== Active Bookings ===");
                for (Map<String, String> b : active)
                    renderBooking(Integer.parseInt(b.get("id")), b);
            }
            case 2 -> {
                List<Map<String, String>> all = bookingCtrl.fetchAllBookings();
                if (all.isEmpty()) { System.out.println("No bookings on record."); break; }
                System.out.println("\n=== All Bookings ===");
                for (Map<String, String> b : all)
                    renderBooking(Integer.parseInt(b.get("id")), b);
            }
            case 3 -> displayAvailableRooms();
            case 4 -> {}
            default -> System.out.println("Invalid.");
        }
    }

    private static void renderBooking(int bookingId, Map<String, String> b) {
        System.out.println("------------------------------");
        System.out.println("Booking ID   : " + bookingId);
        System.out.println("Guest ID     : " + b.getOrDefault("guestId", "N/A"));
        System.out.println("Room ID      : " + b.getOrDefault("roomId", "N/A"));
        System.out.println("Check-in     : " + b.getOrDefault("checkIn", "N/A"));
        System.out.println("Check-out    : " + b.getOrDefault("checkOut", "N/A"));
        System.out.println("Total Amount : Rs." + b.getOrDefault("totalAmount", "0"));
        System.out.println("Status       : " + b.getOrDefault("status", "N/A"));
        System.out.println("------------------------------");
    }

    private static void printMenu(String[] options) {
        System.out.println();
        for (int i = 0; i < options.length; i++)
            System.out.printf("  %d. %s%n", i + 1, options[i]);
    }

    private static int promptInt(String label) {
        while (true) {
            System.out.print(label);
            try {
                String raw = reader.readLine();
                if (raw == null) continue;
                return Integer.parseInt(raw.trim());
            } catch (Exception e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static String promptString(String label) {
        System.out.print(label);
        try {
            String line = reader.readLine();
            return line == null ? "" : line.trim();
        } catch (Exception e) { return ""; }
    }

    private static String promptStringWithDefault(String label, String fallback) {
        String input = promptString(label);
        return input.isEmpty() ? fallback : input;
    }
}
