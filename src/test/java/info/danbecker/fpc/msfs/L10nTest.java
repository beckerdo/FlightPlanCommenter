package info.danbecker.fpc.msfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import info.danbecker.fpc.msfs.L10n.Leg;


public class L10nTest {
	@BeforeEach
    public void setup() {
	}
	
	@Test
    public void testPaths() throws URISyntaxException {
		// Various tests to confirm how Java handles files
		String testFileName = "en-US.germany.locPak"; // Provide a file in this project
		// System.out.println( "file name=" + testFileName );
		// URL url = ClassLoader.getSystemClassLoader().getResource(testFileName); // can find from project sub dirs
		// System.out.println( "url=" + url ); // file:/E:/computer/eclipse-workspace/FlightPlanCommenter/target/test-classes/en-US.germany.locPak		
		// URI uri = url.toURI();
		// System.out.println( "uri=" + uri ); // file:/E:/computer/eclipse-workspace/FlightPlanCommenter/target/test-classes/en-US.germany.locPak//
		// System.out.println( "uri path=" + uri.getPath() ); // /E:/computer/eclipse-workspace/FlightPlanCommenter/target/test-classes/en-US.germany.locPak//
		// URL to URI to File recommended. Careful of nulls from nonexisting files
		File file = Paths.get( ClassLoader.getSystemClassLoader().getResource(testFileName).toURI() ).toFile();
		File parent = Paths.get( file.getParent() ).toFile();
		// System.out.println( "file parent,name=" + parent + "," + file.getName() );
		// System.out.println( "all files in parent=" + Arrays.toString( parent.list() )); 
		String ext = testFileName.substring( testFileName.lastIndexOf( "." ) + 1); // no ext for files!
		String[] filtered = parent.list(new FilenameFilter() { // Can also use streams and BiPredicate
		    @Override
		    public boolean accept(File dir, String name) {
		        return name.endsWith(ext);
		    }
		});
		// System.out.println( "ext \"" + ext + "\" files in parent=" + Arrays.toString( filtered ));
		assertTrue( Arrays.asList( filtered ).contains( testFileName ), "test file in subdirectory");		
		
		
		// Path thisDir = Paths.get("."); // Project dir, not resource dir
		// System.out.println( ". path=" + thisDir ); // .
		// System.out.println( ". path normalized=" + thisDir.normalize() ); //
		// System.out.println( ". path absolute=" + thisDir.toAbsolutePath() ); // E:\computer\eclipse-workspace\FlightPlanCommenter\.
		// System.out.println( ". path absolute normalized=" + thisDir.toAbsolutePath().normalize() ); // E:\computer\eclipse-workspace\FlightPlanCommenter
	
		// No leading /, backward slash,  and escapes		
		// String windowsName = "E:\\computer\\eclipse-workspace\\FlightPlanCommenter\\target\\test-classes\\en-US.germany.locPak";
		String windowsName = file.getPath().replace( "/", "\\");
		// System.out.println( "windows fileName=" + windowsName );
		// System.out.println( "windows fileName normalize=" + Paths.get( windowsName ).normalize() ); // local file system slashes
		// System.out.println( "windows fileName exists=" + Paths.get( windowsName ).toFile().exists( ));
				
		// No leading /, forward slash, and no espaces
		String linuxName = "FlightPlanCommenter/target/test-classes/en-US.germany.locPak";
		// System.out.println( "linux fileName=" + linuxName ); 
		// System.out.println( "linux fileName normalize=" + Paths.get( linuxName ).normalize() ); // local file system slashes
		// System.out.println( "linux fileName exists=" + Paths.get( linuxName ).toFile().exists( ));
		assertTrue( Paths.get( windowsName ).normalize().toString().contains( Paths.get( linuxName ).normalize().toString()), "normalized file names on Windows and Linux");
	}

	@Test
    public void testConstructors() {
		// getClass().getResourceAsStream("foo.properties");
//		L10n msfsL10n = new L10n( FlightPlanCommenter.TESTINSTALL, FlightPlanCommenter.MSFSBUSHTRIP, FlightPlanCommenter.MSFSL10N );
//		
//		assertEquals( FlightPlanCommenter.TESTINSTALL + File.separator + FlightPlanCommenter.MSFSBUSHTRIP + File.separator + 
//				FlightPlanCommenter.MSFSL10N + L10n.MSFSLOCPAK, 
//				msfsL10n.getFullPath(), "constructed L10n path");
	}

	@Test
    public void testBasics() throws URISyntaxException, IOException {
		URL jsonTest = ClassLoader.getSystemClassLoader().getResource("en-US.germany.locPak");		
		L10n msfsL10n = new L10n( Paths.get( jsonTest.toURI() ).toFile().toString() );
		msfsL10n.readAsJson();
		
		assertEquals( "Germany Journey", msfsL10n.getTitle(), "title" ); 
		assertEquals( "Germany", msfsL10n.getLocation(), "location" ); 
		assertEquals( "Helgoland", msfsL10n.getPlanBegin(), "plan begin" ); 
		assertEquals( "Jolling", msfsL10n.getPlanEnd(), "plan end" );
		// assertTrue( msfsL10n.getPlanComment().startsWith( "This flight begins" ), "plan comment");		
	}

	@Test
    public void testLocations() throws URISyntaxException, IOException {
		URL jsonTest = ClassLoader.getSystemClassLoader().getResource("en-US.germany.locPak");		
		L10n msfsL10n = new L10n( Paths.get( jsonTest.toURI() ).toFile().toString() );
		msfsL10n.readAsJson();
		
		List<String> locations = msfsL10n.getLocations();
		assertNotNull( locations, "locations null");
		// System.out.println( "locations length=" + locations.size());
		assertEquals( locations.size(), msfsL10n.getLocationsLength(), "locations length" ); 
		assertEquals( "Trischen", msfsL10n.getLocation( 0 ), "locations get" ); 
		assertEquals( -1, msfsL10n.getLocationIndex( "Nowhere" ), "locations index" ); 
		assertEquals( 72, msfsL10n.getLocationIndex( "Jolling" ), "locations index" ); 		
	}

	@Test
    public void testLegs() throws URISyntaxException, IOException {
		URL jsonTest = ClassLoader.getSystemClassLoader().getResource("en-US.germany.locPak");		
		L10n msfsL10n = new L10n( Paths.get( jsonTest.toURI() ).toFile().toString() );
		msfsL10n.readAsJson();
		
		List<L10n.Leg> legs = msfsL10n.getLegs();
		assertNotNull( legs, "legs null");
		// System.out.println( "legs length=" + legs.size());
		assertEquals( legs.size(), msfsL10n.getLegsLength(), "legs length" );
		Leg first = msfsL10n.getLeg( 0 );
		assertEquals( "St Michaelisdonn", first.end, "leg get" ); 
		// assertEquals( -1, msfsL10n.getLocationIndex( new Leg() ), "locations index" ); 
	}

	@Test
    public void testLegsAustria() throws URISyntaxException, IOException {
		URL jsonTest = ClassLoader.getSystemClassLoader().getResource("en-US.austria.locPak");		
		// System.out.println( jsonTest );
		L10n msfsL10n = new L10n( Paths.get( jsonTest.toURI() ).toFile().toString() );
		msfsL10n.readAsJson();
		
		List<L10n.Leg> legs = msfsL10n.getLegs();
		assertNotNull( legs, "legs null");
		// System.out.println( "legs length=" + legs.size());
		assertEquals( legs.size(), msfsL10n.getLegsLength(), "legs length" );
		Leg first = msfsL10n.getLeg( 0 );
		assertEquals( "Linz", first.end, "leg get" ); 
		// assertEquals( -1, msfsL10n.getLocationIndex( new Leg() ), "locations index" ); 
	}
}