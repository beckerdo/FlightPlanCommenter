package info.danbecker.fpc;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import info.danbecker.fpc.util.DocUtils;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathException;


public class DocUtilsTest {
	Document doc;
	NodeList albums;
	
	@BeforeEach
    public void setup() throws ParserConfigurationException, SAXException, IOException{
		doc = DocUtils.readInput("src/test/resources/test.xml");
		doc.getDocumentElement().normalize();		
		albums = doc.getDocumentElement().getElementsByTagName("Album");		
	}
	
	@Test
    public void testInput() {
		assertNotNull( doc, "document created");
		assertEquals( "Albums", doc.getDocumentElement().getNodeName(), "root element");
		assertEquals( 4,  albums.getLength(), "child node count" );
	}
	
	@Test
    public void testGetElementCount() {
		assertEquals( 4, DocUtils.getElementCount( doc, "Album" ), "getElementCount");
	}
	
	@Test
    public void testNodeListToString() {
		DocUtils.printElementsText( doc, "Album");
		NodeList albums = doc.getElementsByTagName( "Album" );
		String result = DocUtils.nodeListToString( albums.item( 0 ).getChildNodes() );
		assertTrue( result.startsWith("title=Houses"), "nodeListToString" );
	}

	@Test
    public void testNodeIterable() {
		int count = 0;
        for (@SuppressWarnings("unused") Node album : DocUtils.NodeIterable.iterable(albums)) {
        	count++;
    	}
		assertTrue( 4 == count, "NodeIterable count" );
	}

	@Test
    public void testGetIndex() {
		assertEquals( 1, DocUtils.getIndex(albums, "artist", "Prince"), "node list index" );
		assertEquals( 3, DocUtils.getIndex(albums, "artist", "Taylor Swift"), "node list index" );
		assertEquals( -1, DocUtils.getIndex(albums, "artist", "crap"), "node list index" );
	}

	@Test
    public void testGetChildNodeText() {
		assertEquals( "Led Zeppelin", DocUtils.getChildNodeText( albums.item( 0 ), "artist"), "child node text" );
		assertTrue( null == DocUtils.getChildNodeText( albums.item( 0 ), "crap"), "non-existant child node text" );
	}

	@Test
    public void testUpdateChildNodeText() {
		assertEquals( "Led Zeppelin", DocUtils.getChildNodeText( albums.item( 0 ), "artist"), "get child node text" );
		DocUtils.updateChildNodeText( albums.item( 0 ), "artist", "John Bonham");
		assertEquals( "John Bonham", DocUtils.getChildNodeText( albums.item( 0 ), "artist"), "get child node text" );
	}

	@Test
    public void testXPathNode() throws XPathException {	
		assertEquals( 4, DocUtils.getXpathNodes(doc, "//Albums/Album").getLength(), "xpath nodes found" );
		
		assertEquals( "Led Zeppelin", DocUtils.getChildNodeText( albums.item( 0 ), "artist"), "get child node text" );
		assertTrue( DocUtils.updateXpathNode( doc, "//Albums/Album[1]/artist", "Fred Zeppelin", false ), "Xpath node item updated" );
		assertEquals( "Fred Zeppelin", DocUtils.getChildNodeText( albums.item( 0 ), "artist"), "get child node text" );
		assertTrue( !DocUtils.updateXpathNode( doc, "//Bad", "Fred Zeppelin", false ), "Xpath node item not updated" );
		assertEquals( "Fred Zeppelin", DocUtils.getChildNodeText( albums.item( 0 ), "artist"), "get child node text" );
		assertTrue( DocUtils.updateXpathNode( doc, "//Albums/Album[1]/artist", " Jr.", true ), "Xpath node item appended" );
		assertEquals( "Fred Zeppelin, Jr.", DocUtils.getChildNodeText( albums.item( 0 ), "artist"), "get child node text" );
	}

	@Test
    public void testCloneDocEmptyNode() throws XPathException, ParserConfigurationException {
	    Document editedDoc = DocUtils.cloneDoc( doc, null );
	    assertEquals( DocUtils.getXpathNodes(editedDoc, "//Albums/Album").getLength(), 
	    	DocUtils.getXpathNodes(doc, "//Albums/Album").getLength(), "clone doc node count");
	    
	    editedDoc = DocUtils.cloneDoc( doc, "Albums" );

	    assertEquals( 0, DocUtils.getXpathNodes(editedDoc, "//Albums/Album").getLength(), "empty Node Albums" ); 
	}
	
	@Test
    public void testWriteXML() throws IOException {
		String path = "testOutput.xml";
		DocUtils.writeOutput( doc, path );
			
		assertTrue( Files.exists( Path.of( path )), "output file created" ); 
		assertTrue( Files.isReadable( Path.of( path )), "output file readable" ); 
		assertTrue( 500 < Files.size( Path.of( path )), "output file size" ); 
		
		Files.deleteIfExists(Path.of( path ));
	}

}