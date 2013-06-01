package XQuery;

import org.apache.xerces.parsers.DOMParser;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import XQuery.DebugLogger;

import java.util.*;
import java.io.*;

public class DomOperations {

	public static DebugLogger log = new DebugLogger("DomOperations");

	public static Node GetRootNodeFromPath(String path) {
		DOMParser p = new DOMParser();
		try {
			p.parse(path);
		} catch (SAXException e) {
			e.printStackTrace();
			log.ErrorLog("Parsing path:" + path + " failed!");
		} catch (IOException e) {
			e.printStackTrace();
			log.ErrorLog("Parsing path:" + path + " failed!");
		}
		Document doc = p.getDocument();
		Node root = doc.getDocumentElement();
		return root;
	}

	public static ArrayList<Node> RecursiveSearch(Node root, String tag) {
		ArrayList<Node> resultList = new ArrayList<Node>();
		if (root.getNodeName().equals(tag)) {
			log.DebugLog(root.getNodeName() + "?" + tag);
			resultList.add(root);
		}
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			resultList.addAll(RecursiveSearch(children.item(i), tag));
		}
		return resultList;
	}

}
