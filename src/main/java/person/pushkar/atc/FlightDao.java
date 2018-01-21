package person.pushkar.atc;

import java.util.List;

import person.pushkar.atc.model.Flight;
import person.pushkar.atc.model.FlightStatus;

/**
 * Dao for managing flights
 * @author pushkar
 *
 */
public interface FlightDao {

	/**
	 * Adds flight to store
	 * @param flight
	 */
	public void add(Flight flight);
	
	/**
	 * Lists all the flights
	 * @return
	 */
	public List<Flight> getAll();
	
	/**
	 * Updates status of the flight
	 * @param id Id of the flight to change the status for 
	 * @param status Status to set on the flight
	 */
	public void updateFlightStatus(String id, FlightStatus status);
	
	/**
	 * Returns next flight to process
	 * @return Next flight to process
	 * @throws InterruptedException
	 */
	public Flight getNextFlight() throws InterruptedException;
	
	/**
	 * Finds flight by id
	 * @param id Id of the flight to find data for
	 * @return
	 */
	public Flight findById(String id);
}
