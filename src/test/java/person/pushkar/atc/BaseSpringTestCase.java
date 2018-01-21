package person.pushkar.atc;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import person.pushkar.atc.config.TestConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfiguration.class }, loader=AnnotationConfigContextLoader.class)
@Ignore
public class BaseSpringTestCase {

}
