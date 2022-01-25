package info.danbecker.fpc.lnm;

import java.io.File;
import java.io.IOException;
import java.util.StringJoiner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import info.danbecker.fpc.DocUtils;


/**
 * Little Navmap Plan (LNMPPLN) Waypoint
 * As described in LNMPLN <Documentation>https://www.littlenavmap.org/lnmpln.html</Documentation>

 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class Waypoint implements Comparable<Waypoint> {
	public final static double METERS_PER_NAUTICALMILE = 0.00053995680346; // US nautical mile
	
	protected String name; // opt
	protected String ident;
	protected String region; // opt
	protected String airway; // opt
	protected String comment; // opt
	protected WaypointType waypointType;
	protected Position pos;
	

	protected Waypoint() {
	}
	
	public Waypoint( String ident, WaypointType waypointType, Position pos) {
		this.ident = ident; this.waypointType = waypointType; this.pos = pos;
	}
	public Waypoint( String ident, WaypointType waypointType, Position pos, String name) {
		this.ident = ident; this.waypointType = waypointType; this.pos = pos;
		this.name = name;
	}

	public static Waypoint fromNode( Node waypointNode ) {
		Waypoint wp = new Waypoint();
		
        // <Name>Gardermoen</Name>
        // <Ident>ENGM</Ident>
        // <Type>AIRPORT</Type>
        // <Pos Lon="11.098961" Lat="60.194527" Alt="671.00"/>
        for (Node child : DocUtils.NodeIterable.iterable( waypointNode.getChildNodes() )) {
            // System.out.println( "   name=" + child.getNodeName() + ", text=" + child.getTextContent());
        	switch( child.getNodeName() ) {
    			case "Name": wp.name = child.getTextContent(); break;
    			case "Ident": wp.ident = child.getTextContent(); break;
     			case "Type": wp.waypointType = WaypointType.valueOf( child.getTextContent()); break;
    			case "Pos": wp.pos = Position.fromNode( child ); break;
    			case "Region": wp.region = child.getTextContent(); break;
    			case "Airway": wp.airway = child.getTextContent(); break;
    			case "Comment": wp.comment = child.getTextContent(); break;
    			case "#text": break; //System.out.println( "Waypoint #text=" + child.getNodeValue()); break; // ignore
    			default: System.out.println( "Unknown waypoint data with name \"" + child.getNodeName() + "\"");
        	}
        }
        // System.out.println( "Waypoint (" + waypointNode.getChildNodes().getLength() + " nodes)=" + wp.toString());
		return wp;
	}
	
	public double distanceFrom( Waypoint other) {
		if ( null == this.pos )
			throw new IllegalArgumentException( "this waypoint position is null");
		if ( null == other )
			throw new IllegalArgumentException( "from waypoint is null");
		
		return this.pos.distanceFrom( other.pos );
	}

	@Override
	public int compareTo(Waypoint that) {
		//System.out.println( "compareTo");
		if (null == that) return 1;
		int test = 0;
		// Put nulls behind real numbers
		if (null == ident && null == that.ident) return 0;
		if (null == ident ) return -1; if (null == that.ident ) return 1;
		if ( 0 != ( test = ident.compareTo(that.ident))) return test;  
		if (null == waypointType && null == that.waypointType) return 0;
		if (null == waypointType ) return -1; if (null == that.waypointType ) return 1;
		if ( 0 != ( test = waypointType.compareTo( that.waypointType))) return test;  
		if (null == pos && null == that.pos) return 0;
		if (null == pos ) return -1; if (null == that.pos ) return 1;
		if ( 0 != ( test = pos.compareTo( that.pos))) return test;  
		return test;
	}

    @Override
    public int hashCode() {
    	// Better job someday
        return ident.hashCode() + pos.hashCode() * 17;
    }

    @Override
    public boolean equals(final Object obj) {
        // Compare with self   
        if (obj == this) return true; 
  
        // Compare with class type
        if (!(obj instanceof Waypoint)) return false; 
        // Cast to same type  
        Waypoint that = (Waypoint) obj; 
          
        // Compare data by compare method
        return 0 == this.compareTo( that );
    }

	@Override
	public String toString() {
		String toString = "Waypoint[ident=" + ident + ",type=" + waypointType + ",pos=" + pos + "]";
		return toString;
	}
	
	public String toShortString() {
		String nameStr = "";
		if ( null != name )
			nameStr = name + ",";
		String toString = "Waypoint[" + nameStr + "ident=" + ident + ",type=" + waypointType + "]";
		return toString;
	}

}