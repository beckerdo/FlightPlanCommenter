package info.danbecker.fpc.lnm;

/**
 * Little Navmap Plan (LNMPPLN) WaypointType
 * As described in LNMPLN <Documentation>https://www.littlenavmap.org/lnmpln.html</Documentation>

 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public enum WaypointType implements Comparable<WaypointType>{
	AIRPORT,
	UNKNOWN,
	WAYPOINT,
	VOR,
	NDB,
	USER;
}