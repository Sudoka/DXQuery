/**
 * 
 */
package XQuery;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.*;
import java.lang.invoke.MethodHandles.Lookup;

/**
 * @author QDX
 * 
 */
public class XQProcessVisitor implements XQueryParserVisitor,
		XQueryParserTreeConstants {

	public DebugLogger log;
	public NodeProcessor processor;

	public org.w3c.dom.Node root;
	public Document doc;

	public final int REMOVE_VARKEEPER = 1;
	public final int REMOVE_CONTEXT = 2;

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
		// root should only has one node, which is AST_XQ
		assert (node.jjtGetNumChildren() == 1);
		VariableKeeper result = (VariableKeeper) node.children[0].jjtAccept(
				this, new XContext());

		log.DebugLog("Got result size:" + result.size());
		// try {
		// DOMPrinter.printXML(result.GetVarNodeList().get(0).node);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		for (Node n : result.GetNodes()) {

			log.DebugLog("Node:" + n.getNodeName());
			if (n.getNodeType() == Node.TEXT_NODE) {
				log.DebugLog("text node value:" + n.getNodeValue());
			}
			try {
				System.out.println(DOMPrinter.printXML(n));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
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
		doc = (Document) root;

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
					if (((Node) o).getNodeType() == Node.TEXT_NODE) {
						log.DebugLog("text node value:"
								+ ((Node) o).getNodeValue());
					}
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
		if (!(data instanceof XContext)) {
			log.ErrorLog("data not instanceof Context!");
		}
		VariableKeeper result = new VariableKeeper();
		XContext firstContext = null;

		int childrenNum = node.jjtGetNumChildren();
		if (childrenNum == 0) {
			log.ErrorLog("An XQ node should not have 0 children!");
			return null;
		}
		SimpleNode firstChild = (SimpleNode) node.children[0];

		int firstChildId = firstChild.getId();
		switch (firstChildId) {
		// encountering AP and Tag name can be done immediately
		case JJTAP:
			// appearance of AP at the first child implies this XQ only has one
			// node
			assert (childrenNum == 1);
			ArrayList<Object> apResult = (ArrayList<Object>) ((AST_AP) firstChild)
					.jjtAccept(this, data);
			result.SimpleAddNodeList(apResult);
			return result;
		case JJTTAGNAME:

			String tag1 = firstChild.getText();
			String tag2 = node.children[2].getText();
			assert (tag1.equals(tag2));
			AST_XQ secondChild = (AST_XQ) node.children[1];
			VariableKeeper tmpXQ = (VariableKeeper) secondChild.jjtAccept(this,
					data);
			DocumentImpl docNew = new DocumentImpl();
			Element newRoot = docNew.createElement(tag1);
			for (Node n : tmpXQ.GetNodes()) {
				newRoot.appendChild(docNew.importNode(n, true));
			}
			ArrayList<Object> tmpAdd = new ArrayList<Object>();
			tmpAdd.add(newRoot);
			result.SimpleAddNodeList(tmpAdd);
			if (childrenNum == 3) {
				return result;
			}
			break;
		// appearance of Var in the first child does not imply much on the
		// production rule
		case JJTVAR:
			String name = ((AST_VAR) firstChild).getText();
			VariableKeeper tmpResult = ((XContext) data).Lookup(name);
			if (!(tmpResult == null)) {
				result = tmpResult;
			}
			log.RegularLog("case JJTVAR and binds to "
					+ (result == null ? null : result.size()) + " nodes");
			break;
		case JJTSTRING:
			String string = firstChild.getText();
			string = new String(string.substring(1, string.length() - 1));
			Node newTextNode = doc.createTextNode(string);
			tmpAdd = new ArrayList<Object>();
			tmpAdd.add(newTextNode);
			result.SimpleAddNodeList(tmpAdd);
			break;
		case JJTXQ:
			result = (VariableKeeper) firstChild.jjtAccept(this, data);
			break;
		case JJTFORCLAUSE:
			assert (childrenNum >= 2);
			// get new context from for clause, we will pass it to let where and
			// return clause after this
			firstContext = (XContext) firstChild.jjtAccept(this, data);
			break;
		case JJTLETCLAUSE:
			assert (childrenNum == 2);
			// get new context from let clause, we will pass it to later clauses
			firstContext = (XContext) firstChild.jjtAccept(this, data);
			break;
		default:
			log.ErrorLog("Encountered unexpected first child:["
					+ jjtNodeName[firstChildId] + "] with children number:<"
					+ childrenNum + "> !");
			break;
		}
		if (childrenNum == 1) {
			return result;
		}

		switch (childrenNum) {
		case 2:
			SimpleNode secondChild = (SimpleNode) node.children[1];
			int secondChildId = secondChild.getId();
			switch (secondChildId) {
			case JJTRETURNCLAUSE:
				return secondChild.jjtAccept(this, firstContext);
			case JJTXQ:
				return secondChild.jjtAccept(this, firstContext);
			default:
				log.ErrorLog("Encountered unexpected second child:["
						+ jjtNodeName[secondChildId] + "] \nwith first child:["
						+ jjtNodeName[firstChildId] + "] !");
				break;
			}
		case 3:
			secondChild = (SimpleNode) node.children[1];
			secondChildId = secondChild.getId();
			SimpleNode thirdChild = (SimpleNode) node.children[2];
			int thirdChildId = thirdChild.getId();
			switch (secondChildId) {
			case JJTSINGLESLASH:
			case JJTDOUBLESLASH:
				assert (thirdChildId == JJTRP);
				int opeartion = secondChildId == JJTSINGLESLASH ? DomOperations.RP_SIMPLE_FETCH
						: DomOperations.RP_RECURSIVE_FETCH;
				VariableKeeper tmpResult = ((AST_RP) thirdChild)
						.EvaluateRPUnderVariable(doc, result,
								(AST_RP) thirdChild, opeartion);
				return tmpResult;
			case JJTCOMMA:
				assert (thirdChildId == JJTXQ);
				/**
				 * with comma, the xq on the left of comma and right of comma
				 * should be evaluated under the same context, thus pass data as
				 * parameter. Also,
				 */
				VariableKeeper result2 = (VariableKeeper) thirdChild.jjtAccept(
						this, data);
				VariableKeeper finalResult = result.CreateByMerge(result2);
				return finalResult;
			case JJTLETCLAUSE:
				assert (firstChildId == JJTFORCLAUSE && thirdChildId == JJTRETURNCLAUSE);
				XContext secondContext = (XContext) secondChild.jjtAccept(this,
						firstContext);
				return thirdChild.jjtAccept(this, secondContext);
			case JJTWHERECLAUSE:
				assert (firstChildId == JJTFORCLAUSE && thirdChildId == JJTRETURNCLAUSE);
				secondContext = (XContext) secondChild.jjtAccept(this,
						firstContext);
				return thirdChild.jjtAccept(this, secondContext);
			default:
				log.ErrorLog("Encountered unexpected third child:["
						+ jjtNodeName[thirdChildId] + "] \nwith first child:["
						+ jjtNodeName[firstChildId] + "] \nwith second child:["
						+ jjtNodeName[secondChildId] + "] !");
				break;
			}
			break;
		case 4:
			secondChild = (SimpleNode) node.children[1];
			secondChildId = secondChild.getId();
			thirdChild = (SimpleNode) node.children[2];
			thirdChildId = thirdChild.getId();
			SimpleNode fourthChild = (SimpleNode) node.children[3];
			int fourthChildId = fourthChild.getId();
			assert (firstChildId == JJTFORCLAUSE
					&& secondChildId == JJTLETCLAUSE
					&& thirdChildId == JJTWHERECLAUSE && fourthChildId == JJTRETURNCLAUSE);
			XContext secondContext = (XContext) secondChild.jjtAccept(this,
					firstContext);
			XContext thirdContext = (XContext) thirdChild.jjtAccept(this,
					secondContext);
			return fourthChild.jjtAccept(this, thirdContext);
		case 5:
			SimpleNode fifthChild = (SimpleNode) node.children[4];
			int fifthChildId = fifthChild.getId();
			assert (fifthChildId == JJTXQ);
			VariableKeeper result2 = (VariableKeeper) fifthChild.jjtAccept(
					this, data);
			VariableKeeper finalResult = result.CreateByMerge(result2);
			return finalResult;
		default:
			secondChild = (SimpleNode) node.children[1];
			secondChildId = secondChild.getId();
			thirdChild = (SimpleNode) node.children[2];
			thirdChildId = thirdChild.getId();
			fourthChild = (SimpleNode) node.children[3];
			fourthChildId = fourthChild.getId();
			log.ErrorLog("Encountered unexpected third child:["
					+ jjtNodeName[thirdChildId] + "] \nwith first child:["
					+ jjtNodeName[firstChildId] + "] \nwith second child:["
					+ jjtNodeName[secondChildId] + "] \nwith fourth child:["
					+ jjtNodeName[fourthChildId] + "] !");
			break;
		}
		log.ErrorLog("Returned result at the very end of XQ!");
		return result;
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
		return VisitLetOrFor(node, data);
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
		return VisitLetOrFor(node, data);
	}

	/**
	 * For clause and Let clase has exactly the same structure, so here we can
	 * use one subroutine to process these two nodes.
	 * 
	 * @param node
	 *            a for node or a let node
	 * @param data
	 *            the context
	 * @return a new context with all the new variables bindings
	 */
	private Object VisitLetOrFor(SimpleNode node, Object data) {
		assert (node instanceof AST_FORCLAUSE || node instanceof AST_LETCLAUSE);
		XContext result = ((XContext) data).clone();
		XContext context = (XContext) data;
		log.ErrorLog("If assertion failure occurs here, the children"
				+ " number of for clause has problem!");
		assert ((node.jjtGetNumChildren() + 1) % 3 == 0);
		int varNum = (node.jjtGetNumChildren() + 1) / 3;
		for (int i = 0; i < varNum; i++) {
			SimpleNode nameNode = (SimpleNode) node.children[i * 3];
			SimpleNode xqNode = (SimpleNode) node.children[i * 3 + 1];
			log.DebugLog("xqNode:" + jjtNodeName[xqNode.getId()] + "; varNode:"
					+ jjtNodeName[nameNode.getId()]);
			assert (xqNode.getId() == JJTXQ && nameNode.getId() == JJTVAR);
			String varName = nameNode.getText();
			// get the value
			VariableKeeper xqResult = (VariableKeeper) xqNode.jjtAccept(this,
					result);
			// set name to the value
			xqResult.SetName(varName);
			// bind the value to variable
			result.Extend(varName, xqResult);
		}
		log.DebugLog("=>in VisitLetOrFor, here are the variable bindings:");
		for (String varName : result.GetVarNames()) {
			System.out.println(varName);
			for (Node oneNode : result.Lookup(varName).GetNodes()) {
				System.out.println(oneNode.getNodeName());
			}
		}

		return result;
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
		assert (node.jjtGetNumChildren() == 1);
		SimpleNode xqNode = (SimpleNode) node.children[0];
		assert (xqNode.getId() == JJTXQ);
		return xqNode.jjtAccept(this, data);
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
		assert (node.jjtGetNumChildren() == 1);
		XContext newContext = ((XContext) data).clone();
		// TODO: add more code here to remove the unsatisified nodes
		Object toBeRemoved = node.children[0].jjtAccept(this, newContext);
		if (toBeRemoved instanceof XContext) {
			return new XContext();
		} else {
			VariableKeeper removeList = (VariableKeeper) toBeRemoved;
			log.DebugLog("In Where, to be removed:" + removeList.size());
			removeList.PrintAllVars();
			newContext.RecursiveRemoveVariableKeeper(removeList);
			return newContext;
		}
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
		XContext newContext = ((XContext) data).clone();

		int childrenNum = node.jjtGetNumChildren();
		if (childrenNum == 0) {
			log.ErrorLog("An Cond node should not have 0 children!");
			return null;
		}
		SimpleNode firstChild = (SimpleNode) node.children[0];
		int firstChildId = firstChild.getId();

		VariableKeeper removeList = new VariableKeeper();
		XContext removeContext = new XContext();
		int removeFlag = 0;
		switch (firstChildId) {
		case JJTCOND:
			Object removeOb = firstChild.jjtAccept(this, newContext);
			if (removeOb instanceof XContext) {
				removeFlag = REMOVE_CONTEXT;
				removeContext = (XContext) removeOb;
				if (childrenNum == 1)
					return removeContext;
			} else {
				removeFlag = REMOVE_VARKEEPER;
				removeList = (VariableKeeper) removeOb;
				if (childrenNum == 1)
					return removeList;
			}
			break;

		case JJTXQ:
			assert (childrenNum >= 3);
			((XContext) data).DebugPrintAllBingdings();
			VariableKeeper xqResult1 = (VariableKeeper) firstChild.jjtAccept(
					this, data);
			SimpleNode secondChild = (SimpleNode) node.children[1];
			int secondChildId = secondChild.getId();
			SimpleNode thirdChild = (SimpleNode) node.children[2];
			int thirdChildId = thirdChild.getId();
			assert (thirdChildId == JJTXQ);
			switch (secondChildId) {
			case JJTEQ:
			case JJTIS:
				VariableKeeper xqResult2 = (VariableKeeper) thirdChild
						.jjtAccept(this, data);
				xqResult1.PrintAllVars();
				System.out.println("-------------------------");
				xqResult2.PrintAllVars();
				int operation = (secondChildId == JJTEQ ? VariableKeeper.EQ_DISJOINT
						: VariableKeeper.IS_DISJOINT);
				removeList = xqResult1.DisJoint(xqResult2, operation);
				System.out.println("-------------------------");
				removeList.PrintAllVars();
				removeFlag = REMOVE_VARKEEPER;
				break;
			default:
				log.ErrorLog("Evaluating Cond, encountered unexpected second child:"
						+ jjtNodeName[secondChildId]
						+ " after XQ as the first child.");
				break;
			}
			if (childrenNum == 3) {
				return removeList;
			}
			break;
		case JJTEMPTY:
			secondChild = (SimpleNode) node.children[1];
			secondChildId = secondChild.getId();
			assert (secondChildId == JJTXQ);
			VariableKeeper xqResult = (VariableKeeper) secondChild.jjtAccept(
					this, data);

			if (xqResult == null || xqResult.size() == 0) {
				removeList = new VariableKeeper();
				removeFlag = REMOVE_VARKEEPER;
				if (childrenNum == 2) {
					return removeList;
				}
			} else {
				removeContext = (XContext) data;
				removeFlag = REMOVE_CONTEXT;
				if (childrenNum == 2)
					return removeContext;
			}
			break;
		case JJTSOME:
			XContext someResult = null;
			XContext tmpContext = ((XContext) data).clone();
			boolean canReturn = false;
			removeFlag = REMOVE_VARKEEPER;
			// evaluate some with correct number of children
			if (((SimpleNode) node.children[childrenNum - 2]).getId() == JJTAND
					|| ((SimpleNode) node.children[childrenNum - 2]).getId() == JJTOR) {
				AST_COND tmp = new AST_COND(JJTCOND);
				for (int i = 0; i < childrenNum - 2; i++) {
					tmp.jjtAddChild(node.children[i], i);
				}
				someResult = EvaluateSome(tmp, (XContext) data);
			} else {
				canReturn = true;
				someResult = EvaluateSome(node, (XContext) data);
			}
			for (String varName : tmpContext.GetVarNames()) {
				if (someResult.Lookup(varName) == null
						|| someResult.Lookup(varName).size() == 0) {
					removeList = removeList.CreateByMerge(tmpContext
							.Lookup(varName));
				}
			}
			if (canReturn)
				return removeList;
			break;
		case JJTNOT:
			secondChild = (SimpleNode) node.children[1];
			secondChildId = secondChild.getId();
			assert (secondChildId == JJTCOND);
			removeOb = secondChild.jjtAccept(this, newContext);
			removeFlag = REMOVE_VARKEEPER;
			if (removeOb instanceof XContext) {
				removeList = new VariableKeeper();
			} else {
				VariableKeeper wholeVarKeeper = new VariableKeeper();
				// for (String varName : newContext.GetVarNames()) {
				// wholeVarKeeper = wholeVarKeeper.CreateByMerge(newContext
				// .Lookup(varName));
				// }
				wholeVarKeeper = newContext.Zip();
				VariableKeeper tmpRemove = (VariableKeeper) removeOb;
				wholeVarKeeper.Subtract(tmpRemove);
				removeList = wholeVarKeeper;
				if (childrenNum == 2) {
					return removeList;
				}
			}
			break;
		default:
			log.ErrorLog("Encoumtered unxepected first child:"
					+ jjtNodeName[firstChildId]);
			break;
		}

		SimpleNode operatorNode = (SimpleNode) node.children[childrenNum - 2];
		int operatorId = operatorNode.getId();
		assert (operatorId == JJTAND || operatorId == JJTOR);
		Object removeOb = node.children[childrenNum - 1].jjtAccept(this,
				newContext);
		if (operatorId == JJTAND) {
			// make union and return
			if (removeFlag == REMOVE_CONTEXT) {
				return removeContext;
			} else if (removeFlag == REMOVE_VARKEEPER) {
				if (removeOb instanceof XContext) {
					return (XContext) removeOb;
				} else {
					return removeList.CreateByMerge((VariableKeeper) removeOb);
				}
			} else {
				log.ErrorLog("Remove Flag has unexpected value!!!");
			}
		} else {
			// make overlap and return
			if (removeFlag == REMOVE_CONTEXT) {
				return removeOb;
			} else {
				if (removeOb instanceof XContext) {
					return removeList;
				} else {
					return removeList.Intersect((VariableKeeper) removeOb);
				}
			}
		}

		log.ErrorLog("Returning at the end of Cond, not expected!!");
		return data;
	}

	private XContext EvaluateSome(AST_COND node, XContext context) {
		XContext result = context.clone();
		// bind variables:
		int childrenNum = node.jjtGetNumChildren();
		assert ((childrenNum - 1) % 3 == 0);
		int loopNum = (childrenNum - 1) / 3;
		for (int i = 0; i < loopNum; i++) {
			SimpleNode firstNode = ((SimpleNode) node.children[i * 3]);
			assert (firstNode.getId() == JJTSOME || firstNode.getId() == JJTCOMMA);
			SimpleNode secondNode = ((SimpleNode) node.children[i * 3 + 1]);
			SimpleNode thirdNode = ((SimpleNode) node.children[i * 3 + 2]);
			assert (secondNode.getId() == JJTVAR && thirdNode.getId() == JJTXQ);
			String name = secondNode.getText();
			VariableKeeper xqVar = (VariableKeeper) thirdNode.jjtAccept(this,
					result);
			result.Extend(name, xqVar);
		}
		SimpleNode lastNode = (SimpleNode) node.children[childrenNum - 1];
		assert (lastNode.getId() == JJTCOND);
		Object condResult = lastNode.jjtAccept(this, result);
		if (condResult instanceof XContext) {
			return context;
		} else {
			VariableKeeper removeList = (VariableKeeper) condResult;
			if (removeList != null && removeList.size() > 0
					&& removeList.GetVarNodeList() != null)
				for (VarNode varNode : removeList.GetVarNodeList()) {
					try {
						result.Lookup(varNode.name).RemoveNode(varNode.node);
					} catch (NullPointerException e) {

					}
				}
			XContext finalResult = context.clone();
			for (String varName : result.GetVarNames()) {
				if (result.Lookup(varName) == null
						|| result.Lookup(varName).size() == 0) {
					finalResult.Remove(varName);
				}
			}
			return finalResult;
		}
	}

	public ArrayList<VarNode> Intersect(ArrayList<VarNode> a,
			ArrayList<VarNode> b) {
		ArrayList<VarNode> result = new ArrayList<VarNode>();
		for (VarNode varNode : a) {
			for (VarNode node2 : b) {
				if (a.equals(b)) {
					result.add(varNode.clone());
				}
			}
		}
		return result;
	}

	public ArrayList<VarNode> Union(ArrayList<VarNode> a, ArrayList<VarNode> b) {
		ArrayList<VarNode> result = new ArrayList<VarNode>();
		for (VarNode varNode : a) {
			result.add(varNode.clone());
		}
		for (VarNode varNode : b) {
			boolean addFlag = true;
			for (VarNode compare : a) {
				if (a.equals(b)) {
					addFlag = false;
					break;
				}
			}
			if (addFlag) {
				result.add(varNode.clone());
			}
		}
		return result;
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

	@Override
	public Object visit(AST_Empty node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(AST_Some node, Object data) {
		// TODO Auto-generated method stub
		return null;
	}

}
