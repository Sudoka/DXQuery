package Optimizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import XQuery.AST_COND;
import XQuery.AST_XQ;
import XQuery.DebugLogger;
import XQuery.SimpleNode;
import XQuery.XContext;
import XQuery.XQueryParserTreeConstants;

public class OptimizeNodeProcessor implements XQueryParserTreeConstants {

	public DebugLogger log = new DebugLogger("OptimizeNodeProcessor");

	/**
	 * Warning: this method has side effects on context.baseCondNodes
	 * 
	 * @param context
	 * @param condNode
	 */
	public void condGetOptimizeRecords(XContext context, AST_COND condNode) {
		if (context.baseCondNodes == null) {
			context.baseCondNodes = new ArrayList<AbstractBaseCondNode>();
		}
		condRecursiveGetOptimizeRecords(context, condNode);
		int splitNodeNum = 0;
		SplitNode spN = null;
		for (AbstractBaseCondNode node : context.baseCondNodes) {
			if (node.nodeType == AbstractBaseCondNode.SPLITNODE) {
				splitNodeNum++;
				spN = (SplitNode) node;
			}
		}
		if (splitNodeNum == 1) {
			Set<String> partition1 = context.getAncestorNameSet(spN
					.getBaseBinding().get(0));
			Set<OperateNode> operateNodes1 = getOperateNodesAndExpandPartition(
					context, partition1);

			Set<String> partition2 = context.getAncestorNameSet(spN
					.getOtherBinding().get(0));
			Set<OperateNode> operateNodes2 = getOperateNodesAndExpandPartition(
					context, partition2);

			// check if partition succeeded
			Set<String> tmpSet = new HashSet<String>();
			tmpSet.addAll(partition2);
			int size2 = tmpSet.size();
			assert (size2 == partition2.size());
			tmpSet.addAll(partition1);
			if (tmpSet.size() < partition1.size() + size2) {
				log.ErrorLog("Partition failed!");
				return;
			}

			String part1 = "Partition1:\n";
			for (String string : partition1) {
				part1 += (string + "\n");
			}
			log.DebugLog(part1);

			String part2 = "Partition2:\n";
			for (String string : partition2) {
				part2 += (string + "\n");
			}
			log.DebugLog(part2);

		} else {
			log.ErrorLog("Has not implement multiple attribute join yet!!!!!!");
		}
	}

	/**
	 * Warning: this method has side effect on partition!!!
	 * 
	 * @param context
	 * @param nodeList
	 * @param partition
	 * @return
	 */
	private Set<OperateNode> getOperateNodesAndExpandPartition(
			XContext context, Set<String> partition) {
		Set<OperateNode> result = new HashSet<OperateNode>();
		for (AbstractBaseCondNode node : context.baseCondNodes) {
			if (node.nodeType == AbstractBaseCondNode.OPERATENODE) {
				Set<String> ancestorSet = context.getAncestorNameSet(node
						.getBaseBinding().get(0));
				int size = ancestorSet.size();
				ancestorSet.addAll(partition);
				// this means this operator node has common ancestor with
				// current split node partition
				if (ancestorSet.size() < partition.size() + size) {
					partition.add(node.getBaseBinding().get(0));
					result.add((OperateNode) node);
				}
			}
		}
		return result;
	}

	private void condRecursiveGetOptimizeRecords(XContext context, AST_COND node) {
		int childrenNum = node.jjtGetNumChildren();
		AbstractBaseCondNode leftNode = null;
		switch (childrenNum) {
		// production: Cond => XQ eq XQ
		case 3:
			// production: Cond => XQ eq XQ and Cond
		case 5:
			SimpleNode firstChild = (SimpleNode) node.jjtGetChild(0);
			int firstChildId = firstChild.getId();
			assert (firstChildId == JJTXQ);
			SimpleNode secondChild = (SimpleNode) node.jjtGetChild(1);
			int secondChildId = secondChild.getId();
			assert (secondChildId == JJTEQ);
			SimpleNode thirdChild = (SimpleNode) node.jjtGetChild(2);
			int thirdChildId = thirdChild.getId();
			assert (thirdChildId == JJTXQ);
			leftNode = buildNode((AST_XQ) firstChild, (AST_XQ) thirdChild);
			context.baseCondNodes.add(leftNode);
			if (childrenNum == 3) {
				return;
			}
			SimpleNode fourthChild = (SimpleNode) node.jjtGetChild(3);
			int fourthChildId = fourthChild.getId();
			assert (fourthChildId == JJTAND);
			SimpleNode fifthChild = (SimpleNode) node.jjtGetChild(4);
			int fifthChildId = fifthChild.getId();
			assert (fifthChildId == JJTCOND);
			condRecursiveGetOptimizeRecords(context, (AST_COND) fifthChild);
			break;
		default:
			log.ErrorLog(" in condRecursiveGetOptimizeRecords\n "
					+ "Encountered unexpected children number!\n"
					+ "childrenNum:" + childrenNum);
			break;
		}
	}

	private AbstractBaseCondNode buildNode(AST_XQ xq1, AST_XQ xq2) {
		// these two nodes are either XQ->Var or XQ->StringConstant
		assert (xq1.jjtGetNumChildren() == 1 && xq2.jjtGetNumChildren() == 1);

		SimpleNode terminateNode1 = (SimpleNode) xq1.jjtGetChild(0);
		int terminateNode1Type = terminateNode1.getId();

		SimpleNode terminateNode2 = (SimpleNode) xq2.jjtGetChild(0);
		int terminateNode2Type = terminateNode2.getId();

		AbstractBaseCondNode node = null;
		if (terminateNode1Type == JJTVAR) {
			if (terminateNode2Type == JJTVAR) {
				// instantiate as split node
				node = new SplitNode();
				node.nodeType = AbstractBaseCondNode.SPLITNODE;

				// set base binding
				ArrayList<String> binding1 = new ArrayList<String>();
				binding1.add(terminateNode1.getText());
				node.setBaseBinding(binding1);

				// set other binding
				ArrayList<String> binding2 = new ArrayList<String>();
				binding2.add(terminateNode2.getText());
				((SplitNode) node).setOtherBinding(binding2);
			} else if (terminateNode2Type == JJTSTRING) {
				// instantiate as operate node
				node = new OperateNode();
				node.nodeType = AbstractBaseCondNode.OPERATENODE;
				// set base binding
				ArrayList<String> binding = new ArrayList<String>();
				binding.add(terminateNode1.getText());
				node.setBaseBinding(binding);

				// set string constant
				String strConstant = new String(terminateNode2.getText());
				((OperateNode) node).setStringConstant(strConstant);
			} else {
				log.ErrorLog(" in buildNode(AST_XQ xq1, AST_XQ xq2)\n "
						+ "Encountered unexpected node type!\n" + "xq1:"
						+ jjtNodeName[terminateNode1Type] + "\n" + "xq2:"
						+ jjtNodeName[terminateNode2Type]);
			}
		} else if (terminateNode1Type == JJTSTRING) {
			if (terminateNode2Type == JJTVAR) {
				// instantiate as operate node
				node = new OperateNode();
				node.nodeType = AbstractBaseCondNode.OPERATENODE;

				// set string constant
				String strConstant = new String(terminateNode1.getText());
				((OperateNode) node).setStringConstant(strConstant);

				// set base binding
				ArrayList<String> binding = new ArrayList<String>();
				binding.add(terminateNode2.getText());
				node.setBaseBinding(binding);
			} else {
				log.ErrorLog(" in buildNode(AST_XQ xq1, AST_XQ xq2)\n "
						+ "Encountered unexpected node type!\n" + "xq1:"
						+ jjtNodeName[terminateNode1Type] + "\n" + "xq2:"
						+ jjtNodeName[terminateNode2Type]);
			}
		} else {
			log.ErrorLog(" in buildNode(AST_XQ xq1, AST_XQ xq2)\n "
					+ "Encountered unexpected node type!\n" + "xq1:"
					+ jjtNodeName[terminateNode1Type] + "\n" + "xq2:"
					+ jjtNodeName[terminateNode2Type]);
		}

		return node;
	}

	// private void xqGetOptimizeRecords(XContext context, AST_XQ node) {
	//
	// }
}
