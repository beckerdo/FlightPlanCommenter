package info.danbecker.fpc.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringJoiner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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

/**
 * Utils for dealing with XML and org.w3c.dom.Document.
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public final class DocUtils {
	public static Document readInput( String inputPath ) throws ParserConfigurationException, SAXException, IOException {
		// Instantiate the Factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(new File(inputPath));
	}

	/** Return a count of the elements in doc with the given tagName.
	 * 
	 * @param doc
	 * @param tagName
	 * @return
	 */
	public static int getElementCount( Document doc, String tagName ) {
		NodeList elements = doc.getElementsByTagName(tagName);
		if ( null == elements) return 0;
		return elements.getLength();
	}

	/** Pretty print children node names and text from the Nodes with the given name.
	 * 
	 * @param doc
	 * @param nodeName
	 */
	public static void printElementsText( Document doc, String nodeName ) {
		NodeList header = doc.getElementsByTagName(nodeName);
		if (null != header && null != header.item(0)) {
			System.out.println(nodeName + ":");
			NodeList children = header.item(0).getChildNodes(); // get first
			for (int temp = 0; temp < children.getLength(); temp++) {
				Node node = children.item(temp);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					System.out.println("   " + node.getNodeName() + "=" + node.getTextContent());
				}
			}
		}	
	}
	
	/** 
	 * Append the node name and text for each of the nodes in the list.
	 * 
	 * @param nodeList
	 * @return
	 */
	public static String nodeListToString( NodeList nodeList ) {
		// NodeList has no forEach.
		StringJoiner sj = new StringJoiner(",");
		if ( null != nodeList ) {
			for ( int i = 0; i < nodeList.getLength(); i++ ) {
				Node item = nodeList.item(i);
				if (null != item && item.getNodeType() == Node.ELEMENT_NODE ) {
					sj.add( item.getNodeName() + "=" + item.getTextContent() );
				}
			}
		} else {
			sj.add( "null list");
		}
		return sj.toString();	
	}

	/** 
	 * Pretty print the given document to System.out
	 * @param doc
	 * @throws TransformerException
	 */
    public static final void prettyPrint(Document doc) throws TransformerException {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(doc), new StreamResult(out));
        System.out.println(out.toString());
    }

    /**
     * Return index of child node with given text or -1 if not found
     */
    public static int getIndex( NodeList list, String elementName, String text ) {
    	if ( null == list || null == text) return -1;
    	for (int nodei = 0; nodei < list.getLength(); nodei++ ) {
    		Node node = list.item( nodei );
    		String childText = DocUtils.getChildNodeText( node, elementName);
    		// System.out.println( "Child text=" + childText );
    		if ( null != childText && text.equals( childText ))
    			return nodei;    		
    	}
        return -1;
    }

    /**
     * Get text content of the child element with given name.
     */
    public static String getChildNodeText( Node node, String elementName ) {
    	if ( null == node || null == elementName) return null;
    	NodeList list = node.getChildNodes();
    	for (int nodei = 0; nodei < list.getLength(); nodei++ ) {
    		Node child = list.item( nodei );
    		if ( elementName.equals( child.getNodeName()))
    			return child.getTextContent();    		
    	}
        return null;
    }
    	
    /**
     * Update text content of this node child element with given name
     * If there is no child element with the given name, one is added.
     */
    public static String updateChildNodeText( Node node, String elementName, String textContent ) {
    	if ( null == node || null == elementName) return null;
    	NodeList list = node.getChildNodes();
    	for (int nodei = 0; nodei < list.getLength(); nodei++ ) {
    		Node child = list.item( nodei );
    		if ( elementName.equals( child.getNodeName())) {
    			child.setTextContent( textContent );
    			return textContent;
    		}
    	}
    	// Child node with element name does not exist. Add one.
        Node newChild = node.getOwnerDocument().createElement(elementName);
		newChild.setTextContent( textContent );
        node.appendChild(newChild);
        return null;
    }

    /** 
     * Use the Xpath given to find nodes in the document.
     * 
     * @param doc
     * @param xPath
     * @return
     * @throws XPathException
     */
	public static NodeList getXpathNodes(Document doc, String xPath) throws XPathException {
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
        XPathExpression expr = xp.compile( xPath );
        return (NodeList) expr.evaluate(doc, XPathConstants.NODESET);		
	}

	/** 
	 * Use the XPath given to set or append the given text in the node.
	 * @param doc
	 * @param xPath
	 * @param text
	 * @param append
	 * @return
	 * @throws XPathException
	 */
	public static boolean updateXpathNode(Document doc, String xPath, String text, boolean append) throws XPathException {
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp = xpf.newXPath();
        XPathExpression expr = xp.compile( xPath );
        Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
        if ( null != node ) {
        	if ( !append ) {
        		node.setTextContent(text);
        	} else {
            	String nodeText = node.getTextContent();
            	if ( !nodeText.contains(text)) {
            		node.setTextContent(nodeText + "," + text );
            	}
        	}
        	return true;
        } else { 
        	System.out.println( "Xpath=" + xPath + ", node not found" );        	
        }
        return false;
	}

	/** 
	 * Provides a clone of a document, safe for pruning and adding.
	 *  
	 * @param original
	 * @param emptyNode - if give, this node is replaced with an empty element.
	 * @return
	 * @throws ParserConfigurationException 
	 */
    public static Document cloneDoc( Document originalDocument, String emptyNode ) throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        Node originalRoot = originalDocument.getDocumentElement();

        Document copiedDocument = db.newDocument();
        Node copiedRoot = copiedDocument.importNode(originalRoot, true);
        copiedDocument.appendChild(copiedRoot);

        // Optionally prune the given node.
        if ( null != emptyNode ) {
        	emptyNode( copiedDocument, emptyNode );
        }
        // Perhaps append create time
    	return copiedDocument;
    }
    
    
    /**
     * Remove the first node with the given element name.
     * Append and empty node with the same name.
     * Append an
     * @param doc
     * @param nodeName
     * @param tagItemNum
     */
    public static void emptyNode( Document doc, String nodeName ) {
    	emptyNode( doc, nodeName, 0 ); // useful API for one decendent
    }
	
    /**
     * Remove the node with the given element name and index.
     * Append and empty node with the same name.
     * Append an
     * @param doc
     * @param nodeName
     * @param tagItemNum
     */
    public static void emptyNode( Document doc, String nodeName, int tagItemNum ) {
        Element element = (Element) doc.getElementsByTagName(nodeName).item(tagItemNum);
        Node wpParent = element.getParentNode();
        wpParent.removeChild(element);
        Element createdElement = doc.createElement(nodeName);
        wpParent.appendChild(createdElement);    	
    }
	
    /** Clone the given node into this document and append it as a child of the given parent.
     * 
     * @param doc
     * @param parent
     * @param node
     */
    public static void cloneAppendNode( Document doc, Element parent, Node node ) {
    	Node newNode = doc.importNode(node, true);
    	parent.appendChild(newNode);    	
    }
    public static void cloneAppendNode( Document doc, Element parent, Node node, String optionalElementText ) {
    	Node newNode = doc.importNode(node, true);
    	if ( null != optionalElementText && optionalElementText.length() > 0)
		    updateChildNodeText( newNode, "Comment", optionalElementText );
    	parent.appendChild(newNode);
    }
  
    /**
     * A simple class to give Java iteration to NodeList
     * Use like this:
     * NodeList nodeList = ...;
     * for (Node node : iterable(nodeList)) {
     *    // ....
     * }
     * or equivalently like this:
     * NodeList nodeList = ...;
     * iterable(nodeList).forEach(node -> {
     *    // ....
     * });
     * Thanks Stack overflow
     * https://stackoverflow.com/questions/19589231/can-i-iterate-through-a-nodelist-using-for-each-in-java/48153597
     * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
     *
     */
    public static class NodeIterable {
    	public static Iterable<Node> iterable(final NodeList n) {
    		return new Iterable<Node>() {
    			@Override
    			public Iterator<Node> iterator() {
    				return new Iterator<Node>() {
    					int index = 0;

    					@Override
    					public boolean hasNext() {
    						return index < n.getLength();
    					}

    					@Override
    					public Node next() {
    						if (hasNext()) {
    							return n.item(index++);
    						} else {
    							throw new NoSuchElementException();
    						}
    					}

    					@Override
    					public void remove() {
    						throw new UnsupportedOperationException();
    					}
    				};
    			}
    		};
    	}
    }
    
    /**
     * Write the given doc to the provided path
     * @param doc
     * @param path
     * @return
     * @throws IOException
     */
	public static boolean writeOutput( Document doc, String path ) throws IOException {
		// Validate path
		if (null == path || 0 == path.length() ) 
			throw new IllegalArgumentException( "output path must not be null or empty");
		if ( path.startsWith( "/" )) {
			path = path.substring(1); // clip  off leading / from URL;
		}
		Files.deleteIfExists(Path.of( path ));
	    boolean created = (new File(path)).createNewFile();
		if (!Files.isWritable( Path.of( path )) ) 
			throw new IllegalArgumentException( "output path must be writeable, path=" + path);		
		
        // write dom document to a file
        try (FileOutputStream output =  new FileOutputStream(path)) {
            writeXml(doc, output);
        } catch (IOException | TransformerException e ) {
            e.printStackTrace();
        }
        return created;
	}
	
    /** Write the given doc to the given output stream.
     * 
     * @param doc
     * @param output
     * @throws TransformerException
     * @throws IOException
     */
	public static void writeXml(Document doc, OutputStream output) 
		throws TransformerException, IOException {

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();		
		// get rid of standalone attribute which LNM does not like
		doc.setXmlStandalone(true);
		// Hack to insert newline which is not added by default (comment will be removed)
		doc.insertBefore(doc.createComment("DocUtils"), doc.getDocumentElement());
		DOMSource source = new DOMSource(doc);
		
		StringWriter outputXmlStringWriter = new StringWriter();
		StreamResult result = new StreamResult(outputXmlStringWriter);
		transformer.transform(source, result);

		// Now replace our comment with a newline
		String outputXmlString = outputXmlStringWriter.toString()
		    .replaceFirst("<!--DocUtils-->", "\n");

		output.write(outputXmlString.getBytes());
	}
}