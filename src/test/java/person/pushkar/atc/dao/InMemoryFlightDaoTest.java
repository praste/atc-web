package person.pushkar.atc.dao;

import java.security.SecureRandom;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import person.pushkar.atc.BaseSpringTestCase;
import person.pushkar.atc.FlightDao;
import person.pushkar.atc.model.Flight;
import person.pushkar.atc.model.FlightDirection;
import person.pushkar.atc.model.FlightStatus;

public class InMemoryFlightDaoTest extends BaseSpringTestCase {
	
	@Autowired 
	private FlightDao flightDao;
	
	private Random rand = new SecureRandom();
	
	private AtomicInteger versionNumberGenerator = new AtomicInteger();
	
	@Test
	public void testAddFlight() {
		Flight flight = buildRandomFlight();
		
		int before = flightDao.getAll().size();
		
		flightDao.add(flight);
		
		int after = flightDao.getAll().size();
		Assert.assertEquals(before +1, after);
		
		Flight retrieved = flightDao.findById(flight.getId());
		
		Assert.assertEquals(flight.getId(), retrieved.getId());
		Assert.assertEquals(flight.getFlightDirection(), retrieved.getFlightDirection());
		Assert.assertEquals(flight.getVersionNumber(), retrieved.getVersionNumber());
	}
	
	@Test
	public void getAllFlights() {
		int before = flightDao.getAll().size();
		
		int flightsToAdd = rand.nextInt(10);
		
		for(int i = 0; i < flightsToAdd; i ++) {
			flightDao.add(buildRandomFlight());
		}
		
		int after = flightDao.getAll().size();
		
		Assert.assertEquals(before + flightsToAdd, after);
	}
	
	
	@Test 
	public void updateFlightStatus() {
		Flight flight = buildRandomFlight();
		
		String flightId = flight.getId();
		
		flightDao.add(flight);
		
		Flight flightRetrieved = flightDao.findById(flightId);
		Assert.assertTrue(flightRetrieved.getFlightStatus() == FlightStatus.AWAITING);
		
		flightDao.updateFlightStatus(flightRetrieved.getId(), FlightStatus.INFLIGHT);
		flightRetrieved = flightDao.findById(flightId);

		Assert.assertTrue(flightRetrieved.getFlightStatus() == FlightStatus.INFLIGHT);

		flightDao.updateFlightStatus(flightRetrieved.getId(), FlightStatus.SUCCESSFULL);
		flightRetrieved = flightDao.findById(flightId);
		Assert.assertTrue(flightRetrieved.getFlightStatus() == FlightStatus.SUCCESSFULL);
	}
	
	private Flight buildRandomFlight() {
		return new Flight(RandomStringUtils.randomAscii(5), 
				rand.nextBoolean() ? FlightDirection.LAND : FlightDirection.TAKE_OFF,
				versionNumberGenerator.incrementAndGet());
	}

}
