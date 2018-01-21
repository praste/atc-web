package person.pushkar.atc.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

public class SemaphoreRunwayTokenManagerTest {

	private SemaphoreRunwayTokenManager runwayTokenManager = new SemaphoreRunwayTokenManager();
	
	@Test
	public void testCommissionARunway() {
		int before = runwayTokenManager.getActiveRunwaysCount();
		runwayTokenManager.commissionRunway(RandomStringUtils.randomAscii(5));
		Assert.assertEquals(before + 1, runwayTokenManager.getActiveRunwaysCount());
	}
	
	@Test
	public void decommissionARunway() {
		int before = runwayTokenManager.getActiveRunwaysCount();
		runwayTokenManager.decommissionRunway(RandomStringUtils.randomAscii(5));
		Assert.assertEquals(before - 1, runwayTokenManager.getActiveRunwaysCount());
	}
	
	@Test(expected=IllegalStateException.class)
	public void ensureDecomssionRunwaysDoesDropBelowZero() {
		int activeRunwaysCount = runwayTokenManager.getActiveRunwaysCount();
		for(int i = 0; i < activeRunwaysCount  + 1; i++) {
			runwayTokenManager.decommissionRunway(RandomStringUtils.randomAscii(5));
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void ensureComssionRunwaysDoesGoAboveCapacity() {
		while(runwayTokenManager.getActiveRunwaysCount() <= SemaphoreRunwayTokenManager.MAX_RUNWAYS) {
			runwayTokenManager.decommissionRunway(RandomStringUtils.randomAscii(5));
		}
	}
}
