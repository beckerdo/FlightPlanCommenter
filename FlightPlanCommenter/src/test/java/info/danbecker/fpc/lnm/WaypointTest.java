package info.danbecker.fpc.lnm;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class WaypointTest {
	@BeforeEach
    public void setup() {
	}
	
	@Test
    public void testSort() {
		List<Waypoint> list = Arrays.asList(
			new Waypoint( "xyz", WaypointType.WAYPOINT, new Position( 10, 10, 500)),		
			new Waypoint( "abc", WaypointType.WAYPOINT, new Position( -10, 10, 500)),		
			new Waypoint( "KLMN", WaypointType.AIRPORT, new Position( 100, 100, 0)),		
			new Waypoint( "KMNO", WaypointType.AIRPORT, new Position( 100, -100, 0)),		
			new Waypoint( "456", WaypointType.USER, new Position( 0, 0, 100)),		
			new Waypoint( "123", WaypointType.USER, new Position( 0, 0, -100)));		

		Collections.sort( list );
		// System.out.println( list );

		Waypoint test = new Waypoint( "123", WaypointType.USER, new Position( 0, 0, -100));
		// assertTrue ( list.get( 0 ) == test, "first" ); // == will fail on different objects
		assertTrue( list.contains(test), "list item" );
		assertTrue( test.equals( list.get( 0 )), "first equals" );
		assertTrue( 0 == test.compareTo( list.get( 0 )), "first compareTo" );
	}
	
	@Test
    public void testDistance() {
		// Using Norway 
		// ENGM;Gardermoen;671;11.08388901;60.20277786
		// ENGK;EN;Gullknapp;238;8.70777798;58.51833344
		// Around 124.54 nm, LNM says 133 (more indirect route)
		List<Waypoint> list = Arrays.asList(
			new Waypoint( "ENGM", WaypointType.AIRPORT, new Position( 60.20277786f, 11.08388901f, 671.0f)),		
			new Waypoint( "ENGK", WaypointType.AIRPORT, new Position( 58.51833344f, 8.70777798f, 238.0f))		
		);		

		double distance = list.get(0).distanceFrom( list.get( 1 ));
		assertTrue( Math.abs( 124.54d - distance) < 0.01d, "waypoint distance" );
	}
	
	@Test
    public void testFromNode() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Node waypointNode = doc.createElement("Waypoint");
        Node name = doc.createElement("Name"); name.setTextContent("Gardermoen");
        Node ident = doc.createElement("Ident"); ident.setTextContent("ENGM");
        Node airport = doc.createElement("Type"); airport.setTextContent("AIRPORT");
        Node comment = doc.createElement("Comment"); comment.setTextContent("Comment");
        Node pos = doc.createElement("Pos"); 
        ((Element)pos).setAttribute("Lat","60.194527");
        ((Element)pos).setAttribute("Lon","11.098961");
        ((Element)pos).setAttribute("Alt","671.0");
        waypointNode.appendChild(name);
        waypointNode.appendChild(ident);
        waypointNode.appendChild(airport);
        // waypointNode.appendChild(comment);
        waypointNode.appendChild(pos);
        
        Waypoint created = Waypoint.fromNode( waypointNode );
        // System.out.println( "Waypoint=" + created.toString());
        
        Waypoint test = new Waypoint( "ENGM", WaypointType.AIRPORT, new Position( 60.194527f, 11.098961f, 671.0f), "Gardermoen");
        assertEquals( test, created, "Waypoint from Node");

        assertEquals( "Waypoint[Gardermoen,ident=ENGM,type=AIRPORT]", test.toShortString(), "toShortString");
        assertEquals( "Waypoint[ident=ENGM,type=AIRPORT,pos=Position[lat=60.194527, lon=11.098961, alt=671.0]]", test.toString(), "toString");
	}
}