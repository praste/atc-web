package person.pushkar.atc.rest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api/v1")
public class ApplicationConfig extends Application {

	public Set<Class<?>> getClasses() {
        return new HashSet<Class<?>>(Arrays.asList(AtcResource.class));
    }
}