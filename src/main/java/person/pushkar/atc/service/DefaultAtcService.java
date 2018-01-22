package person.pushkar.atc.service;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import person.pushkar.atc.FlightDao;
import person.pushkar.atc.model.Flight;
import person.pushkar.atc.model.FlightDirection;
import person.pushkar.atc.model.FlightLedger;
import person.pushkar.atc.model.FlightStatus;

@Component
@Service
public class DefaultAtcService implements AtcService {
	
	private static transient final Logger LOG = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	private long startTime = System.currentTimeMillis();

	private static final long TIME_ON_RUNWAY = TimeUnit.SECONDS.toMillis(30);
	
	private volatile boolean isActive = false;
	
	@Autowired
	private FlightDao flightDao;
	
	@Autowired
	private RunwayTokenManager  runwayTokenManager;
	
	private ExecutorService schedular = Executors.newSingleThreadExecutor();
	
	private FlightProcessor flightProcessor =  new FlightProcessor();
	
	/**
	 * Flight processor to handle the transition for flights from AWAITING -> INFLIGHT -> SUCCESSFUL.
	 * Each transition is handled in a separate thread.
	 * @author pushkar
	 *
	 */
	private  class FlightProcessor {
		private ExecutorService executorService =  
			Executors.newFixedThreadPool(RunwayTokenManager.MAX_RUNWAYS);
		
		public void process(Flight flight) {
			executorService.submit(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					
					// update flight status and add it to processedFlights list
					LOG.info(String.format("Flight %s is %s\n", 
							flight.getId(),
							flight.getFlightDirection() == FlightDirection.LAND ? "landing" : "taking off"));
					
					flightDao.updateFlightStatus(flight.getId(), FlightStatus.INFLIGHT);
					

					// wait for flight to take off / land
					Thread.sleep(TIME_ON_RUNWAY);

					// update flight status to successful
					flightDao.updateFlightStatus(flight.getId(), FlightStatus.SUCCESSFULL);

					LOG.info(String.format("Flight %s successfully %s\n", 
							flight.getId(),
							flight.getFlightDirection() == FlightDirection.LAND ? "landed" : "took off"));
					
					// release runway
					runwayTokenManager.releaseRunway();
						
					return null;
				}
			});
		}
		
		public void shutdown() {
			executorService.shutdown();
		}
	}
	
	public void setFlightDao(FlightDao flightDao) {
		this.flightDao = flightDao;
	}
	
	@Override
	public void queueFlight(Flight flight) {
		flightDao.add(flight);
	}

	@Override
	public FlightLedger getStatus() {
		long timeSinceStart = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
		
		return new FlightLedger(timeSinceStart, flightDao.getAll());
	}
	
	public synchronized void startService() {
		if(isActive) {
			LOG.info("Services is already started");
			return;
		}
		isActive = true;
		schedular.submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				while(isActive) {
					// wait for runway
					runwayTokenManager.acquireRunway();
					
					Flight flight = flightDao.getNextFlight();
					
					LOG.info(String.format("Scheduling flight %s for %s \n",  
							flight.getId(),
							flight.getFlightDirection() == FlightDirection.LAND ? "landing" : "take off"));
					
					flightProcessor.process(flight);
				}
				
				return null;
			}
		});
	}
	
	@PreDestroy
	public synchronized void stopService() {
		isActive = false;
		// ensure all runways are available
		schedular.shutdownNow();
		
		// wait for all the inflight flights to finish 
		flightProcessor.shutdown();
	}
	
	@Override
	public long getFlightProcessingTime() {
		return TIME_ON_RUNWAY;
	}

}


