package XQuery;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeProcessor implements XQueryParserTreeConstants {

	public DebugLogger log = new DebugLogger("NodeProcessor");

	public NodeProcessor() {

	}

	/*
	 * walk down the RP tree and evaluate it. the reason why I did not use the
	 * visitor method in XQProcessVisitor is that my implementation needs more
	 * parameters provided by XQProcessVisitor visit method.
	 */
	public ArrayList<Object> ProcessRP(AST_RP thisNode, Node domParent,
			int domOperation) {
		log.RegularLog("DomParent Node name:" + domParent.getNodeName());
		// will be returned by this method
		ArrayList<Object> result = null;

		int childrenNum = thisNode.jjtGetNumChildren();
		if (childrenNum == 0) {
			log.ErrorLog("An RP node should not have 0 children!");
			return null;
		}

		// evaluate the left(first) child
		SimpleNode firstRPparam = (SimpleNode) thisNode.children[0];
		int firstChildId = firstRPparam.getId();

		switch (firstChildId) {
		case JJTRP:
			// in this case, the RP is wrapped in "()", so just evaluate
			// the RP inside "()" with the current parent and opeartion
			result = ProcessRP((AST_RP) firstRPparam, domParent, domOperation);
			break;
		case JJTTAGNAME:
			// In this case, search the DOM tree with the opeartion needed.
			String tag = firstRPparam.getText();
			result = (ArrayList<Object>) (domOperation == DomOperations.RP_SIMPLE_FETCH ? DomOperations
					.SimpleSearch(domParent, tag) : DomOperations
					.RecursiveSearch(domParent, tag));
			break;
		case JJTSTAR:
			// In this case, just get all the direct children of current root
			result = DomOperations.getAllChildren(domParent);
			break;
		case JJTDOT:
			// in this case, do nothing
			result = new ArrayList<Object>();
			result.add(domParent);
			break;
		case JJTDDOT:
			// in this case, get parent node as result
			result = new ArrayList<Object>();
			result.add(domParent.getParentNode());
			break;
		case JJTTXT:
			int childrenSize = domParent.getChildNodes().getLength();
			/*
			 * in this case, the result would be String, which is why this
			 * method returns ArrayList<Object> instead of ArrayList<Node> In
			 * order to find out the semantic text node, we check how many
			 * children does a node has, if and only if it has one child which
			 * is a text node, the node is a semantic text node
			 */
			if (childrenSize == 1
					&& domParent.getFirstChild().getNodeType() == Node.TEXT_NODE) {
				// check if the node is a text node, if it is,
				// get the text inside and collect them
				result = new ArrayList<Object>();
				result.add(domParent.getTextContent());
			} else {
				// if the node is not a text, node, the result
				// will be null, so do nothing and return it
				log.RegularLog("inside switch(" + childrenNum + ")"
						+ "switch(JJTTXT)");
			}
			break;
		default:
			log.ErrorLog("inside switch(" + childrenNum + ")" + "switch("
					+ firstChildId + ")");
			break;
		}

		switch (childrenNum) {
		case 1:
			// in this case, all the job need to be done are finished
			// in the previous switch statement
			log.RegularLog("Only one children, finished processing, result size is:"
					+ (result == null ? "0" : result.size()));
			break;
		case 2:
			// in this case, whether to return the result we got from
			// the left child depends on the value of PF
			SimpleNode theOperator = (SimpleNode) thisNode.children[1];
			int operatorNodeId = theOperator.getId();
			// if PF returns false, we should not return anything
			if (operatorNodeId != JJTPF) {
				log.ErrorLog("The node has two children, and the second is not PF!"
						+ " only RP[PF] has two children!");
				result = null;
			} else {
				if (result != null) {
					result = ProcessPF((AST_PF) theOperator, result);
				}
			}
			break;
		case 4:
			/*
			 * when there 4 children, the situation is almost the same as when
			 * there 3 children. So when it is case 4, we do some extra work to
			 * reuse the code in case 3. while case 3 itself will still works
			 * well when it is case 3. Here, theOperator is the PF node
			 */
			theOperator = (SimpleNode) thisNode.children[1];
			if (result != null) {
				result = ProcessPF((AST_PF) theOperator, result);
			}
		case 3:
			SimpleNode secondRPparam = null;
			if (childrenNum != 4) {
				/*
				 * in this case, we will need to evaluate the second RP based on
				 * theOperator, they are in their original position
				 */
				theOperator = (SimpleNode) thisNode.children[1];
				secondRPparam = (SimpleNode) thisNode.children[2];
			} else {
				/*
				 * in this case, we also need to evaluate the second RP based on
				 * theOperator, but their position are offset by the PF node
				 */
				theOperator = (SimpleNode) thisNode.children[2];
				secondRPparam = (SimpleNode) thisNode.children[3];
			}
			operatorNodeId = theOperator.getId();
			// the second child determines the operation between the
			// result from left and right child
			switch (operatorNodeId) {
			case JJTSINGLESLASH:
				// if the type is string, then there is no meaning to do
				// anything
				if (result.size() > 0 && result.get(0) instanceof Node) {
					ArrayList<Object> tmpResult = new ArrayList<Object>();
					/*
					 * for each child of thridChild, evaluate them, get their
					 * result and aggregate the result into tmpResult
					 */
					for (Object n : result) {
						Node m = (Node) n;
						ArrayList<Object> onePassResult = ProcessRP(
								(AST_RP) secondRPparam, m,
								DomOperations.RP_SIMPLE_FETCH);
						if (onePassResult != null)
							tmpResult.addAll(onePassResult);
					}
					// the result at this level should be from those children
					result = tmpResult;
				}
				break;
			case JJTDOUBLESLASH:
				// if the type is string, then there is no meaning to do
				// anything
				if (result != null && result.size() > 0
						&& result.get(0) instanceof Node) {
					ArrayList<Object> tmpResult = new ArrayList<Object>();
					/*
					 * for each child of thridChild, evaluate them, get their
					 * result and aggregate the result into tmpResult. the only
					 * difference from case JJTSINGLESLASH is that the ProcessRP
					 * here use recursive node finding
					 */
					for (Object n : result) {
						Node m = (Node) n;
						ArrayList<Object> recursiveFetchResult = ProcessRP(
								(AST_RP) secondRPparam, m,
								DomOperations.RP_RECURSIVE_FETCH);
						if (recursiveFetchResult != null) {
							tmpResult.addAll(recursiveFetchResult);
						} else {
							log.RegularLog("recursive fetched 0 nodes");
						}

					}
					// the result at this level should be from those children
					result = tmpResult;
				}
				break;
			case JJTCOMMA:
				// in this case, we need to evaluate the right child,
				// concatenate the result with the result from left child
				ArrayList<Object> tmpResult = new ArrayList<Object>();
				// get result from the right child
				tmpResult = ProcessRP((AST_RP) secondRPparam, domParent,
						domOperation);
				// check the type of the left and right result
				if (tmpResult.size() > 0 && result.size() > 0) {
					Object typeTester1 = tmpResult.get(0);
					Object typeTester2 = result.get(0);
					// both of them are String
					boolean check1 = typeTester1 instanceof String
							&& typeTester2 instanceof String;
					// both are Node
					boolean check2 = typeTester1 instanceof Node
							&& typeTester2 instanceof Node;
					// at least one should hold
					if (!(check1 || check2)) {
						log.ErrorLog("The type of the to be concatinated list should be the"
								+ " same and should be only String or Node type! in JJCOMMA");
						return null;
					}
				}
				// if the type check passes, concatenate the results. unique
				// operation
				// will be done at the end of this method
				result.addAll(tmpResult);
				break;

			default:
				log.ErrorLog("In RP with 3 children, the second child is not expected type!");
				result = null;
				break;
			}
			break;
		default:
			log.ErrorLog("inside switch(" + childrenNum + ")");
			break;
		}
		// execute unique on the result
		result = unique(result);
		return result;
	}

	public ArrayList<Object> unique(ArrayList<Object> list) {
		ArrayList<Object> result = list;
		/*
		 * this code block execute unique operation on the result, since the
		 * type of the result can be Node or String, we cannot simply call
		 * doUniqueOnNodeList. First, check the size and type of list
		 */
		if (list != null && list.size() > 1) {
			if (list.get(0) instanceof Node) {
				log.DebugLog("list size is:" + list.size());
				/*
				 * convert the Object list into Node list and call
				 * doUniqueOnNodeList
				 */
				List<Node> newresult = new ArrayList<Node>();
				for (Object o : list) {
					newresult.add((Node) o);
				}
				newresult = doUniqueOnNodeList(newresult);
				// convert the Node list back to Object list in order to return
				result = new ArrayList<Object>();
				for (Node n : newresult) {
					result.add(n);
				}
			} else {
				/*
				 * if the type is String, we should do nothing. so assign list
				 * directly to result. But there should only be two types of
				 * elements in the result list
				 */
				result = list;
				assert list.get(0) instanceof String;
			}
		}
		return result;
	}

	private List<Node> doUniqueOnNodeList(List<org.w3c.dom.Node> list) {
		List<Node> result = new ArrayList<Node>();
		for (int i = 0; i < list.size(); i++) {
			boolean duplicateFlag = false;
			for (int j = i + 1; j < list.size(); j++) {
				if (list.get(i).isSameNode(list.get(j))) {
					duplicateFlag = true;
					break;
				}
			}
			if (!duplicateFlag) {
				result.add(list.get(i));
			}
		}
		return result;
	}

	public ArrayList<Object> ProcessPF(AST_PF thisNode,
			ArrayList<Object> domParents) {
		if (domParents == null || domParents.size() == 0) {
			return domParents;
		}
		ArrayList<Object> result = new ArrayList<Object>();
		int childrenNum = thisNode.jjtGetNumChildren();

		switch (childrenNum) {
		case 1:
			SimpleNode firstchild = (SimpleNode) thisNode.children[0];
			int childId = firstchild.getId();
			switch (childId) {
			case JJTRP:
				/*
				 * for each node in domParents, if evaluated result from RP is
				 * true, add it to afterPfEvaluation. The following code
				 * automatically handles String result list
				 */
				for (Object o : domParents) {
					if (o instanceof Node) {
						ArrayList<Object> rpResult = ProcessRP(
								(AST_RP) firstchild, (Node) o,
								DomOperations.RP_SIMPLE_FETCH);
						if (rpResult != null && rpResult.size() > 0) {
							result.add(o);
						}
					}
				}
				break;
			case JJTPF:
				result = ProcessPF((AST_PF) firstchild, domParents);
				break;
			default:
				log.ErrorLog("In ProcessPF, with " + childrenNum
						+ " children. Encountered child type:"
						+ XQueryParserTreeConstants.jjtNodeName[childId]
						+ " which should not happen.");
				break;
			}
			break;
		case 2:
			firstchild = (SimpleNode) thisNode.children[0];
			SimpleNode secondchild = (SimpleNode) thisNode.children[1];
			int firstchildId = firstchild.getId();
			int secondchildId = secondchild.getId();

			// error handling
			if (firstchildId != JJTNOT || secondchildId != JJTPF) {
				log.ErrorLog("In ProcessPF, with " + childrenNum
						+ " children. Encountered child type:"
						+ XQueryParserTreeConstants.jjtNodeName[firstchildId]
						+ " and "
						+ XQueryParserTreeConstants.jjtNodeName[secondchildId]
						+ " which should not happen.");
			} else {
				// get elements ignoring not
				ArrayList<Object> pfResult = ProcessPF((AST_PF) secondchild,
						domParents);
				for (Object o : domParents) {
					result.add(o);
				}
				// subtract those elements
				result.removeAll(pfResult);
			}
			break;
		case 3:
			firstchild = (SimpleNode) thisNode.children[0];
			secondchild = (SimpleNode) thisNode.children[1];
			SimpleNode thirdchild = (SimpleNode) thisNode.children[2];
			secondchildId = secondchild.getId();

			switch (secondchildId) {
			// handling these two are very familiar, for coding reusing purpose
			// here we put the case statement together
			case JJTEQ:
			case JJTIS:
				firstchildId = firstchild.getId();
				int thirdchildId = thirdchild.getId();
				// error handling
				if (!(firstchildId == JJTRP && thirdchildId == JJTRP)) {
					log.ErrorLog("In ProcessPF, with "
							+ childrenNum
							+ " children and (JJTEQ or JJTIS). Encountered child type:"
							+ XQueryParserTreeConstants.jjtNodeName[firstchildId]
							+ " and "
							+ XQueryParserTreeConstants.jjtNodeName[thirdchildId]
							+ " which should not happen.");
				} else {
					for (Object o : domParents) {
						if (o instanceof Node) {
							// evaluate the left RP
							ArrayList<Object> leftRpList = ProcessRP(
									(AST_RP) firstchild, (Node) o,
									DomOperations.RP_SIMPLE_FETCH);
							// evaluate the right RP
							ArrayList<Object> rightRpList = ProcessRP(
									(AST_RP) thirdchild, (Node) o,
									DomOperations.RP_SIMPLE_FETCH);
							// evaluate according to the operator
							boolean flag = (secondchildId == JJTEQ ? CheckListEQWrapper(
									leftRpList, rightRpList)
									: CheckListISWrapper(leftRpList,
											rightRpList));
							if (flag) {
								result.add(o);
							}
						}
					}
				}
				break;
			// handling these two are very familiar, for coding reusing purpose
			// here we put the case statement together
			case JJTAND:
			case JJTOR:
				firstchildId = firstchild.getId();
				thirdchildId = thirdchild.getId();
				// error handling
				if (!(firstchildId == JJTRP && thirdchildId == JJTPF)) {
					log.ErrorLog("In ProcessPF, with "
							+ childrenNum
							+ " children and (JJTEQ or JJTIS). Encountered child type:"
							+ XQueryParserTreeConstants.jjtNodeName[firstchildId]
							+ " and "
							+ XQueryParserTreeConstants.jjtNodeName[thirdchildId]
							+ " which should not happen.");
				} else {
					for (Object o : domParents) {
						if (o instanceof Node) {
							/*
							 * evaluate the left RP. Notice that, although the
							 * production says "PF -> PF and PF", the left child
							 * here should be RP. After eliminating left
							 * recursion, the end node will directly appear
							 * here, which in this case is RP.
							 */
							ArrayList<Object> leftRpList = ProcessRP(
									(AST_RP) firstchild, (Node) o,
									DomOperations.RP_SIMPLE_FETCH);

							// here, we should evaluate the right PF based on o
							// from domParents
							ArrayList<Object> tmpParam = new ArrayList<Object>();
							tmpParam.add(o);
							// evaluate the right PF
							ArrayList<Object> rightRpList = ProcessPF(
									(AST_PF) thirdchild, tmpParam);

							// evaluate left result
							boolean leftFlag = false;
							if (leftRpList != null && leftRpList.size() > 0) {
								leftFlag = true;
							}
							// evaluate right result
							boolean rightFlag = false;
							if (rightRpList != null && rightRpList.size() > 0) {
								rightFlag = true;
							}
							// evaluate according to the operator
							boolean flag = (secondchildId == JJTAND ? (leftFlag & rightFlag)
									: (leftFlag | rightFlag));
							if (flag) {
								result.add(o);
							}
						}
					}
				}

				break;
			default:
				log.ErrorLog("In ProcessPF, with " + childrenNum
						+ " children. Encountered thirdchild type:"
						+ XQueryParserTreeConstants.jjtNodeName[secondchildId]
						+ " which should not happen.");
				break;
			}

			break;
		default:
			log.ErrorLog("In ProcessPF, encountered childrenNum:" + childrenNum
					+ "which should not happen.");
			break;
		}
		return result;
	}

	private boolean CheckListISWrapper(ArrayList<Object> listA,
			ArrayList<Object> listB) {
		// TODO: add implementation of IS operation!
		return true;
	}

	// TODO: here we need to clarify the semantics of of EQ!!!!
	private boolean CheckListEQWrapper(ArrayList<Object> listA,
			ArrayList<Object> listB) {
		// Warning: here we treat two nulls as equal
		if (listA == null || listB == null) {
			// Warning: here we treat two nulls as equal
			if (listA == null && listB == null)
				return true;
			else
				return false;
		}
		if (!(listA.size() == listB.size())) {
			return false;
		}
		for (int i = 0; i < listA.size(); i++) {
			Object A = listA.get(i);
			Object B = listB.get(i);
			/*
			 * only support two node list, if the lists are String list, default
			 * false
			 */
			if (!(A instanceof Node && B instanceof Node)) {
				return false;
			}
			if (!CheckEQ((Node) A, (Node) B)) {
				return false;
			}
		}
		return true;
	}

	private boolean CheckEQ(Node a, Node b) {
		if (a == null || b == null) {
			// Warning: here we treat two nulls as equal
			if (a == null && b == null)
				return true;
			else
				return false;
		}
		if (!a.getNodeName().equals(b.getNodeName())) {
			return false;
		}
		if (!(a.getChildNodes().getLength() == b.getChildNodes().getLength())) {
			return false;
		}
		NodeList alist = a.getChildNodes();
		NodeList blist = b.getChildNodes();
		for (int i = 0; i < alist.getLength(); i++) {
			if (!CheckEQ(alist.item(i), blist.item(i))) {
				return false;
			}
		}
		return true;

	}
}
