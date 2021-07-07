package net.schwarzbaer.java.lib.openwebif;

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
		content = fixXML(content);
		//System.out.println();
		//System.out.println("Raw RemoteControl Description:");
		//System.out.println(content);
		//System.out.println();
		Document doc = parse(content);
		if (doc != null) {
			System.out.println("Parsed RemoteControl Description:");
			showXMLformated(doc);
		}
		
		// TODO
		return new Key[0];
	}
	
	private static String fixXML(String content) {
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
	
	public static class Key {
		
	}
	
}
