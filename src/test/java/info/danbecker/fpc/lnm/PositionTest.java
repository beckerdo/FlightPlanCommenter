package info.danbecker.fpc.lnm;

import org.junit.jupiter.api.Test;

import info.danbecker.fpc.lnm.Position;

import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PositionTest {
	@BeforeEach
    public void setup() {
	}
	
	@Test
    public void testSort() {
		List<Position> list = Arrays.asList(
			new Position( -10, -10, -10),		
			new Position( -10, 0, 0),
			new Position( 0, -10, 0),
			new Position( 0, 0, -10),
			new Position( 0, 0, 0),
			new Position( 0, Float.NaN, Float.NaN), new Position( Float.NaN, 0, Float.NaN ), new Position( 0, 0, Float.NaN ),
			new Position( 0, 10, 0),
			new Position( 10, 0, 0),
			new Position( 10, 10, 10),
			Position.UNKNOWN );

		Collections.sort( list );
		// System.out.println( list );

		Position test = new Position(10, 10, 10);
		// assertTrue ( list.get( 0 ) == test, "first" ); // == will fail on different objects
		assertTrue( test.equals( list.get( 0 )), "first equals" );
		assertTrue( 0 == test.compareTo( list.get( 0 )), "first compareTo" );
		assertTrue( list.get( list.size() - 1) == Position.UNKNOWN, "last" );

		assertTrue( list.indexOf( new Position( 10, 0, 0 )) < list.indexOf( new Position( -10, 0, 0 )), "lat index" );
		assertTrue( list.indexOf( new Position( 0, 10, 0 )) < list.indexOf( new Position( 0, -10, 0 )), "lon index" );
		assertTrue( list.indexOf( new Position( 0, 0, 10 )) < list.indexOf( new Position( 0, 0, -10 )), "alt index" );
	}
	
	@Test
    public void testDistance() {
		// Using Norway 
		// ENGM;Gardermoen;671;11.08388901;60.20277786
		// ENGK;EN;Gullknapp;238;8.70777798;58.51833344
		// Around 124.54 nm, LNM says 133 (more indirect route)

		// Waypoint( "ENGM", WaypointType.AIRPORT, new Position( 60.20277786f, 11.08388901f, 671.0f)),		
		Position thisPos = new Position( 60.20277786f, 11.08388901f, 671.0f);
		// Waypoint( "ENGK", WaypointType.AIRPORT, new Position( 58.51833344f, 8.70777798f, 238.0f))
		Position otherPos = new Position( 58.51833344f, 8.70777798f, 238.0f);
				
		double distance = Position.distance(
			Double.valueOf(thisPos.lat()), Double.valueOf(otherPos.lat()),
			Double.valueOf(thisPos.lon()), Double.valueOf(otherPos.lon()),
			Double.valueOf(thisPos.alt()), Double.valueOf(otherPos.alt())
		);
		assertTrue( Math.abs( 124.54d - distance) < 0.01d, "position distance" );
		
		distance = thisPos.distanceFrom( otherPos );
		assertTrue( Math.abs( 124.54d - distance) < 0.01d, "position distance" );
	}
	
	

}