package person.pushkar.atc.util;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomStringUtils;

import person.pushkar.atc.model.Flight;
import person.pushkar.atc.model.FlightDirection;

public class FlightBuilderUtil {

	private static Random rand = new SecureRandom();
	
	private static AtomicInteger versionNumberGenerator = new AtomicInteger();
	
	public static Flight buildRandomFlight() {
		return new Flight(RandomStringUtils.randomAscii(5), 
				rand.nextBoolean() ? FlightDirection.LAND : FlightDirection.TAKE_OFF,
				versionNumberGenerator.incrementAndGet());
	}

}
