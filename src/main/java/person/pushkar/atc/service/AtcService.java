package person.pushkar.atc.service;

import person.pushkar.atc.model.Flight;
import person.pushkar.atc.model.FlightLedger;

/**
 * Interface for the Air Traffic Control.
 * @author pushkar
 *
 */
public interface AtcService {
	
	/**
	 * Method to return status of the all flights in the system.
	 * @return Structure representing status of the all the flights and time elapsed from the start of the service
	 */
	public FlightLedger getStatus();
	
	/**
	 * Queue a flight for landing/take off
	 * @param flight Flight to queue
	 */
	public void queueFlight(Flight flight);
	
	/**
	 * starts the service should be called by the consumer of the interface
	 */
	public void startService();
	
	
	/**
	 * returns time in milliseconds required to takeoff/land 
	 * @return
	 */
	public long getFlightProcessingTime();
	
	/**
	 * Stops the service. waits for all the current INFLIGT flights to takeoff/land successfully
	 */
	public void stopService();
}