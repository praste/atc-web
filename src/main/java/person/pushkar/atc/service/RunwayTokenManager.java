package person.pushkar.atc.service;


public interface RunwayTokenManager {
	
	public static final int MAX_RUNWAYS = 5;

	/**
	 * Puts a runway into commission
	 * @param id Identifier of the runway to commission
	 */
	public void commissionRunway(String id);
	
	/**
	 * Decommissions a runway
	 * @param id Identifier of the runway to decommission
	 */
	public void decommissionRunway(String id);
	
	/**
	 * Returns a count of active runways
	 * @return a count of active runways
	 */
	public int getActiveRunwaysCount();
	
	/**
	 * acquires a runway
	 */
	public void releaseRunway();
	
	/**
	 * releases runway
	 * @throws InterruptedException
	 */
	public void acquireRunway() throws InterruptedException;
}
