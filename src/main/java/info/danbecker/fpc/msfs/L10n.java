package info.danbecker.fpc.msfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * MSFS specific language strings for bush trips, missions, commenting
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class L10n {
	public static final String MSFSLOC_EXT = "locPak"; // search for position of "." + 1;
	public static final String LEGJOINER = "to";
	public static final String LEGDELIM = " to ";
	public static final String AUTHORSTRING = "Created "; // some have "By" or "by"

	String fullPath; // full file system origin, e.g. "D:\games\MSFlightSim\Packages\Official\OneStore\microsoft-bushtrip-germany".locPak""
	String pathInstall; // file system origin, e.g. "D:\games\MSFlightSim\Packages\Official\OneStore" two dirs up
	String tripName; // sub path from install, e.g. "microsoft-bushtrip-germany" containing dir
	String locFile; // localization string, e.g. "en-US" or "en-US.austria"
	String locLang; // localization string, e.g. "en-US"
	
	String keyPrefix; // common String key prefix, e.g. "asobo-bushtrip-germany.Mission."
	
	String title; // title e.g. "Germany Journey"
	String location; // country or territory e.g. "Germany"
	String planBegin; // overall beginning "Helgoland"
	String planEnd; // overall plan ending "Jolling"
	String planComment; // overall plan comment "This flight begins in the far north of Germany, ..."
	
	List<String> locations; 
	List<Leg> legs;
	
	public class Leg {
		int titleKey; // 79 for "asobo-bushtrip-germany.Mission.79": "Duene to St Michaelisdonn"
		String begin; // "Duene"
		String end; // "St Michaelisdonn"
		
		List<String> comments; // ""After launching from Helgoland Airport, make a sharp turn..."

		public List<String> getComments() { return comments; } // should return immutable
		
		public String toString() {
			return "[Leg begin=" + begin +",end=" + end + ", comments=" + (null == comments ? "null" : comments.size()) + "]";
		}
	}
	
	protected JSONObject jsonBase;
	protected JSONObject jsonLP;
	protected JSONObject jsonStrings;
	
	protected L10n() {
	}
	
	/**
	 * 
	 * @param path - should be full path 
	 */
	public L10n (String path) {
		if (null == path || 0 == path.length() ) 
			throw new IllegalArgumentException( "l10n path must not be null or empty");
		if (!Files.exists( Path.of( path ) )) 
			throw new IllegalArgumentException( "l110n comments path must exist, path=" + path );
		if (!Files.isReadable( Path.of( path ) )) 
			throw new IllegalArgumentException( "l110n comments path must be readable, path=" + path);

		String[] components = fullPathToComponents( path );		
		this.fullPath = path;
		this.pathInstall = components[0];
		this.tripName = components[1];
		this.locLang = components[2];
		
		System.out.println( "L10n inputs:" );
		System.out.println( "input path=" + path );
	}
	
	public L10n (String pathInstall, String tripName, String locFile ) {
		if (null == pathInstall || 0 == pathInstall.length() ) 
			throw new IllegalArgumentException( "l10n comments path must not be null or empty");
		String fullPath = componentsToFullPath(pathInstall, tripName, locFile);
		if (!Files.exists( Path.of( fullPath )) ) 
			throw new IllegalArgumentException( "l110n comments file must exist, path=" + fullPath);
		if (!Files.isReadable( Path.of( fullPath )) ) 
			throw new IllegalArgumentException( "l110n comments file must be readable, path=" + fullPath);
		
		this.fullPath = fullPath;
		this.pathInstall = pathInstall;
		this.tripName = tripName;
		this.locFile = locFile;
		System.out.println( "L10n inputs:" );
		System.out.println( "install=" + pathInstall + ",tripName=" + tripName +  ",locFile=" + locFile);
	
	}
	
	/*
	 * Breaks a given String path such as 
	 * "/E:/computer/eclipse-workspace/FlightPlanCommenter/src/test/resources/en-US.norway.locPak"
	 * "/E:/documents/hobbies/flight/flightplans/FlightPlanCommenter/microsoft-bushtrip-germany/en-US.locPak;
	 * into 
	 * item 0, path: /E:/documents/hobbies/flight/flightplans/FlightPlanCommenter/
	 * item 1, tripName: microsoft-bushtrip-germany
	 * item 2, fileName: en-US.germany.locPak 
	 */
	public static String[] fullPathToComponents(String path) {
		char delim = File.separatorChar;
		// URL always has forward slash
		if (path.startsWith( "/" ))	path = path.substring(1);
		
		// Break this down into pathInstall, tripName, locFileName
		String locFile = path.substring(path.lastIndexOf(delim) + 1);
		String noFile = path.substring(0, path.lastIndexOf(delim) );
		String tripName = noFile.substring(noFile.lastIndexOf(delim) + 1);
		String noTrip = path.substring(0, path.lastIndexOf(delim) );
		return new String [] { noTrip, tripName, locFile };
	}

	public static String componentsToFullPath(String pathInstall, String tripName, String fileName) {
		// Assemble full path from directory, bush trip name, fileName
		return pathInstall + File.separator + tripName + File.separator + fileName;
	}
	
	/** 
	 * Read localized Strings into the object.
	 */
	public JSONObject readAsJson() throws IOException {
		InputStream is = Files.newInputStream(Path.of( getFullPath() ), StandardOpenOption.READ);
        JSONTokener tokener = new JSONTokener(is);
        jsonBase = new JSONObject(tokener);
        jsonLP = jsonBase.getJSONObject("LocalisationPackage");
        jsonStrings = jsonLP.getJSONObject("Strings");
        
        // Find key prefix in String container
        String first = jsonStrings.keySet().iterator().next();
        keyPrefix = first.substring( 0, first.lastIndexOf( "." ) + 1);
		        
		int keyPosition = 1;
		keyPosition = readHeader( jsonStrings, keyPrefix, keyPosition );
		
		// Parse locations
		keyPosition = readLocations( jsonStrings, keyPrefix, keyPosition );

		// Parse legs
		keyPosition = readLegs( jsonStrings, keyPrefix, keyPosition );

		// Reread the prior string which should be a "from to to"
        return jsonLP;
	}

	// Read header fields from Strings map
	// Has the side effect of setting title, location planBeing, planEnd, planComment
	// Returns the next unread key position in the Strings map
	public int readHeader(JSONObject jsonStrings, String keyprefix, int keyPosition) {
		title = jsonStrings.getString(keyPrefix + keyPosition++);
		String legString = jsonStrings.getString(keyPrefix + keyPosition++);
		// Some files have no location. Skip over if this looks like legs
		if (legString.contains(LEGDELIM)) {
			location = "";
		} else {
			location = legString;
			legString = jsonStrings.getString(keyPrefix + keyPosition++);
		}
		String[] beginEnd = getBeginEnd(legString);
		planBegin = beginEnd[0];
		planEnd = beginEnd[1];
		// Some files have two legs
		planComment = jsonStrings.getString(keyPrefix + keyPosition++);
		if (null != planEnd && planComment.contains( planEnd )) {
			System.out.println( "planComment=\"" + planComment + "\". Appears to be leg. Will discard and read next key for comment");			
			planComment = jsonStrings.getString(keyPrefix + keyPosition++);		
		}
		return keyPosition;
	}

	// Read locations from Strings map
	// Has the side effect of creating locations list.
	// Returns the next unread key position in the Strings map
	public int readLocations(JSONObject jsonStrings, String keyPrefix, int keyPosition) {
		locations = new LinkedList<String>();
		String location = jsonStrings.getString(keyPrefix + keyPosition++);
		while (null != location && !location.contains(LEGDELIM)) {
			locations.add(location);
			location = jsonStrings.getString(keyPrefix + keyPosition++);
		}
		// The while has read beyond locations into a "from to to" string. 
		// Return previous position
		return keyPosition - 1;
	}

	// Read "from to to" legs and comments from Strings map
	// Has the side effect of creating legs list.
	// Returns the next unread key position in the Strings map
	public int readLegs(JSONObject jsonStrings, String keyPrefix, int keyPosition) {
		legs = new LinkedList<Leg>();
		int commentCount = 0;
		String legString = jsonStrings.optString(keyPrefix + keyPosition++);
		while (null != legString) {
			Leg leg = new Leg();
			leg.titleKey = keyPosition - 1;
			leg.comments = new LinkedList<String>();
			String[] beginEnd = getBeginEnd(legString);
			leg.begin = beginEnd[0];
			leg.end = beginEnd[1];
			legString = null;

			// Comment might be null (end of file), next leg, or author string.
			String comment = jsonStrings.optString(keyPrefix + keyPosition++);
			while (null != comment && comment.length() > 0 ) {
				if ( comment.contains(leg.end + LEGDELIM )) {
					legString = comment;
					comment = null;
				} else if ( comment.contains( AUTHORSTRING )) {
					comment = null;
				} else {
					// System.out.println( "comment=" + comment );
					leg.comments.add(comment);
					comment = jsonStrings.optString(keyPrefix + keyPosition++);
				}
			}			
			// System.out.println("Leg " + legs.size() + "=" + leg);
			legs.add(leg);
			commentCount += leg.comments.size();
		}
		// System.out.println( "Found leg count=" + legs.size() + ", commentCount=" + commentCount);
		return keyPosition;
	}

	public static String[] getBeginEnd( String stops ) {
		if ( !stops.contains( LEGDELIM ))
			throw new IllegalArgumentException( "String \"" + stops + "\" does not include \"" + LEGDELIM + "\"");
		String [] beginEnd = stops.split(LEGDELIM);
		return beginEnd;
	}
	
	public String getFullPath() {
		return fullPath;
	}
	
	public String getLanguage() {
		return jsonLP.getString( "Language" );
	}
	
	public String getTitle() { return title; }
	public String getLocation() { return location; }
	public String getPlanBegin() { return planBegin; }
	public String getPlanEnd() { return planEnd; }
	public String getPlanComment() { return planComment; }
	
	
	// Dangerous. Should make an immutable copy
	public List<String> getLocations() {
		return locations;
	} 
	public int getLocationsLength() {
		return locations.size();
	}
	public String getLocation( int index ) {
		return locations.get( index );
	} 
	public int getLocationIndex( String location ) {
		return locations.indexOf( location );
	} 

	// Dangerous. Should make an immutable copy
	public List<Leg> getLegs() {
		return legs;
	} 
		public int getLegsLength() {
		return legs.size();
	}
	public Leg getLeg( int index ) {
		return legs.get( index );
	}

	/*
	 * Return leg beginning with given name or null
	 */
	public Leg getLegBeginningWith( String airportName ) {
		if ( null == airportName) return null;
		for (Leg leg : legs ) {
			if ( airportName.equals( leg.begin))
				return leg;
		}
		return null;
	}
}