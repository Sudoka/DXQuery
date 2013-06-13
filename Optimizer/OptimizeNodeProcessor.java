package Optimizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.sun.xml.internal.bind.v2.TODO;

import XQuery.AST_COND;
import XQuery.AST_FORCLAUSE;
import XQuery.AST_RETURNCLAUSE;
import XQuery.AST_XQ;
import XQuery.DebugLogger;
import XQuery.SimpleNode;
import XQuery.VariableKeeper;
import XQuery.XContext;
import XQuery.XQProcessVisitor;
import XQuery.XQueryParserTreeConstants;
import XQuery.XQueryParserVisitor;

public class OptimizeNodeProcessor implements XQueryParserTreeConstants {

	public DebugLogger log = new DebugLogger("OptimizeNodeProcessor");
	private ArrayList<SplitNode> splitNodeList = new ArrayList<SplitNode>();

	/**
	 * Warning: this method has side effects on context.baseCondNodes
	 * 
	 * @param context
	 * @param condNode
	 */
	public ArrayList<Partition> condGetOptimizeRecords(XContext context,
			AST_COND condNode) {
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
			splitNodeList.add(spN);
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
				return null;
			}

			// String part1 = "Partition1:\n";
			// for (String string : partition1) {
			// part1 += (string + "\n");
			// }
			// log.DebugLog(part1);
			//
			// String part2 = "Partition2:\n";
			// for (String string : partition2) {
			// part2 += (string + "\n");
			// }
			// log.DebugLog(part2);
			ArrayList<Partition> result = new ArrayList<Partition>();
			result.add(new Partition(partition1, operateNodes1));
			result.add(new Partition(partition2, operateNodes2));
			return result;

		} else {
			log.ErrorLog("Has not implement multiple attribute join yet!!!!!!");
			return null;
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

	public String joinRewrite(AST_FORCLAUSE forNode,
			ArrayList<Partition> partitions, XQProcessVisitor visitor,
			AST_RETURNCLAUSE returnXq) {

		String result = "";
		AST_XQ xqNodeFinal = (AST_XQ) returnXq.jjtGetChild(0);
		visitor.noNestedXQRewrite = true;
		String xqResultStr = (String) visitor.visit(xqNodeFinal, null);
		visitor.noNestedXQRewrite = false;
		log.DebugLog(xqResultStr);

		if (partitions.size() == 2) {
			String part1 = "for";
			String return1 = "return ";
			boolean firstFlag1 = true;
			Partition partition1 = partitions.get(0);
			String part2 = "for";
			String return2 = "return ";
			boolean firstFlag2 = true;
			Partition partition2 = partitions.get(1);

			assert ((forNode.jjtGetNumChildren() + 1) % 3 == 0);
			// we will need to make varNum amount of variable bindings
			int varNum = (forNode.jjtGetNumChildren() + 1) / 3;
			for (int i = 0; i < varNum; i++) {
				SimpleNode nameNode = (SimpleNode) forNode.jjtGetChild(i * 3);

				SimpleNode xqNode = (SimpleNode) forNode.jjtGetChild(i * 3 + 1);
				assert (xqNode.getId() == JJTXQ && nameNode.getId() == JJTVAR);

				String varName = nameNode.getText();
				visitor.noNestedXQRewrite = true;
				String xqStr = (String) visitor.visit((AST_XQ) xqNode, null);

				if (partition1.varNameSet.contains(varName)) {
					String prefix = "";
					if (firstFlag1) {
						prefix = " ";
						firstFlag1 = false;
					} else {
						prefix = ",\n";
					}
					part1 += (prefix + varName + " in " + xqStr);
				} else if (partition2.varNameSet.contains(varName)) {
					String prefix = "";
					if (firstFlag2) {
						prefix = " ";
						firstFlag2 = false;
					} else {
						prefix = ",\n";
					}
					part2 += (prefix + varName + " in " + xqStr);
				} else {
					log.ErrorLog("VarName not found in any partition!!");
					return null;
				}
			}
			visitor.noNestedXQRewrite = false;
			part1 = AppendWhereText(part1, partition1);
			part2 = AppendWhereText(part2, partition2);

			return1 = GetInnerReturnText(partition1);
			return2 = GetInnerReturnText(partition2);

			part1 += ("\n" + return1);
			part2 += ("\n" + return2);

			// log.DebugLog(part1);
			// log.DebugLog(part2);

			// I only implemented condition where split node amount is one
			assert (splitNodeList.size() == 1);

			SplitNode splitNode = splitNodeList.get(0);
			String part3 = "[" + splitNode.getBaseBinding().get(0) + "]";
			String part4 = "[" + splitNode.getOtherBinding().get(0) + "]";

			String prefix = "for $tuple in join(\n";
			String finalResult = "";
			finalResult += (prefix + part1 + ",\n\n" + part2 + ",\n\n" + part3
					+ ", " + part4 + "\n)\n");
			finalResult += ("return " + xqResultStr);
			System.out.println(finalResult);
			return finalResult;

		} else {
			log.ErrorLog("Partitions more than 2 has not been implemented yet!");
			return null;
		}

	}

	private String GetInnerReturnText(Partition partition) {
		String result = "return <tuple>{\n";

		for (String str : partition.varNameSet) {
			result += "<" + str.substring(1) + ">{" + str + "}</"
					+ str.substring(1) + ">\n";
		}
		result += "}</tuple>";
		return result;
	}

	private String AppendWhereText(String current, Partition partition) {
		String result = new String(current);
		boolean firstFlag = true;
		for (OperateNode on : partition.operateNodeSet) {
			if (firstFlag) {
				result += ("\nwhere " + on.getBaseBinding().get(0) + " eq " + on
						.getStringConstant());
				firstFlag = false;
			} else {
				result += (" and " + on.getBaseBinding().get(0) + " eq " + on
						.getStringConstant());
			}
		}
		return result;
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
