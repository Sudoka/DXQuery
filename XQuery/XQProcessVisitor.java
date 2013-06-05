/**
 * 
 */
package XQuery;

import org.apache.xerces.parsers.DOMParser;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.*;
import java.io.*;

/**
 * @author QDX
 * 
 */
public class XQProcessVisitor implements XQueryParserVisitor,
		XQueryParserTreeConstants {

	public DebugLogger log;
	public NodeProcessor processor;

	public org.w3c.dom.Node root;

	public XQProcessVisitor() {
		log = new DebugLogger("XQProcessVisitor");
		processor = new NodeProcessor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.SimpleNode,
	 * java.lang.Object)
	 */
	@Override
	public Object visit(SimpleNode node, Object data) {
		log.RegularLog("SimpleNode, only called when the node is not implemented");
		data = node.childrenAccept(this, data);
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.AST_Root, java.lang.Object)
	 */
	@Override
	public Object visit(AST_Root node, Object data) {
		log.RegularLog("Visit: AST_Root" + " <" + node.jjtGetNumChildren()
				+ ">");
		data = node.childrenAccept(this, data);
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.AST_AP, java.lang.Object)
	 */
	@Override
	public Object visit(AST_AP node, Object data) {
		log.SetObjectControl(true, true, true);
		log.RegularLog("Visit: AST_AP" + " <" + node.jjtGetNumChildren() + ">");

		String filename = node.children[0].getText();

		log.DebugLog(filename);

		root = DomOperations.GetRootNodeFromPath(filename);

		log.DebugLog(root.getNodeName());

		for (XQuery.Node n : node.children) {
			log.DebugLog(this.jjtNodeName[((SimpleNode) n).getId()]);
		}

		ArrayList<Object> result = null;
		if (((SimpleNode) node.children[1]).getId() == JJTSINGLESLASH) {
			result = processor.ProcessRP((AST_RP) node.children[2], root,
					DomOperations.RP_SIMPLE_FETCH);
		} else if (((SimpleNode) node.children[1]).getId() == JJTDOUBLESLASH) {
			result = processor.ProcessRP((AST_RP) node.children[2], root,
					DomOperations.RP_RECURSIVE_FETCH);
		}
		if (result != null)
			log.DebugLog("The result size is:" + result.size());
		else
			log.DebugLog("The result size is: 0");
		if (result != null) {
			for (Object o : result) {
				if (o instanceof Node) {
					log.DebugLog("node name:" + ((Node) o).getNodeName());
				} else if (o instanceof String) {
					log.DebugLog("get String:" + o);
				} else {
					log.ErrorLog("returned list contain none Node and String type!");
				}
			}
		}
		// node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.AST_RP, org.w3c.dom.Node)
	 */
	@Override
	public Object visit(AST_RP node, Object data) {
		log.RegularLog("Visit: AST_RP" + " <" + node.jjtGetNumChildren() + ">");

		// data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.AST_PF, java.lang.Object)
	 */
	@Override
	public Object visit(AST_PF node, Object data) {
		log.RegularLog("Visit: ASP_PF");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.AST_XQ, java.lang.Object)
	 */
	@Override
	public Object visit(AST_XQ node, Object data) {
		log.RegularLog("Visit: AST_XQ" + " <" + node.jjtGetNumChildren() + ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.AST_FORCLAUSE,
	 * java.lang.Object)
	 */
	@Override
	public Object visit(AST_FORCLAUSE node, Object data) {
		log.RegularLog("Visit: AST_FORCLAUSE" + " <" + node.jjtGetNumChildren()
				+ ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.AST_LETCLAUSE,
	 * java.lang.Object)
	 */
	@Override
	public Object visit(AST_LETCLAUSE node, Object data) {
		log.RegularLog("Visit: AST_LETCLAUSE" + " <" + node.jjtGetNumChildren()
				+ ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.AST_WHERECLAUSE,
	 * java.lang.Object)
	 */
	@Override
	public Object visit(AST_WHERECLAUSE node, Object data) {
		log.RegularLog("Visit: AST_WHERECLAUSE" + " <"
				+ node.jjtGetNumChildren() + ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.AST_RETURNCLAUSE,
	 * java.lang.Object)
	 */
	@Override
	public Object visit(AST_RETURNCLAUSE node, Object data) {
		log.RegularLog("Visit: AST_RETURNCLAUSE" + " <"
				+ node.jjtGetNumChildren() + ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.AST_COND, java.lang.Object)
	 */
	@Override
	public Object visit(AST_COND node, Object data) {
		log.RegularLog("Visit: AST_COND" + " <" + node.jjtGetNumChildren()
				+ ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.AST_FILENAME,
	 * java.lang.Object)
	 */
	@Override
	public Object visit(AST_FILENAME node, Object data) {
		log.RegularLog("Visit: AST_FILENAME" + " <" + node.jjtGetNumChildren()
				+ ">");
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.AST_VAR, java.lang.Object)
	 */
	@Override
	public Object visit(AST_VAR node, Object data) {
		log.RegularLog("Visit: AST_VAR" + " <" + node.jjtGetNumChildren() + ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.AST_STRING,
	 * java.lang.Object)
	 */
	@Override
	public Object visit(AST_STRING node, Object data) {
		log.RegularLog("Visit: AST_STRING" + " <" + node.jjtGetNumChildren()
				+ ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.AST_TAGNAME,
	 * java.lang.Object)
	 */
	@Override
	public Object visit(AST_TAGNAME node, Object data) {
		log.RegularLog("Visit: AST_TAGNAME" + " <" + node.jjtGetNumChildren()
				+ ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return data;
	}

	@Override
	public Object visit(AST_DoubleSlash node, Object data) {
		log.RegularLog("Visit: AST_DoubleSlash" + " <"
				+ node.jjtGetNumChildren() + ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * @Override public Object visit(AST_RP2 node, Object data) {
	 * log.RegularLog("Visit: AST_RP2" + " <" + node.jjtGetNumChildren() + ">");
	 * data = node.childrenAccept(this, data); // TODO Auto-generated method
	 * stub return null; }
	 */
	@Override
	public Object visit(AST_SingleSlash node, Object data) {
		log.RegularLog("Visit: AST_SingleSlash" + " <"
				+ node.jjtGetNumChildren() + ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(AST_Comma node, Object data) {
		log.RegularLog("Visit: AST_Comma" + " <" + node.jjtGetNumChildren()
				+ ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(AST_Star node, Object data) {
		log.RegularLog("Visit: AST_Star" + " <" + node.jjtGetNumChildren()
				+ ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(AST_Dot node, Object data) {
		log.RegularLog("Visit: AST_Dot" + " <" + node.jjtGetNumChildren() + ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(AST_DDot node, Object data) {
		log.RegularLog("Visit: AST_DDot" + " <" + node.jjtGetNumChildren()
				+ ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(AST_Txt node, Object data) {
		log.RegularLog("Visit: AST_Txt" + " <" + node.jjtGetNumChildren() + ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(AST_Eq node, Object data) {
		log.RegularLog("Visit: AST_Eq" + " <" + node.jjtGetNumChildren() + ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(AST_Is node, Object data) {
		log.RegularLog("Visit: AST_Is" + " <" + node.jjtGetNumChildren() + ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(AST_Not node, Object data) {
		log.RegularLog("Visit: AST_Not" + " <" + node.jjtGetNumChildren() + ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(AST_And node, Object data) {
		log.RegularLog("Visit: AST_And" + " <" + node.jjtGetNumChildren() + ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(AST_Or node, Object data) {
		log.RegularLog("Visit: AST_Or" + " <" + node.jjtGetNumChildren() + ">");
		data = node.childrenAccept(this, data);
		// TODO Auto-generated method stub
		return null;
	}

}
