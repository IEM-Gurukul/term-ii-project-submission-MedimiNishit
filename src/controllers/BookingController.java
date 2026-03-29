package controllers;

import store.BookingStore;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class BookingController {

    private final BookingStore   store;
    private final RoomController roomCtrl;
    private final GuestController guestCtrl;

    public BookingController(BookingStore store, RoomController roomCtrl, GuestController guestCtrl) {
        this.store     = store;
        this.roomCtrl  = roomCtrl;
        this.guestCtrl = guestCtrl;
    }

    public String makeBooking(int guestId, int roomId, String arrivalDate, String departureDate) {
        if (!guestCtrl.guestExists(guestId))
            return "Error: No guest found with ID " + guestId + ".";

        if (!roomCtrl.checkRoomAvailability(roomId))
            return "Error: Room " + roomId + " is not available for booking.";

        LocalDate arrival, departure;
        try {
            arrival   = LocalDate.parse(arrivalDate);
            departure = LocalDate.parse(departureDate);
        } catch (DateTimeParseException e) {
            return "Error: Invalid date. Please use YYYY-MM-DD format.";
        }

        if (!departure.isAfter(arrival))
            return "Error: Departure date must be after arrival date.";

        long totalNights  = ChronoUnit.DAYS.between(arrival, departure);
        int  nightlyRate  = roomCtrl.getNightlyRate(roomId);
        int  amountDue    = (int)(totalNights * nightlyRate);

        int bookingId = store.insertBooking(
            String.valueOf(guestId),
            String.valueOf(roomId),
            arrivalDate,
            departureDate,
            String.valueOf(amountDue)
        );

        roomCtrl.reserveRoom(roomId);

        return String.format("Booking confirmed! ID: %d | Duration: %d night(s) | Total: Rs.%d",
            bookingId, totalNights, amountDue);
    }

    public String performCheckout(int bookingId) {
        Map<String, String> booking = store.getBookingById(bookingId);
        if (booking == null)
            return "Error: Booking ID " + bookingId + " not found.";
        if (!"active".equals(booking.get("status")))
            return "Error: This booking is not currently active.";

        int roomId = Integer.parseInt(booking.get("roomId"));
        store.markCheckedOut(bookingId);
        roomCtrl.releaseRoom(roomId);

        return "Checkout complete. Amount charged: Rs." + booking.getOrDefault("totalAmount", "0");
    }

    public String cancelBooking(int bookingId) {
        Map<String, String> booking = store.getBookingById(bookingId);
        if (booking == null)
            return "Error: Booking ID " + bookingId + " not found.";
        if (!"active".equals(booking.get("status")))
            return "Error: Only active bookings can be cancelled.";

        int roomId = Integer.parseInt(booking.get("roomId"));
        store.markCancelled(bookingId);
        roomCtrl.releaseRoom(roomId);

        return "Booking #" + bookingId + " has been successfully cancelled.";
    }

    public Map<String, String> fetchBookingById(int bookingId) {
        return store.getBookingById(bookingId);
    }

    public List<Map<String, String>> fetchActiveBookings() {
        return store.getActiveBookings();
    }

    public List<Map<String, String>> fetchAllBookings() {
        return store.getAllBookings();
    }

    public List<Map<String, String>> fetchBookingsByGuest(int guestId) {
        return store.getBookingsForGuest(guestId);
    }
}
