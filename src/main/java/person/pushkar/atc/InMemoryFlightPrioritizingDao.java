package person.pushkar.atc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import org.springframework.stereotype.Component;

import person.pushkar.atc.model.Flight;
import person.pushkar.atc.model.FlightStatus;
import person.pushkar.atc.util.FlightPriortizer;

/**
 * DAO implementation that stores the flights in memory 
 * @author pushkar
 *
 */
@Component
public class InMemoryFlightPrioritizingDao implements FlightDao {

	private FlightPriortizer flightPriortizer = new FlightPriortizer();
	
	private PriorityBlockingQueue<Flight> flightQueue = new PriorityBlockingQueue<>(10, flightPriortizer);

	private Map<String, Flight> flightsDB = new ConcurrentHashMap<>();
	
	@Override
	public void add(Flight flight) {
		// TODO we can add more sophisticated code with following rules in the real world 
		// 1. A flight can not take off twice in row without landing
		// 2. A flight can not land twice in row without taking off
		if(flightsDB.containsKey(flight.getId())) {
			throw new IllegalArgumentException(
				String.format("Flight with id %s is/was already scheduled", flight.getId()));
		}
		flightsDB.put(flight.getId(), flight);
		flightQueue.add(flight);
	}

	@Override
	public List<Flight> getAll() {
		return new ArrayList<>(flightsDB.values());
	}

	@Override
	public void updateFlightStatus(String id, FlightStatus status) {
		Flight flight = flightsDB.get(id);
		if(flight != null) {
			flight.setFlightStatus(status);
		}
	}

	@Override
	public Flight getNextFlight() throws InterruptedException {
		Flight flight = flightQueue.take();
		return flight.clone();
	}

	@Override
	public Flight findById(String id) {
		Flight flight = flightsDB.get(id);
		return flight.clone();
	}

	public Comparator<Flight> getFlightPriortizer() {
		return flightPriortizer;
	}
}
