package info.danbecker.fpc.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static info.danbecker.fpc.util.UnMarkup.unMarkup;

public class UnMarkupTest {	
	public static String LS = System.getProperty("line.separator");

	@BeforeEach
    public void setup() {
	}
	
	@Test
    public void testUnmarkupe() {
		// System.out.println( "Line=" + unMarkup( "fred<br>farkle" ));		
		assertEquals( "fred" +  LS + "farkle", unMarkup( "fred<br>farkle" ), "break replace");
		assertEquals( "fred" +  LS + "farkle", unMarkup( "fred<br/>farkle" ), "break closed replace");
		assertEquals( "fred" +  LS + "farkle", unMarkup( "fred <br/> farkle" ), "break whitespace");
	}
	
}