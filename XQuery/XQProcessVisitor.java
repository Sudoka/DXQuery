/**
 * 
 */
package XQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author QDX
 * 
 */
public class XQProcessVisitor implements XQueryParserVisitor,
		XQueryParserTreeConstants {

	public DebugLogger log;
	// used to process RP and PF node
	public NodeProcessor processor;

	public Node root;
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

		// for (Node n : result.GetNodes()) {
		//
		// log.DebugLog("Node:" + n.getNodeName());
		// if (n.getNodeType() == Node.TEXT_NODE) {
		// log.DebugLog("text node value:" + n.getNodeValue());
		// }
		// try {
		// System.out.println(DOMPrinter.printXML(n));
		// } catch (IOException e) {
		// log.ErrorLog("Some kind of parse error caused by DOMPrinter");
		// e.printStackTrace();
		// }
		// }
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
			log.DebugLog(jjtNodeName[((SimpleNode) n).getId()]);
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
		// I have already handled all the situations, so no need to call
		// childrenAccept
		// node.childrenAccept(this, data);
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
		// this procedure is replaced by method NodeProcess
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.AST_PF, java.lang.Object)
	 */
	@Override
	public Object visit(AST_PF node, Object data) {
		log.RegularLog("Visit: ASP_PF" + " <" + node.jjtGetNumChildren() + ">");
		// this procedure is replaced by method NodeProcess
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see XQuery.XQueryParserVisitor#visit(XQuery.AST_XQ, java.lang.Object)
	 */
	@Override
	public Object visit(AST_XQ node, Object data) {
		log.RegularLog("Visit: AST_XQ" + " <" + node.jjtGetNumChildren() + ">");
		//
		assert (data instanceof XContext);
		// To be returned
		VariableKeeper result = new VariableKeeper();
		// used as parameter for future AST nodes
		XContext firstContext = null;

		int childrenNum = node.jjtGetNumChildren();
		assert (childrenNum > 0);

		SimpleNode firstChild = (SimpleNode) node.children[0];
		int firstChildId = firstChild.getId();

		switch (firstChildId) {
		// AP be evaluated immediately
		case JJTAP:
			// appearance of AP at the first child implies this XQ only has one
			// node
			assert (childrenNum == 1);
			@SuppressWarnings("unchecked")
			ArrayList<Object> apResult = (ArrayList<Object>) ((AST_AP) firstChild)
					.jjtAccept(this, data);
			result.InitializeWithNodeList(apResult);
			return result;
		case JJTTAGNAME:
			// get the start and end tags
			String tag1 = firstChild.getText();
			String tag2 = node.children[2].getText();
			assert (tag1.equals(tag2));

			// Evaluate XQ in middle of tag pair
			AST_XQ secondChild = (AST_XQ) node.children[1];
			VariableKeeper tmpXQ = (VariableKeeper) secondChild.jjtAccept(this,
					data);
			// used to create new node
			DocumentImpl docNew = new DocumentImpl();
			Element newRoot = docNew.createElement(tag1);
			for (Node n : tmpXQ.GetNodes()) {
				// A tricky step
				newRoot.appendChild(docNew.importNode(n, true));
			}
			// a singleton list containing the evaluated result wrapped in tag
			ArrayList<Object> tmpAdd = new ArrayList<Object>();
			tmpAdd.add(newRoot);
			result.InitializeWithNodeList(tmpAdd);
			if (childrenNum == 3) {
				return result;
			}
			break;
		// appearance of Var in the first child does not imply much on the
		// production rule
		case JJTVAR:
			String name = ((AST_VAR) firstChild).getText();
			// lookup in the context and find out the current binding
			VariableKeeper tmpResult = ((XContext) data).Lookup(name);
			if (!(tmpResult == null)) {
				result = tmpResult;
			}
			log.RegularLog("case JJTVAR and binds to "
					+ (result == null ? null : result.size()) + " nodes");
			break;
		case JJTSTRING:
			String string = firstChild.getText();
			// get rid of \" and \" at the beginning and end of the String
			// constant
			string = new String(string.substring(1, string.length() - 1));
			docNew = new DocumentImpl();
			Node newTextNode = docNew.createTextNode(string);
			// a singleton list to initialize result
			tmpAdd = new ArrayList<Object>();
			tmpAdd.add(newTextNode);
			result.InitializeWithNodeList(tmpAdd);
			break;
		case JJTXQ:
			// record the value returned by this XQ
			result = (VariableKeeper) firstChild.jjtAccept(this, data);
			break;
		case JJTFORCLAUSE:
			assert (childrenNum >= 2);
			// get new context from for clause, we will pass it to let where and
			// return clause after this. for clause only changes context var
			// binding
			firstContext = (XContext) firstChild.jjtAccept(this, data);
			break;
		case JJTLETCLAUSE:
			assert (childrenNum == 2);
			// get new context from let clause, we will pass it to later clauses
			firstContext = (XContext) firstChild.jjtAccept(this, data);
			break;
		default:
			log.ErrorLog("Encountered unexpected first child:["
					+ jjtNodeName[firstChildId] + "]\n with children number:<"
					+ childrenNum + "> !");
			break;
		}
		// this means, there is only one node and we have done all the work we
		// should do, now return the result we got
		if (childrenNum == 1) {
			return result;
		}

		switch (childrenNum) {
		// <Let XQ> or <For Return>, either case we have finished the
		// evaluation of this XQ
		case 2:
			SimpleNode secondChild = (SimpleNode) node.children[1];
			int secondChildId = secondChild.getId();
			switch (secondChildId) {
			/**
			 * in this case, the previous node is a for clause, it updates the
			 * context, here we pass the context to the return clause to
			 * evaluate and give the final result
			 */
			case JJTRETURNCLAUSE:
				return secondChild.jjtAccept(this, firstContext);
				/**
				 * in this case, the previous node is a let clause, which is
				 * very similiar with for clause. we do exactly the same thing
				 * as for clause
				 */
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
			/**
			 * here the production rule is either XQ/RP or XQ//RP
			 */
			case JJTSINGLESLASH:
			case JJTDOUBLESLASH:
				assert (thirdChildId == JJTRP);
				int opeartion = (secondChildId == JJTSINGLESLASH ? DomOperations.RP_SIMPLE_FETCH
						: DomOperations.RP_RECURSIVE_FETCH);
				VariableKeeper tmpResult = ((AST_RP) thirdChild)
						.EvaluateRPUnderVariable(result, (AST_RP) thirdChild,
								opeartion);
				return tmpResult;
			case JJTCOMMA:
				assert (thirdChildId == JJTXQ);
				/**
				 * with comma, the xq on the left of comma and right of comma
				 * should be evaluated under the same context, thus pass data as
				 * parameter. Also, with comma, the production should be XQ,XQ
				 * return the concatenated result
				 */
				VariableKeeper result2 = (VariableKeeper) thirdChild.jjtAccept(
						this, data);
				VariableKeeper finalResult = result.CreateByMerge(result2);
				return finalResult;
			case JJTLETCLAUSE:
				/**
				 * in this case, we got the production rule For Let Return
				 */
				assert (firstChildId == JJTFORCLAUSE && thirdChildId == JJTRETURNCLAUSE);
				XContext secondContext = (XContext) secondChild.jjtAccept(this,
						firstContext);
				return thirdChild.jjtAccept(this, secondContext);
			case JJTWHERECLAUSE:
				/**
				 * in this case, we got the production rule For Where Return
				 */
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
			/**
			 * here we have For Let Where Return
			 */
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
			/**
			 * Here we have have <tag>{XQ}</tag>,XQ
			 */
			fourthChild = (SimpleNode) node.children[3];
			fourthChildId = fourthChild.getId();
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
	 * For clause and Let clause has exactly the same structure, so here we can
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
		// If assertion failure occurs here, the children
		// number of for clause has problem!
		assert ((node.jjtGetNumChildren() + 1) % 3 == 0);
		// we will need to make varNum amount of variable bindings
		int varNum = (node.jjtGetNumChildren() + 1) / 3;
		for (int i = 0; i < varNum; i++) {
			SimpleNode nameNode = (SimpleNode) node.children[i * 3];
			SimpleNode xqNode = (SimpleNode) node.children[i * 3 + 1];
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
		// TODO: add more code here to remove the unsatisfied nodes,
		// need more work to implement the correct behavior
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
		assert (childrenNum != 0);

		SimpleNode firstChild = (SimpleNode) node.children[0];
		int firstChildId = firstChild.getId();

		// two return types
		VariableKeeper keepList = new VariableKeeper();
		XContext keepContext = new XContext();

		// indicator of what type of result to return
		int keepFlag = 0;
		switch (firstChildId) {
		case JJTCOND:
			// (Cond)
			Object keepOb = firstChild.jjtAccept(this, newContext);
			if (childrenNum == 1)
				return keepOb;
		case JJTXQ:
			/**
			 * XQ == XQ ; XQ = XQ ; XQ is XQ ; XQ eq XQ followed by And or Or
			 */
			assert (childrenNum >= 3);
			// ((XContext) data).DebugPrintAllBingdings();
			VariableKeeper xqResult1 = (VariableKeeper) firstChild.jjtAccept(
					this, data);

			SimpleNode secondChild = (SimpleNode) node.children[1];
			int secondChildId = secondChild.getId();

			SimpleNode thirdChild = (SimpleNode) node.children[2];
			int thirdChildId = thirdChild.getId();

			assert (thirdChildId == JJTXQ);
			// TODO: Work on this part to implement the correct operation
			// behavior!
			switch (secondChildId) {
			case JJTEQ:
			case JJTIS:
				VariableKeeper xqResult2 = (VariableKeeper) thirdChild
						.jjtAccept(this, data);

				// int operation = (secondChildId == JJTEQ ?
				// VariableKeeper.EQ_DISJOINT
				// : VariableKeeper.IS_DISJOINT);
				// we may need to decide ID equal to use or Value equal to use
				// in the Intersect operation
				keepList = xqResult1.Intersect(xqResult1, xqResult2);
				// System.out.println("-------------------------");
				// removeList.PrintAllVars();
				keepFlag = REMOVE_VARKEEPER;
				break;
			default:
				log.ErrorLog("Evaluating Cond, encountered unexpected second child:"
						+ jjtNodeName[secondChildId]
						+ " after XQ as the first child.");
				break;
			}
			if (childrenNum == 3) {
				return keepList;
			}
			break;
		case JJTEMPTY:
			secondChild = (SimpleNode) node.children[1];
			secondChildId = secondChild.getId();
			assert (secondChildId == JJTXQ);
			VariableKeeper xqResult = (VariableKeeper) secondChild.jjtAccept(
					this, data);
			// TODO: may need more work
			// the XQ result is empty, so no nodes get filter, return an empty
			// list
			if (xqResult == null || xqResult.size() == 0) {
				// the XQ result is empty, thus the empty clause does not
				// stand, no variables should be kept, return a context for that
				keepContext = (XContext) data;
				keepFlag = REMOVE_CONTEXT;
				if (childrenNum == 2)
					return (XContext) data;
			} else {
				keepList = new VariableKeeper();
				keepFlag = REMOVE_VARKEEPER;
				if (childrenNum == 2) {
					return keepList;
				}
			}
			break;
		case JJTSOME:
			XContext someResult = null;
			boolean canReturn = false;
			keepFlag = REMOVE_VARKEEPER;
			/**
			 * production: some var in xq and cond evaluate some with correct
			 * number of children
			 */
			if (((SimpleNode) node.children[childrenNum - 2]).getId() == JJTAND
					|| ((SimpleNode) node.children[childrenNum - 2]).getId() == JJTOR) {
				// generate a new root AST node in order to evaluate some clause
				AST_COND tmp = new AST_COND(JJTCOND);
				for (int i = 0; i < childrenNum - 2; i++) {
					tmp.jjtAddChild(node.children[i], i);
				}
				// go to EvaluateSome do something
				someResult = EvaluateSome(tmp, (XContext) data);
			} else {
				// means there are nothing more following some clause, we can
				// return
				canReturn = true;
				someResult = EvaluateSome(node, (XContext) data);
			}
			keepFlag = REMOVE_CONTEXT;
			keepContext = someResult;
			if (canReturn)
				return keepContext;
			break;
		case JJTNOT:
			secondChild = (SimpleNode) node.children[1];
			secondChildId = secondChild.getId();
			assert (secondChildId == JJTCOND);
			keepOb = secondChild.jjtAccept(this, newContext);
			XContext notContext = newContext.clone();
			// reverse what we have from cond, if we filtered everything, there,
			// we return an empty list indicating that nothing should be
			// filtered
			keepFlag = REMOVE_CONTEXT;
			if (keepOb instanceof XContext) {
				notContext.Subtract(((XContext) keepOb));
			} else {
				notContext.Subtract(((VariableKeeper) keepOb));
			}
			if (childrenNum == 2) {
				return notContext;
			}
			break;
		default:
			log.ErrorLog("Encoumtered unxepected first child:"
					+ jjtNodeName[firstChildId]);
			break;
		}

		/**
		 * at this point, we know that the list may be very long, since "some"
		 * clause may brings a lot of nodes into the children list. But one
		 * thing is sure that, for all the situations not handled till this
		 * point, they are combined by AND or OR operation. So we evaluate the
		 * result of the nodes according to that.
		 */
		SimpleNode operatorNode = (SimpleNode) node.children[childrenNum - 2];
		int operatorId = operatorNode.getId();
		assert (operatorId == JJTAND || operatorId == JJTOR);
		Object removeOb = node.children[childrenNum - 1].jjtAccept(this,
				newContext);
		// TODO: Start from here!!!!
		/**
		 * here since both of cond must be satisfied, we have to remove both
		 * cond1 and cond2
		 */
		if (operatorId == JJTAND) {
			// make union and return
			if (keepFlag == REMOVE_CONTEXT) {
				return keepContext;
			} else if (keepFlag == REMOVE_VARKEEPER) {
				if (removeOb instanceof XContext) {
					return (XContext) removeOb;
				} else {
					return keepList.CreateByMerge((VariableKeeper) removeOb);
				}
			} else {
				log.ErrorLog("Remove Flag has unexpected value!!!");
			}
		} else {
			/**
			 * since it is or opeartor, we only need to remove the intersect of
			 * result from cond1 and cond2
			 */
			if (keepFlag == REMOVE_CONTEXT) {
				return removeOb;
			} else {
				if (removeOb instanceof XContext) {
					return keepList;
				} else {
					return keepList.Intersect((VariableKeeper) removeOb);
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

		// some clause ends with a cond
		SimpleNode lastNode = (SimpleNode) node.children[childrenNum - 1];
		assert (lastNode.getId() == JJTCOND);
		Object condResult = lastNode.jjtAccept(this, result);

		if (condResult instanceof XContext) {
			return context;
		} else {
			Set<String> keepVarName = new HashSet<String>();
			VariableKeeper keepList = (VariableKeeper) condResult;
			if (keepList != null && keepList.size() > 0
					&& keepList.GetVarNodeList() != null) {
				for (VarNode varNode : keepList.GetVarNodeList()) {
					try {
						keepVarName.add(varNode.name);
					} catch (NullPointerException e) {
						log.ErrorLog("about line 745");
					}
				}
			}
			XContext finalResult = context.clone();
			for (String varName : result.GetVarNames()) {
				if (!keepVarName.contains(varName)) {
					keepVarName.remove(varName);
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
				if (varNode.equals(node2)) {
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
				if (varNode.equals(compare)) {
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
