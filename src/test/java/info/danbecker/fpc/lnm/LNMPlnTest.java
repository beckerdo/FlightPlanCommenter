package info.danbecker.fpc.lnm;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import javax.xml.parsers.ParserConfigurationException;


public class LNMPlnTest {
	@BeforeEach
    public void setup() {
	}
	
	@Test
    public void testLNMPln() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
		String inputPlanName = "VFR Gardermoen (ENGM) to Sorkjosen (ENSR).lnmpln";
		String inputPlanFilename = 
		Paths.get( ClassLoader.getSystemClassLoader().getResource(inputPlanName).toURI() ).toFile().toString();

		
		LNMPln lnmPln = new LNMPln(inputPlanFilename);
		lnmPln.readInput();
		
		assertEquals( 1, lnmPln.getWaypointsCount(), "Waypoints element count");
	}

}