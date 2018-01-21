package person.pushkar.atc.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;

import person.pushkar.atc.FlightDao;
import person.pushkar.atc.model.Flight;
import person.pushkar.atc.model.FlightDirection;
import person.pushkar.atc.model.FlightLedger;
import person.pushkar.atc.model.FlightStatus;

public class AtcServiceTest {
	
	@InjectMocks
	private DefaultAtcService atcService;
	
	@Spy
	private SemaphoreRunwayTokenManager runwayTokenManager;
	
	@Mock
	private FlightDao flightDao;
	
	private AtomicInteger versionGenerator = new AtomicInteger();
	
	@Before
	public void setup() {
	    MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testServiceLifeCycleSingleRunway() throws InterruptedException {
		// make only one runway active
		if(runwayTokenManager.getActiveRunwaysCount() < 1) {
			runwayTokenManager.commissionRunway(RandomStringUtils.randomAscii(5));
		} else {
			while(runwayTokenManager.getActiveRunwaysCount() != 1) {
				runwayTokenManager.decommissionRunway(RandomStringUtils.randomAscii(5));
			}
		}
		
		// enqueue two flights to take off flight and one to land.
		// Finish order should be takeOff, land and takeOff 
		Flight takeOffFlight1 = 
			new Flight(String.valueOf(1), 
				FlightDirection.TAKE_OFF, 
				versionGenerator.incrementAndGet());
		Flight takeOffFlight2 = 
			new Flight(String.valueOf(2), 
			FlightDirection.TAKE_OFF, 
			versionGenerator.incrementAndGet());
		Flight landingFlight1 = 
			new Flight(String.valueOf(3), 
			FlightDirection.LAND, 
			versionGenerator.incrementAndGet());
		
		Map<String, Flight> flightsDB = new HashMap<>();
		flightsDB.put(takeOffFlight1.getId(), takeOffFlight1);
		flightsDB.put(takeOffFlight2.getId(), takeOffFlight2);
		flightsDB.put(landingFlight1.getId(), landingFlight1);
		
		Mockito.doReturn(Arrays.asList(takeOffFlight1, takeOffFlight2,landingFlight1)).when(flightDao).getAll();
		
		Mockito.when(flightDao.getNextFlight()).thenReturn(takeOffFlight1).thenReturn(landingFlight1).thenReturn(takeOffFlight2);
		
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Flight flight = flightsDB.get(invocation.getArgumentAt(0, String.class));
				FlightStatus status = invocation.getArgumentAt(1, FlightStatus.class);
				flight.setFlightStatus(status);
				
				return null;
			}
		}). when(flightDao).updateFlightStatus(Matchers.any(String.class), Matchers.any(FlightStatus.class));
		
		// now start the service
		atcService.startService();
		
		long timeFirstFlightQueued = System.currentTimeMillis();
		long flightProcessingTime = atcService.getFlightProcessingTime();

		atcService.queueFlight(takeOffFlight1);
		atcService.queueFlight(takeOffFlight2);
		atcService.queueFlight(landingFlight1);
		
		// ensure time is less than takeoff/landing time and that flight 1 is taking off
		FlightLedger ledger = atcService.getStatus();
		Assert.assertTrue(ledger.getTime() - timeFirstFlightQueued < atcService.getFlightProcessingTime());
		Assert.assertEquals(0, ledger.getInFlightLanding().size());
		Assert.assertEquals(1, ledger.getInFlightTakeOff().size());
		Assert.assertEquals(1, ledger.getWaitingForTakeOff().size());
		Assert.assertEquals(1, ledger.getWaitingForLanding().size());
		Assert.assertEquals(0, ledger.getSuccessfullyLanded().size());
		Assert.assertEquals(0, ledger.getSuccessfullyTookOff().size());
		Assert.assertEquals(0, ledger.getInFlightLanding().size());
		Assert.assertEquals(takeOffFlight1.getId(), ledger.getInFlightTakeOff().get(0).getId());
		
		// sleep long enough for takeOffFlight1 to finish and next flight to start processing
		Thread.sleep(flightProcessingTime + flightProcessingTime / 4);
		
		ledger = atcService.getStatus();
		Assert.assertTrue(ledger.toString(), ledger.getTime() - timeFirstFlightQueued < atcService.getFlightProcessingTime());
		Assert.assertEquals(ledger.toString(), 1, ledger.getInFlightLanding().size());
		Assert.assertEquals(ledger.toString(), 0, ledger.getInFlightTakeOff().size());
		Assert.assertEquals(ledger.toString(), 1, ledger.getWaitingForTakeOff().size());
		Assert.assertEquals(ledger.toString(), 0, ledger.getWaitingForLanding().size());
		Assert.assertEquals(ledger.toString(), 0, ledger.getSuccessfullyLanded().size());
		Assert.assertEquals(ledger.toString(), 1, ledger.getSuccessfullyTookOff().size());
		Assert.assertEquals(ledger.toString(), landingFlight1.getId(), ledger.getInFlightLanding().get(0).getId());
		
		// sleep long enough for takeOffFlight1 to finish and next flight to start processing
		Thread.sleep(flightProcessingTime + flightProcessingTime /4);
		
		ledger = atcService.getStatus();
		Assert.assertTrue(ledger.getTime() - timeFirstFlightQueued < atcService.getFlightProcessingTime());
		Assert.assertEquals(0, ledger.getInFlightLanding().size());
		Assert.assertEquals(1, ledger.getInFlightTakeOff().size());
		Assert.assertEquals(0, ledger.getWaitingForTakeOff().size());
		Assert.assertEquals(0, ledger.getWaitingForLanding().size());
		Assert.assertEquals(1, ledger.getSuccessfullyLanded().size());
		Assert.assertEquals(1, ledger.getSuccessfullyTookOff().size());
		Assert.assertEquals(takeOffFlight2.getId(), ledger.getInFlightTakeOff().get(0).getId());
		
		atcService.stopService();
		
		// sleep long enough for takeOffFlight1 to finish and next flight to start processing
		Thread.sleep(flightProcessingTime + flightProcessingTime /4);
		
		ledger = atcService.getStatus();
		Assert.assertTrue(ledger.getTime() - timeFirstFlightQueued < atcService.getFlightProcessingTime());
		Assert.assertEquals(0, ledger.getInFlightLanding().size());
		Assert.assertEquals(0, ledger.getInFlightTakeOff().size());
		Assert.assertEquals(0, ledger.getWaitingForTakeOff().size());
		Assert.assertEquals(0, ledger.getWaitingForLanding().size());
		Assert.assertEquals(1, ledger.getSuccessfullyLanded().size());
		Assert.assertEquals(2, ledger.getSuccessfullyTookOff().size());
	}
}
