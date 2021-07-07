package net.schwarzbaer.java.lib.openwebif;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RemoteControl {
	
//	http://192.168.2.75/static/remotes/
//	http://192.168.2.75/static/remotes/et7x00/rc.png
//	http://192.168.2.75/static/remotes/et7x00/rcpositions.xml
//	http://192.168.2.75/static/remotes/et7x00/remote.html
	
	private final String machinebuild;

	public RemoteControl(SystemInfo systemInfo) {
		if (systemInfo==null) throw new IllegalArgumentException("new RemoteControl( <null> ) is not allowed");
		if (systemInfo.info==null || systemInfo.info.machinebuild==null) throw new IllegalStateException(); 
		machinebuild = systemInfo.info.machinebuild;
	}
	
	public BufferedImage getRemoteControlImage(String baseURL, Consumer<String> setIndeterminateProgressTask) {
		return getRemoteControlImage(baseURL, machinebuild, setIndeterminateProgressTask);
	}

	public static BufferedImage getRemoteControlImage(String baseURL, String machinebuild, Consumer<String> setIndeterminateProgressTask) {
		if (setIndeterminateProgressTask!=null) setIndeterminateProgressTask.accept("Get RemoteControl Image");
		baseURL = OpenWebifTools.removeAllTrailingSlashes(baseURL);
		
		// http://192.168.2.75/static/remotes/et7x00/rc.png
		String url = String.format("%s/static/remotes/%s/rc.png", baseURL, machinebuild);
		return OpenWebifTools.getImage(url);
	}
	
	public Key[] getKeys(String baseURL, Consumer<String> setIndeterminateProgressTask) {
		return getKeys(baseURL, setIndeterminateProgressTask, machinebuild);
	}

	public static Key[] getKeys(String baseURL, Consumer<String> setIndeterminateProgressTask, String machinebuild) {
		baseURL = OpenWebifTools.removeAllTrailingSlashes(baseURL);
		
		if (setIndeterminateProgressTask!=null) setIndeterminateProgressTask.accept("Get RemoteControl Description File");
		// http://192.168.2.75/static/remotes/et7x00/remote.html
		String url = String.format("%s/static/remotes/%s/remote.html", baseURL, machinebuild);
		//String url = String.format("%s/static/remotes/%s/rcpositions.xml", baseURL, machinebuild);
		 
		String content = OpenWebifTools.getContent(url);
		if (content==null) return null;
		
		if (setIndeterminateProgressTask!=null) setIndeterminateProgressTask.accept("Parse RemoteControl Description");
		content = fixDirtyXML(content);
		//System.out.println();
		//System.out.println("Raw RemoteControl Description:");
		//System.out.println(content);
		//System.out.println();
		Document doc = XML.parse(content);
		//if (doc != null) {
		//	System.out.println("Parsed RemoteControl Description:");
		//	XML.showXMLformated(doc);
		//}
		
		return parseDocument(doc);
	}

	private static Key[] parseDocument(Document doc) {
		if (doc==null) return null;
		
		NodeList areaNodes = doc.getElementsByTagName("area");
		Key[] keys = new Key[areaNodes.getLength()];
		for (int i=0; i<areaNodes.getLength(); i++)
			keys[i] = Key.parse(areaNodes.item(i),i+1);
		
		return keys;
	}
	
	public static class Key {

		public final String title;
		public final String keyCode;
		public final Shape shape;

		public Key(String title, String keyCode, Shape shape) {
			this.title = title;
			this.keyCode = keyCode;
			this.shape = shape;
		}

		private static Key parse(Node areaNode, int keyIndex) {
			NamedNodeMap attributes = areaNode.getAttributes();
			Node   shapeAttr = attributes.getNamedItem("shape");
			Node  coordsAttr = attributes.getNamedItem("coords");
			Node   titleAttr = attributes.getNamedItem("title");
			Node onclickAttr = attributes.getNamedItem("onclick");
			
			String   shapeStr =   shapeAttr==null ? null :   shapeAttr.getNodeValue();
			String  coordsStr =  coordsAttr==null ? null :  coordsAttr.getNodeValue();
			String   title    =   titleAttr==null ? null :   titleAttr.getNodeValue();
			String onclickStr = onclickAttr==null ? null : onclickAttr.getNodeValue();
			
			Shape shape = Shape.parse(shapeStr,coordsStr,keyIndex);
			
			// onclick="pressMenuRemote('8');"
			String prefix = "pressMenuRemote('";
			String suffix = "');";
			String keyCode = null;
			if (onclickStr.startsWith(prefix) && onclickStr.endsWith(suffix))
				keyCode = onclickStr.substring(prefix.length(), onclickStr.length()-suffix.length());
			else
				System.err.printf("Parsing RemoteControl Description: Can't parse key %d. Found unexpected 'onclick' value: \"%s\"%n", keyIndex, onclickStr);
			
			return new Key(title, keyCode, shape);
		}
		
		public static class Shape {
			public enum Type { Rect, Circle }
			
			public final Type type;
			public final Point center;
			public final int radius;
			public final Point corner1;
			public final Point corner2;
			
			private Shape(Point center, int radius) {
				type = Type.Circle;
				this.corner1 = null;
				this.corner2 = null;
				this.center = center;
				this.radius = radius;
			}
			private Shape(Point corner1, Point corner2) {
				type = Type.Rect;
				this.corner1 = corner1;
				this.corner2 = corner2;
				this.center = null;
				this.radius = 0;
			}
			public static Shape parse(String shapeStr, String coordsStr, int keyIndex) {
				if (shapeStr ==null) { System.err.printf("Parsing RemoteControl Description: Can't parse shape %d. No shape type found.%n", keyIndex); return null; }
				if (coordsStr==null) { System.err.printf("Parsing RemoteControl Description: Can't parse shape %d. No coords found.%n", keyIndex); return null; }
				
				String[] coordsStrs = coordsStr.split(",");
				int[] coordsInts;
				switch (shapeStr) {
				case "rect":
					// "79,86,105,97"
					if (coordsStrs.length!=4) {
						System.err.printf("Parsing RemoteControl Description: Can't parse shape %d. Unexpected number of coord values found: expected 4 values for a rectangle , but found %d values in \"%s\"%n", keyIndex, coordsStrs.length, coordsStr);
						return null;
					}
					coordsInts = parseInts(coordsStrs, keyIndex, coordsStr);
					return new Shape(new Point(coordsInts[0], coordsInts[1]), new Point(coordsInts[2], coordsInts[3]));
					
				case "circle":
					// "95,132,7"
					if (coordsStrs.length!=3) {
						System.err.printf("Parsing RemoteControl Description: Can't parse shape %d. Unexpected number of coord values found: expected 3 values for a circle, but found %d values in \"%s\"%n", keyIndex, coordsStrs.length, coordsStr);
						return null;
					}
					coordsInts = parseInts(coordsStrs, keyIndex, coordsStr);
					return new Shape(new Point(coordsInts[0], coordsInts[1]), coordsInts[2]);
					
				default:
					System.err.printf("Parsing RemoteControl Description: Can't parse shape %d. Unexpected shape type found: \"%s\"%n", keyIndex, shapeStr);
					return null;
				}
			}
			
			private static int[] parseInts(String[] strs, int keyIndex, String coordsStr) {
				int[] result = new int[strs.length];
				for (int i=0; i<strs.length; i++) {
					String str = strs[i];
					try {
						result[i] = Integer.parseInt(str);
					} catch (NumberFormatException e) {
						// e.printStackTrace();
						System.err.printf("Parsing RemoteControl Description: Can't parse shape %d. Unexpected coord value found: %d. value of \"%s\" is \"%s\"%n", keyIndex, i+1, coordsStr, str);
						return null;
					}
				}
				return result;
			}
		}
	}

	private static String fixDirtyXML(String content) {
		StringWriter strOut = new StringWriter();
		try (
				BufferedReader in = new BufferedReader(new StringReader(content));
				PrintWriter out = new PrintWriter(strOut);
		) {
			out.println("<root>");
			String line;
			while ( (line=in.readLine())!=null ) {
				
				if (line.startsWith("	<area ") && line.endsWith("\">")) {
					line = line.substring(0, line.length()-1) + "/>";
					
				} else if (line.startsWith("<img ") && line.endsWith(" >")) {
					line = line.substring(0, line.length()-1) + "/>";
					
				}
				out.println(line);
			}
			out.println("</root>");
			
		} catch (IOException e) {}
		return strOut.toString();
	}
	
	@SuppressWarnings("unused")
	private static class XML {

		private static Document parse(String xmlStr) {
			if (xmlStr==null) return null;
			try {
				return DocumentBuilderFactory
						.newInstance()
						.newDocumentBuilder()
						.parse(new InputSource(new StringReader(xmlStr)));
			} catch (SAXException | IOException | ParserConfigurationException e) {
				e.printStackTrace();
				return null;
			}
		}

		public static void showXMLformated(Document document) {
			StringBuilder sb = new StringBuilder();
			showXMLformated(sb,document);
			System.out.print(sb);
		}

		public static void showXMLformated(StringBuilder sb, Document document) {
			if (document==null) return;
			showXMLformated(sb,"",document);
		}

		private static void showXMLformated(StringBuilder sb, String indent, Node node) {
			sb.append(indent+toString(node)+"\r\n");
			NodeList childNodes = node.getChildNodes();
			for (int i=0; i<childNodes.getLength(); ++i)
				showXMLformated(sb,indent+"|   ", childNodes.item(i));
		}

		public static String toString(Node node) {
			switch (node.getNodeType()) {
			case Node.DOCUMENT_NODE     : return toString((Document    )node);
			case Node.ELEMENT_NODE      : return toString((Element     )node);
			case Node.TEXT_NODE         : return toString((Text        )node);
			case Node.COMMENT_NODE      : return toString((Comment     )node);
			case Node.CDATA_SECTION_NODE: return toString((CDATASection)node);
			}
			return String.format("[%d] %s", node.getNodeType(), node.getNodeName());
		}

		public static String toString(Comment comment) {
			return String.format("<!-- %s -->", comment.getNodeValue());
		}

		public static String toString(CDATASection cdataSection) {
			return String.format("[CDATA %s ]", cdataSection.getNodeValue());
		}

		public static String toString(Text text) {
			return String.format("\"%s\"", text.getNodeValue());
		}

		public static String toString(Element element) {
			StringBuilder sb = new StringBuilder();
			NamedNodeMap attributes = element.getAttributes();
			for (int i=0; i<attributes.getLength(); ++i) {
				Node attr = attributes.item(i);
				sb.append(String.format(" %s=\"%s\"", attr.getNodeName(), attr.getNodeValue()));
			}
			return String.format("<%s%s>", element.getNodeName(), sb.toString());
		}

		public static String toString(Document document) {
			String str = "Document "+document.getNodeName();
			if (document.getDoctype    ()!=null) str += " DocType:"    +document.getDoctype    ();
			if (document.getBaseURI    ()!=null) str += " BaseURI:"    +document.getBaseURI    ();
			if (document.getDocumentURI()!=null) str += " DocURI:"     +document.getDocumentURI();
			if (document.getXmlEncoding()!=null) str += " XmlEncoding:"+document.getXmlEncoding();
			if (document.getXmlVersion ()!=null) str += " XmlVersion:" +document.getXmlVersion ();
			return str;
		}
		
	}
	
}
