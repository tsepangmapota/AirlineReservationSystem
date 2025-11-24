package com.airline.model;


import javafx.beans.property.*;

public class Flight {
    private final StringProperty flightId;
    private final StringProperty flightCode;
    private final StringProperty source;
    private final StringProperty destination;
    private final StringProperty departure;
    private final StringProperty arrival;
    private final IntegerProperty capacity;
    private final IntegerProperty availableSeats;

    public Flight(String flightId, String flightCode, String source, String destination,
                  String departure, String arrival, int capacity, int availableSeats) {
        this.flightId = new SimpleStringProperty(flightId);
        this.flightCode = new SimpleStringProperty(flightCode);
        this.source = new SimpleStringProperty(source);
        this.destination = new SimpleStringProperty(destination);
        this.departure = new SimpleStringProperty(departure);
        this.arrival = new SimpleStringProperty(arrival);
        this.capacity = new SimpleIntegerProperty(capacity);
        this.availableSeats = new SimpleIntegerProperty(availableSeats);
    }

    // Getter methods
    public String getFlightId() { return flightId.get(); }
    public String getFlightCode() { return flightCode.get(); }
    public String getSource() { return source.get(); }
    public String getDestination() { return destination.get(); }
    public String getDeparture() { return departure.get(); }
    public String getArrival() { return arrival.get(); }
    public int getCapacity() { return capacity.get(); }
    public int getAvailableSeats() { return availableSeats.get(); }

    // Property methods
    public StringProperty flightIdProperty() { return flightId; }
    public StringProperty flightCodeProperty() { return flightCode; }
    public StringProperty sourceProperty() { return source; }
    public StringProperty destinationProperty() { return destination; }
    public StringProperty departureProperty() { return departure; }
    public StringProperty arrivalProperty() { return arrival; }
    public IntegerProperty capacityProperty() { return capacity; }
    public IntegerProperty availableSeatsProperty() { return availableSeats; }
}
