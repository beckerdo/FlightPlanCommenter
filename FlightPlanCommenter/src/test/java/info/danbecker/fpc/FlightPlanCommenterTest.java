package info.danbecker.fpc;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import info.danbecker.fpc.lnm.LNMPln;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FlightPlanCommenterTest {
	@BeforeEach
    public void setup() {
	}
	
	@Test
    public void testParseGatherOptions() throws ParseException, URISyntaxException, IOException {
		String [] args = new String[] { 
			"-op", "abc.lnplm", 
			"-ic", "en-US.norway.locPak",
			"-ip", "VFR Gardermoen (ENGM) to Sorkjosen (ENSR).lnmpln",
		};
		FlightPlanCommenter.parseGatherOptions( args );
			
		assertEquals(args[ 1 ],  FlightPlanCommenter.outputPlanFile, "output plan name" );
		String expectedPlanFile = 
			Paths.get( ClassLoader.getSystemClassLoader().getResource(args[5]).toURI() ).toFile().toString();
		assertEquals( expectedPlanFile, FlightPlanCommenter.inputPlanFile, "input plan name" );
		String expectedLocFile = 
			Paths.get( ClassLoader.getSystemClassLoader().getResource(args[3]).toURI() ).toFile().toString();
		assertEquals( expectedLocFile, FlightPlanCommenter.inputCommentFile, "input comment name" );
	}
	
	
	@Test
    public void testFunction() throws Exception {
		String [] args = new String[] {"-ip", "VFR Gardermoen (ENGM) to Sorkjosen (ENSR).lnmpln", "-ic", "en-US.norway.locPak", "-sa"};
		
		FlightPlanCommenter.main( args );

		String inputPlanFile = Paths.get( ClassLoader.getSystemClassLoader().getResource(args[ 1 ]).toURI() ).toFile().toString();
		assertTrue( Files.exists( Path.of( inputPlanFile )), "input plan file exists" ); 
		assertTrue( Files.isReadable( Path.of( inputPlanFile )), "input plan file readable" ); 

		String outputPlanFile = args[ 1 ].replaceAll( LNMPln.LNMPLN_EXT, "" );
		outputPlanFile += FlightPlanCommenter.OUTPUT_SIGNATURE + LNMPln.LNMPLN_EXT;
		outputPlanFile = Paths.get( ClassLoader.getSystemClassLoader().getResource(outputPlanFile).toURI() ).toFile().toString();
		// System.out.println( "Output plan file=" + outputPlanFile );
		
		// Files will show in target/test classes output
		assertTrue( Files.exists( Path.of( outputPlanFile )), "output plan file exists" ); 
		assertTrue( Files.isReadable( Path.of( outputPlanFile )), "output plan file readable" );
		// Should test date stamp, header contents, comments
		
		String legPlanFile = args[ 1 ].replaceAll( LNMPln.LNMPLN_EXT, "" );
		legPlanFile += FlightPlanCommenter.OUTPUT_SIGNATURE + " " + LNMPln.LNMPLN_LEG + String.format( "%02d", 0 ) + LNMPln.LNMPLN_EXT;
		legPlanFile = Paths.get( ClassLoader.getSystemClassLoader().getResource(legPlanFile).toURI() ).toFile().toString();
		// System.out.println( "Leg plan file=" + legPlanFile );
		
		assertTrue( Files.exists( Path.of( legPlanFile )), "output leg file exists" ); 
		assertTrue( Files.isReadable( Path.of( legPlanFile )), "output leg file readable" );
		// Should test number of legs date stamp, header contents, comments

	}
}