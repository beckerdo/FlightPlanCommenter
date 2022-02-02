package info.danbecker.fpc.util;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Remove HTML markup embedded in a String
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 *
 */
public class UnMarkup {
    public static final String[][] HTML_MARKUP = {
        {"<br>",  System.getProperty("line.separator") }, // line break
        {"<br/>", System.getProperty("line.separator") }, // line break
    };
    private static final HashMap<String,String> lookupMap;
    static {
        lookupMap = new HashMap<String, String>();
        for (final String [] seq : HTML_MARKUP) 
            lookupMap.put(seq[0], seq[1]);
    }


	public static final String unMarkup(String input) {
		if ( null == input) return null;
		for( Map.Entry<String,String> entry : lookupMap.entrySet()) {
			input = input.replaceAll( "\\s*" + entry.getKey() + "\\s*", entry.getValue() );
        }
        return input;
    }

}