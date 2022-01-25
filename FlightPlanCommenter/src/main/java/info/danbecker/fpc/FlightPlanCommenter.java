package info.danbecker.fpc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import info.danbecker.fpc.lnm.LNMPln;
import info.danbecker.fpc.msfs.L10n;

import java.util.logging.Logger;
import static java.lang.String.format;

/**
 * PlanCommenter
 * <p>
 * A tool for taking comments from one flight plan and adding to another.
 * <p>
 * <pre>
 * Example command line "java PlanCommenter -ip flight.lnmpln -ic en_US.locPak -op commented.lnmpln"
 *    -ic comment input file (optional)
 *    -ip flight plan input file
 *    -op flight plan output file
 *    -sa split output plan into legs (with an optional prefix) at airport waypoints
 * </pre>
 * <p>
 * Currently handles:
 * <ul>
 * <li>Little Navmap input and output files (which is nicely formatted XML with schema)
 * <li>Microsoft Flight Simulator bush trip input comment files (which is strangely
 * keyed JSON files)
 * </ul>
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class FlightPlanCommenter {
	public static String OUTPUT_SIGNATURE = " fpc";
	protected static String inputPlanFile;
	protected static String outputPlanFile;
	public static final String LNMPLN_INPUT = "VFR Duene (EDXH) to Jolling (EDPC).lnmpln";
	// alternate "VFR Bird s Nest (KEDC) to Bird s Nest (KEDC).lnmpln"; // Texas
	// alternate "VFR Schwechat (LOWW) to Dornbirn (LOIH).lnmpln"; // Austria
	// alternate "VFR Duene (EDXH) to Jolling (EDPC).lnmpln"; // Germany
	// alternate "VFR Gardermoen (ENGM) to Sorkjosen (ENSR).lnmpln"// Norway

	protected static String splitLeg;
	protected static boolean split;
	
	protected static String inputCommentFile;
	protected static String inputCommentInstall;
	protected static String inputCommentTripName;
	protected static String inputCommentFilename;
	
	public static final String MSFS_INSTALL = "E:/documents/hobbies/flight/flightplans/FlightPlanCommenter";
	// alternate "D:/games/MSFlightSim/Packages/Official/OneStore"
	public static final String MSFS_BUSHTRIP = "microsoft-bushtrip-germany";
	public static final String MSFS_FILE = "en-US.germany.locPak"; // alternate en-US.locPak
	
	/**
	 * Gather program options, read input flight plans, read and append comments, write outputs. 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Flight Plan Commenter (FPC) by Dan Becker");
		Logger.getGlobal().info( "Flight Plan Commenter (FPC) by Dan Becker" );

		parseGatherOptions(args);
		
		// Parse the LNMPLN file.
		LNMPln lnmPln = new LNMPln(inputPlanFile);
		lnmPln.readInput();

		lnmPln.printHeader();
		lnmPln.printWaypoints();

		// Read MSFS L10N if one was given
		if ( null != inputCommentFile ) {
			L10n msfsL10n = new L10n( inputCommentFile );
			msfsL10n.readAsJson();
			// System.out.println( "Requested language=" + MSFSL10N +  ", file language=" + msfsL10n.getLanguage() );		
			// System.out.println( "Trip=\"" + msfsL10n.getTitle() + "\" from " + msfsL10n.getPlanBegin() + L10n.LEGDELIM + msfsL10n.getPlanEnd());
			System.out.println( format( "Input comments %s contains %d legs", msfsL10n.getFullPath(), msfsL10n.getLegsLength() ));
		
			lnmPln.updateHeader();
			lnmPln.addComments( msfsL10n );
		
			lnmPln.writeOutput( outputPlanFile );
		}
		
		if ( split ) {
			lnmPln.split( outputPlanFile, splitLeg );
		}
	}
	
	/** Gather command line options for this application. Place info in this class instance variables. */
	public static void parseGatherOptions(String[] args) throws ParseException, URISyntaxException, IOException {
		// Parse the command line arguments
		Options options = new Options();
		// Use dash with shortcut (-h) or -- with name (--help).
        options.addOption("h", "help", false, "print the command line options");
        options.addOption("ip", "inp", true, "input file for flight plan");
        options.addOption("op", "oup", true, "output file for flight plan");
        options.addOption("ic", "inc", true, "input file for flight comments");
        options.addOption("sa", "sa", false, "split flight plan into legs via airports");

		CommandLineParser cliParser = new DefaultParser();
		CommandLine line = cliParser.parse(options, args);

		// Gather command line arguments for execution
		if (line.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar flightplancommenter.jar <options> info.danbecker.fpc.FlightPlanCommenter",
					options);
			System.exit(0);
		}

        // Gather command line arguments for execution
		System.out.println( "Command parse options:");
        if (line.hasOption("ip")) {
            String option = line.getOptionValue("ip");
            inputPlanFile = option;          
        } else {
        	inputPlanFile = LNMPLN_INPUT;
        }
		// From https://stackoverflow.com/questions/6164448/convert-url-to-normal-windows-filename-java
		// The current recommendation (with JDK 1.7+) is to convert URL → URI → Path/File/String. 
		// So to convert a URL to File, you would say Paths.get(url.toURI()).toFile()
        // Note that URI strings have leading slash, and file strings are normalized to local file system (/ versus \)
        System.out.println( format( "inp option given=%s", inputPlanFile ));
		inputPlanFile = Paths.get( ClassLoader.getSystemClassLoader().getResource(inputPlanFile).toURI() ).toFile().toString();
        System.out.println( format( "inp option normalized=%s", inputPlanFile ));

        if (line.hasOption("op")) {
            String option = line.getOptionValue("op");
            outputPlanFile = option;          
        } else {
    		outputPlanFile = inputPlanFile.replaceAll( LNMPln.LNMPLN_EXT, "" );
    		outputPlanFile += OUTPUT_SIGNATURE + LNMPln.LNMPLN_EXT;
        }
        System.out.println( format( "op option %s", outputPlanFile));

        if (line.hasOption("ic")) {
            String option = line.getOptionValue("ic");
            inputCommentFile = option;
        }
        System.out.println( "ic option given=" + inputCommentFile );
        if ( null != inputCommentFile ) {
        	inputCommentFile = Paths.get( ClassLoader.getSystemClassLoader().getResource(inputCommentFile).toURI() ).toFile().toString();
        	System.out.println( format( "ic option normalized %s", inputCommentFile ));
        }
        
        // Gather command line arguments for execution
        if (line.hasOption("sa")) {
            String option = line.getOptionValue("sa");
            split = true;
            if( null != option && option.length() > 0) {
            	splitLeg = option;
            } else {
            	splitLeg = LNMPln.LNMPLN_LEG;
            }
            System.out.println( "sa split option legs=" + split + ", leg=\"" + splitLeg + "\"");
        } else {
        	split = false;
            System.out.println( "sa split option legs=" + split );
        }
	}
}
