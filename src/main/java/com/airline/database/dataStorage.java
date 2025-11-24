package com.airline.database;

import com.airline.models.Customer;
import com.airline.models.Flight;
import com.airline.models.Reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DataStorage {
    // In-memory data storage
    private static final List<Customer> customers = new ArrayList<>();
    private static final List<Flight> flights = new ArrayList<>();
    private static final List<Reservation> reservations = new ArrayList<>();

    // ID counters
    private static final AtomicInteger customerIdCounter = new AtomicInteger(1);
    private static final AtomicInteger reservationIdCounter = new AtomicInteger(1);

    static {
        initializeSampleData();
    }

    private static void initializeSampleData() {
        // Initialize sample flights
        flights.add(new Flight("AI101", "Air India", "ECO", 20, 150,
                "R001", "Delhi", "Mumbai",
                LocalDateTime.now().plusDays(1).withHour(8).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)));

        flights.add(new Flight("AI102", "Air India", "EXE", 30, 120,
                "R002", "Mumbai", "Delhi",
                LocalDateTime.now().plusDays(1).withHour(14).withMinute(0),
                LocalDateTime.now().plusDays(1).withHour(16).withMinute(0)));

        flights.add(new Flight("SG201", "SpiceJet", "ECO", 15, 130,
                "R003", "Delhi", "Bangalore",
                LocalDateTime.now().plusDays(2).withHour(9).withMinute(0),
                LocalDateTime.now().plusDays(2).withHour(11).withMinute(30)));

        flights.add(new Flight("6E301", "IndiGo", "ECO", 25, 140,
                "R004", "Delhi", "Chennai",
                LocalDateTime.now().plusDays(2).withHour(10).withMinute(0),
                LocalDateTime.now().plusDays(2).withHour(12).withMinute(30)));

        // Initialize sample customers
        customers.add(createCustomer("Lerato Lesholu", "Mohale Makubela", "Male",
                LocalDate.of(1985, 5, 15), "123 Main St, New York",
                "1234567890", "Software Engineer", "None"));

        customers.add(createCustomer("Katleho Rothi", "thato Rothi", "Female"
                LocalDate.of(1978, 8, 22), "Thabong, Maseru",
                "0987654321", "Doctor", "Senior Citizen"));

        customers.add(createCustomer("Thato Mphama", "Botle Thakholi", "Female",
                LocalDate.of(1995, 3, 10), "Roma, Motse_mocha",
                "1122334455", "Student", "Student"));

        System.out.println("Sample data initialized: " + flights.size() + " flights, " +
                customers.size() + " customers");
    }

    private static Customer createCustomer(String name, String fatherName, String gender,
                                           LocalDate dob, String address, String phone,
                                           String profession, String concession) {
        Customer customer = new Customer(name, fatherName, gender, dob, address, phone,
                profession, "Security", concession);
        customer.setCustCode(customerIdCounter.getAndIncrement());
        return customer;
    }

    // Customer operations
    public static List<Customer> getAllCustomers() {
        return new ArrayList<>(customers);
    }

    public static void addCustomer(Customer customer) {
        customer.setCustCode(customerIdCounter.getAndIncrement());
        customers.add(customer);
    }

    public static void updateCustomer(Customer updatedCustomer) {
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getCustCode() == updatedCustomer.getCustCode()) {
                customers.set(i, updatedCustomer);
                break;
            }
        }
    }

    public static void deleteCustomer(int customerId) {
        customers.removeIf(c -> c.getCustCode() == customerId);
    }

    // Flight operations
    public static List<Flight> getAllFlights() {
        return new ArrayList<>(flights);
    }

    public static Flight getFlightByCode(String flightCode) {
        return flights.stream()
                .filter(f -> f.getFCode().equalsIgnoreCase(flightCode))
                .findFirst()
                .orElse(null);
    }

    public static List<Flight> searchFlights(String source, String destination) {
        List<Flight> list = new ArrayList<>();
        for (Flight f : flights) {
            if (f.getSPlace().equalsIgnoreCase(source) &&
                    f.getDPlace().equalsIgnoreCase(destination)) {
                list.add(f);
            }
        }
        return list;
    }

    public static void addFlight(Flight flight) {
        flights.add(flight);
    }

    // Reservation operations
    public static List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }

    public static void addReservation(Reservation reservation) {
        reservation.setPnr(reservationIdCounter.getAndIncrement());
        reservations.add(reservation);
    }

    public static void cancelReservation(int pnr) {
        Reservation reservation = reservations.stream()
                .filter(r -> r.getPnr() == pnr)
                .findFirst()
                .orElse(null);

        if (reservation != null) {
            reservation.setStatus("Cancelled");
        }
    }

    public static List<Reservation> getReservationsByCustomer(int customerId) {
        List<Reservation> list = new ArrayList<>();
        for (Reservation r : reservations) {
            if (r.getCustCode() == customerId) {
                list.add(r);
            }
        }
        return list;
    }

    // Statistics
    public static int getTotalReservations() {
        return reservations.size();
    }

    public static int getConfirmedReservations() {
        return (int) reservations.stream()
                .filter(r -> "Confirmed".equals(r.getStatus()))
                .count();
    }

    public static int getWaitingReservations() {
        return (int) reservations.stream()
                .filter(r -> "Waiting".equals(r.getStatus()))
                .count();
    }

    public static int getCancelledReservations() {
        return (int) reservations.stream()
                .filter(r -> "Cancelled".equals(r.getStatus()))
                .count();
    }
}
