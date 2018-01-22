package person.pushkar.atc.service;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component
public class SemaphoreRunwayTokenManager implements RunwayTokenManager {
	
	private final int INITIAL_ACTIVE_RUNWAYS = 2; 
	private Semaphore runways = new Semaphore(INITIAL_ACTIVE_RUNWAYS, true);
	private AtomicInteger activeRunwaysCount = new AtomicInteger(INITIAL_ACTIVE_RUNWAYS);

	@Override
	public void commissionRunway(String id) {
		if(activeRunwaysCount.get() < MAX_RUNWAYS) {
			activeRunwaysCount.incrementAndGet();
			runways.release();
		} else {
			throw new IllegalStateException(
				String.format("No more runways to commission. All the %d runways are active", MAX_RUNWAYS));
		}
	}

	@Override
	public void decommissionRunway(String id) throws InterruptedException {
		if(activeRunwaysCount.get() == 0) {
			throw new IllegalStateException("No runways to decommission");
		} else {
			activeRunwaysCount.decrementAndGet();
			runways.acquire();
		}
	}

	@Override
	public int getActiveRunwaysCount() {
		return activeRunwaysCount.get();
	}

	@Override
	public void releaseRunway() {
		runways.release();
	}

	@Override
	public void acquireRunway() throws InterruptedException {
		runways.acquire();
	}
}
