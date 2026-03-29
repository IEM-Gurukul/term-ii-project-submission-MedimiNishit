package controllers;

import store.GuestStore;
import java.util.*;

public class GuestController {

    private final GuestStore store;

    public GuestController(GuestStore store) {
        this.store = store;
    }

    public Map<Integer, Map<String, String>> fetchAllGuests() {
        return store.getGuests();
    }

    public Map<String, String> fetchGuestById(int guestId) {
        return store.getGuestById(guestId);
    }

    public List<Map<String, String>> findGuestsByName(String searchTerm) {
        return store.lookupByName(searchTerm);
    }

    public int addGuest(String fullName, String phoneNumber) {
        return store.insertGuest(fullName, phoneNumber);
    }

    public boolean modifyGuest(int guestId, String updatedName, String updatedPhone) {
        return store.modifyGuest(guestId, updatedName, updatedPhone);
    }

    public boolean guestExists(int guestId) {
        return store.getGuestById(guestId) != null;
    }
}
