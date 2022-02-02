package info.danbecker.fpc.lnm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import info.danbecker.fpc.msfs.L10n;
import info.danbecker.fpc.util.DocUtils;

/**
 * Little Navmap Plan (LNMPPLN) Reader
 * 
 * A Little Navmap plan is a well formed XML document with elements
 * for flight planning: airports, waypoints, positions, etc.
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class LNMPln {
	public static final String LNMPLN_EXT = ".lnmpln";
	public static final String LNMPLN_LEG = "leg";
	
	protected LNMPln() {
	}
	
	protected String inputPath;
	protected Document lnmPlnDoc;
	
	public LNMPln (String path) {
		if (null == path || 0 == path.length() ) 
			throw new IllegalArgumentException( "LNMPln input path must not be null or empty");
		if (!Files.exists( Path.of( path )) ) 
			throw new IllegalArgumentException( "LNMPln input path must exist, path=" + path);
		if (!Files.isReadable( Path.of( path )) ) 
			throw new IllegalArgumentException( "LNMPln input path must be readable, path=" + path);		
		inputPath = path;
		System.out.println( "LNMPln inputs:" );
		System.out.println( "input path=" + inputPath );
	}

	public void readInput() throws ParserConfigurationException, SAXException, IOException {
		lnmPlnDoc = DocUtils.readInput(inputPath);
	}

	public void printHeader() {
		System.out.println("Doc root: " + lnmPlnDoc.getDocumentElement().getNodeName());
		DocUtils.printElementsText( lnmPlnDoc, "Header");
	}

	public void printWaypoints() {
		NodeList elements = lnmPlnDoc.getElementsByTagName("Waypoints");
		if (null != elements) {
			for (int temp = 0; temp < elements.getLength(); temp++) {
				Node waypoints = elements.item(temp);
				if (null != waypoints && waypoints.getNodeType() == Node.ELEMENT_NODE) {
					NodeList children = waypoints.getChildNodes();
					System.out.println(waypoints.getNodeName() + " (length=" + children.getLength() + "):");
					for (int childi = 0; childi < children.getLength(); childi++) {
						Node waypoint = children.item(childi);
						if (null != waypoint && waypoint.getNodeType() == Node.ELEMENT_NODE) {
							// System.out.println( waypoint.getNodeName() + ": " +
							// nodeListToString(waypoint.getChildNodes()));
						}
					}
				}
			}
		}
	}

	public int getWaypointsCount() {
		return DocUtils.getElementCount( lnmPlnDoc, "Waypoints"); 
	}

	public void updateHeader() throws XPathException {
		updateHeader( lnmPlnDoc);
	}

	public void updateHeader(Document doc) throws XPathException {
    	// System.out.println( "Xpath=" + xPath + ", text=" + node.getTextContent());
    	String now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME); 
    	// Remove milliseconds
    	if ( now.contains(".")) now = now.substring( 0, now.indexOf("."));
    	DocUtils.updateXpathNode( doc, "//Flightplan/Header/CreationDate", now, false ); // replace
    	DocUtils.updateXpathNode( doc, "//Flightplan/Header/ProgramName", "FlightPlanCommenter", true ); // update 
	}	

	/**
	 * In Little Navmap plans, a list of waypoints can have comments.
	 * These comments are retrieved from the MSFS JSON document. 
	 * @param msfsL10n
	 * @throws XPathException
	 */
	public void addComments( L10n msfsL10n ) throws XPathException {
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
        int commentsAdded = 0;
        
		String xPath = "//Flightplan/Waypoints/Waypoint";
        XPathExpression expr = xp.compile( xPath );
        NodeList waypoints = (NodeList) expr.evaluate(lnmPlnDoc, XPathConstants.NODESET);       
        
		xPath = "//Flightplan/Waypoints/Waypoint[Type='AIRPORT']";
        expr = xp.compile( xPath );
        NodeList airports = (NodeList) expr.evaluate(lnmPlnDoc, XPathConstants.NODESET);
        for (Node airport : DocUtils.NodeIterable.iterable(airports)) {
        	String airportName = DocUtils.getChildNodeText( airport, "Name" );
        	// Find leg beginning with this airport
        	int legi = DocUtils.getIndex( waypoints, "Name", airportName);         	
        	// System.out.println( "Xpath=" + xPath + ", text=" + airportName + ", waypoint index=" + legi );
        	if ( -1 != legi ) {
        		L10n.Leg leg = msfsL10n.getLegBeginningWith(airportName);
        		if ( null != leg && null != leg.getComments()) {
                	// System.out.println( "Xpath=" + xPath + ", text=" + airportName + ", waypoint index=" + legi + ", " + leg.getComments().size() + " comments");
        			for ( String comment: leg.getComments()) {
        				Node waypoint = waypoints.item( legi++ );
        				Element commentNode = lnmPlnDoc.createElement("Comment");
        				commentNode.setTextContent( comment );
        				// System.out.println( commentNode.getTextContent() );
        				waypoint.appendChild(commentNode);
        			}
        			commentsAdded += leg.getComments().size();
        		}
        	}
    	}
        System.out.println( "Added "+ commentsAdded + " comments to plan");
	}
	
	public void writeOutput( String path ) throws IOException {
		DocUtils.writeOutput( lnmPlnDoc, path );
	}
	
	/** 
	 * Split the long flight plan into legs from airport to airport.
	 * A short flight plan with airports A, B, and C would be split
	 * into two files, the first from A to B and the second from B to C.
	 * 
	 * @param inputPlanFile
	 * @param splitLeg
	 * @return
	 * @throws XPathException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws IOException
	 */
	public int split( String inputPlanFile, String splitLeg ) throws XPathException, 
		ParserConfigurationException, TransformerException, IOException {
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
		int fileCount = 0;
		String xPath = "//Flightplan/Waypoints/Waypoint";
        XPathExpression expr = xp.compile( xPath );
        NodeList waypoints = (NodeList) expr.evaluate(lnmPlnDoc, XPathConstants.NODESET);       
		
        // Iterate through waypoints.
        System.out.println( "LNMPlan.split:");
        Waypoint beginAirport = null;
		Document updatedDoc = null;
		Element updatedWaypoints = null;
		int legi = 0;
		float legDistance = 0.0f;
		Waypoint previousWaypoint = null; // for distance calcs.

		// Airport and legs count
		xPath = "//Flightplan/Waypoints/Waypoint[Type='AIRPORT']";
        expr = xp.compile( xPath );
        NodeList airports = (NodeList) expr.evaluate(lnmPlnDoc, XPathConstants.NODESET);
		int legCount = airports.getLength() - 1;
		
        for (Node waypointNode : DocUtils.NodeIterable.iterable(waypoints)) {        	
			Waypoint waypoint = Waypoint.fromNode( waypointNode );
			String commentText = DocUtils.getChildNodeText( waypointNode, "Comment" );
			// System.out.println( "Waypoint=" + waypoint.toShortString() + ", previous=" + previousWaypoint );
			if ( null != waypoint.waypointType && "AIRPORT".equals(waypoint.waypointType.name() )) {
				if ( null == beginAirport ) {
					// First leg starts
					beginAirport = waypoint;
					legi = 0;
					legDistance = 0.0f;
				} else {
					// Current leg ends
					// System.out.println( "Leg " + legi + ", start=" + beginAirport.toShortString() + ", end=" + waypoint.toShortString() );
					beginAirport = waypoint;
					// End airport should have no comment for this leg;
					// Calculate distance of this leg, use that as a comment.
					legDistance += waypoint.distanceFrom(previousWaypoint);
					String distanceComment = legComment( legi++, legCount, legDistance );
					DocUtils.cloneAppendNode( updatedDoc, updatedWaypoints, waypointNode, distanceComment  );

					// Write leg to file	         
			        // Puts all text nodes in the full depth of the sub-tree underneath this node
			        updatedDoc.normalize();			         
			        // DocUtils.prettyPrint(updatedDoc);
		    		String legPlanFile = inputPlanFile.replaceAll( LNMPln.LNMPLN_EXT, "" );
		    		legPlanFile += " " + LNMPLN_LEG + String.format( "%02d", fileCount ) + LNMPLN_EXT;
					updateHeader( updatedDoc );
					DocUtils.writeOutput( updatedDoc, legPlanFile );
					System.out.println( "output path=" + legPlanFile );

					fileCount++;
				}
				// Begin new doc with this airport waypoint
				updatedDoc = DocUtils.cloneDoc( lnmPlnDoc, "Waypoints" );
				updatedWaypoints = (Element) updatedDoc.getElementsByTagName("Waypoints").item(0);
				legDistance = 0.0f;
				DocUtils.cloneAppendNode( updatedDoc, updatedWaypoints, waypointNode, commentText );
			} else {
				// Intermediate waypoint, update current leg waypoints
				if ( null != updatedDoc) {
					if (null != previousWaypoint)
						legDistance += waypoint.distanceFrom(previousWaypoint);
					DocUtils.cloneAppendNode( updatedDoc, updatedWaypoints, waypointNode );
				} else
					System.out.println( "Waypoint appears to come before airport, waypoint=" + waypoint.toShortString());
			}
			previousWaypoint = waypoint; // For distance calcs.
        } 
        return fileCount;
	}
	
	public String legComment( int thisLeg, int numLegs, float distance ) {
		return String.format( "Completed leg %02d/%02d, distance=%.1fnm", thisLeg, numLegs, distance );	
	}
}