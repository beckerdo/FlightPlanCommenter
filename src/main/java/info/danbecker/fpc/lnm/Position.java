package info.danbecker.fpc.lnm;

import java.util.Arrays;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Little Navmap Plan (LNMPPLN) Position
 * As described in LNMPLN <Documentation>https://www.littlenavmap.org/lnmpln.html</Documentation>

 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public record Position(float lat, float lon, float alt) implements Comparable<Position> {
	public static Position UNKNOWN = new Position( Float.NaN, Float.NaN, Float.NaN);
	
	public static Position fromNode( Node positionNode ) {
		NamedNodeMap map = positionNode.getAttributes();
		// <Pos Lon="10.806945" Lat="59.917778" Alt="4000.00"/>
		float lat = Float.valueOf( map.getNamedItem( "Lat" ).getNodeValue() );
		float lon = Float.valueOf( map.getNamedItem( "Lon" ).getNodeValue() );
		float alt = Float.valueOf( map.getNamedItem( "Alt" ).getNodeValue() );
		return new Position( lat, lon, alt );
	}
	
	public double distanceFrom( Position other) {
		if ( null == other)
			throw new IllegalArgumentException( "other position is null");
		
		return distance(
			Double.valueOf(this.lat()), Double.valueOf(other.lat()),
			Double.valueOf(this.lon()), Double.valueOf(other.lon()),
			Double.valueOf(this.alt()), Double.valueOf(other.alt())
		);
	}

	/**
	 * Calculate distance between two points in latitude and longitude taking into
	 * account height difference. If you are not interested in height difference
	 * pass 0.0. Uses Haversine method as its base.
	 * 
	 * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters el2
	 * End altitude in meters
	 * @returns Distance in nautical miles
	 */
	public static double distance(double lat1, double lat2, double lon1, double lon2, double el1, double el2) {

		final int R = 6371; // Radius of the earth

		double latDistance = Math.toRadians(lat2 - lat1);
		double lonDistance = Math.toRadians(lon2 - lon1);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // convert to meters

		double height = el1 - el2;

		distance = Math.pow(distance, 2) + Math.pow(height, 2);

		double meters = Math.sqrt(distance);
		return meters * Waypoint.METERS_PER_NAUTICALMILE;
	}
	
	@Override
	public int compareTo(Position that) {
		//System.out.println( "compareTo");
		if (null == that) return 1;
		int test = 0;
		// Put nulls and NaNs behind real numbers
		if (Float.isNaN( this.lat )) return 1; if (Float.isNaN( that.lat )) return -1;
		if ( 0 != ( test = Float.compare(that.lat, this.lat))) return test;  
		if (Float.isNaN( this.lon )) return 1; if (Float.isNaN( that.lon )) return -1;
		if ( 0 != ( test = Float.compare(that.lon, this.lon))) return test; 
		if (Float.isNaN( this.alt )) return 1; if (Float.isNaN( that.alt )) return -1;
		if ( 0 != ( test = Float.compare(that.alt, this.alt))) return test; 
		return test;
	}
	
	@Override
	public boolean equals(Object obj) {
        // Compare with self   
        if (obj == this) return true; 
  
        // Compare with class type
        if (!(obj instanceof Position)) return false; 

        // Cast to same type  
        Position that = (Position) obj; 
		return 0 == this.compareTo( that );
	}
}
