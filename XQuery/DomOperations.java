package XQuery;

import org.apache.xerces.parsers.DOMParser;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import XQuery.DebugLogger;

import java.util.*;
import java.io.*;

public class DomOperations {

	public static final int RP_SIMPLE_FETCH = 1;
	public static final int RP_RECURSIVE_FETCH = 2;

	public static DebugLogger log = new DebugLogger("DomOperations");

	public DomOperations() {

	}

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
		return doc;
	}

	public static ArrayList<Node> RecursiveSearch(Node root, String tag) {
		// WARNING: this place may choose root into the result, which should
		// not happen, mind!
		ArrayList<Node> resultList = new ArrayList<Node>();
		if (root.getNodeName().equals(tag)) {
			// log.DebugLog(root.getNodeName() + "?" + tag);
			resultList.add(root);
		}
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			resultList.addAll(RecursiveSearch(children.item(i), tag));
		}
		return resultList;
	}

	public static ArrayList<Object> SimpleSearch(Node root, String tag) {
		ArrayList<Object> resultList = new ArrayList<>();
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeName().equals(tag)) {
				resultList.add(children.item(i));
			}
		}
		return resultList;
	}

	public static ArrayList<Object> getAllChildren(Node root) {
		ArrayList<Object> resultList = new ArrayList<>();
		NodeList children = root.getChildNodes();
		/*
		 * Warning: I found that, all the \n in the xml text are also seen as
		 * nodes by calling root.getChildNodes(). They are all text nodes with
		 * only "\n" as value. They do not make any sense in terms of syntax, so
		 * in order to correct the behavior of * in xPath, here we eliminate all
		 * the text nodes with only space characters. This potentially may cause
		 * problem in the future.
		 */
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() == Node.TEXT_NODE) {
				if (!(children.item(i).getNodeValue().trim().length() == 0)) {
					resultList.add(children.item(i));
				}
			} else {
				resultList.add(children.item(i));
			}
		}
		return resultList;
	}
}
