package person.pushkar.atc.rest;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;

import person.pushkar.atc.model.Flight;
import person.pushkar.atc.model.FlightDirection;
import person.pushkar.atc.model.FlightLedger;
import person.pushkar.atc.service.AtcService;
import person.pushkar.atc.service.RunwayTokenManager;

@Path("/atc")
public class AtcResource {

	@Autowired
    private AtcService atcService;
	
	@Autowired
	private RunwayTokenManager runwayTokenManager;
	
	AtomicInteger versionGenerator = new AtomicInteger();
	
	public AtcResource() {
	}
	
	@PostConstruct
	private void startAtcService() {
		atcService.startService();
	}
	
    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public FlightLedger status() {
    	return atcService.getStatus();
    }

    @POST
    @Path("takeOff/airplane/{id : .+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void takeOff(@PathParam("id") String id) {
        Flight flight = new Flight(id, FlightDirection.TAKE_OFF, versionGenerator.incrementAndGet());
        atcService.queueFlight(flight);
    }

    @POST
    @Path("landing/airplane/{id : .+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void landing(@PathParam("id") String id) {
        Flight flight = new Flight(id, FlightDirection.LAND, versionGenerator.incrementAndGet());
        atcService.queueFlight(flight);
    }
    
    
    @POST
    @Path("runway/commission/{id : .+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void commissionRunway(@PathParam("id") String id) {
    	runwayTokenManager.commissionRunway(id);
    }
    
    @POST
    @Path("runway/decommission/{id : .+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void decommissionRunway(@PathParam("id") String id) throws InterruptedException {
    	runwayTokenManager.decommissionRunway(id);
    }
}
