import org.apache.xerces.parsers.DOMParser;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import XQuery.DebugLogger;

import java.util.*;
import java.io.*;

/**
 * Parses and prints content of test.xml file. The result should be identical to
 * the input except for the whitespace.
 */
public class DOMPrinter {
	public static DebugLogger log = new DebugLogger("DOMPrinter");
	public static void main(String[] args) {
		
		try {
			DOMParser p = new DOMParser();
			p.parse("bib.xml");
			Document doc = p.getDocument();
			Node n = doc.getDocumentElement();
			System.out.println("<?xml version=\"1.0\"?>");
			ArrayList<Node> result = RecursiveSearch(n, "last");
			for(Node i : result){
				log.RegularLog(i.getNodeName());
			}
			if (n != null) {
				// System.out.println(printXML(n));
				log.RegularLog(n.getNodeName());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<Node> RecursiveSearch(Node root, String tag) {
		ArrayList<Node> resultList = new ArrayList<Node>();
		if (root.getNodeName().equals(tag)) {
			// log.DebugLog(root.getNodeName()+"?"+tag);
			resultList.add(root);
		}
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			resultList.addAll(RecursiveSearch(children.item(i), tag));
		}
		return resultList;
	}

	public static String printXML(Node node) throws IOException {
		StringWriter str = new StringWriter();

		printWithFormat(node, str, 0, false);
		str.flush();

		return str.toString();
	}

	private final static void printWithFormat(Node node, Writer wr, int n,
			boolean flush) throws IOException {
		switch (node.getNodeType()) {
		case Node.ELEMENT_NODE: {
			System.out.println("ELEMENT NODE");
			// Print opening tag
			wr.write(makeTabs(n) + "<" + node.getNodeName() + " ");
			if (flush)
				wr.flush();

			// Print attributes (if any)
			NamedNodeMap attrs = node.getAttributes();
			Node attr = null;
			if (attrs != null) {
				for (int i = 0; i < attrs.getLength(); i++) {
					attr = attrs.item(i);
					wr.write(attr.getNodeName() + "=\"" + attr.getNodeValue()
							+ "\" ");
					if (flush)
						wr.flush();
				}
			}

			wr.write(">\n");
			if (flush)
				wr.flush();

			// recursively print children
			Node ch = node.getFirstChild();
			while (ch != null) {
				printWithFormat(ch, wr, n + 1, flush);
				if (flush)
					wr.flush();
				ch = (Node) ch.getNextSibling();
			}
			wr.write(makeTabs(n) + "</" + node.getNodeName() + " >\n");
			if (flush)
				wr.flush();
		}
			break;

		case Node.TEXT_NODE: {
			System.out.println("TEXT NODE");
			String text = node.getNodeValue().trim();
			// Make sure we don't print whitespace
			if (text.length() > 0)
				wr.write(makeTabs(n) + text + "\n");
			if (flush)
				wr.flush();
		}
			break;

		default:
			throw new IOException("Cannot print this type of element");
		}
	}

	private static final String makeTabs(int n) {
		StringBuffer result = new StringBuffer("");
		for (int i = 0; i < n; i++)
			result.append("\t");
		return result.toString();
	}
}