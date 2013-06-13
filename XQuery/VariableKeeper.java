package XQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.xerces.dom.DocumentImpl;

import org.w3c.dom.Node;

public class VariableKeeper {
	public static final int EQ_DISJOINT = 1;
	public static final int IS_DISJOINT = 2;

	// hashmap used as index for hash join
	protected HashMap<Node, ArrayList<VarNode>> hashIndex = null;

	// link
	// private ArrayList<ArrayList<VarNode>> linkedData = null;

	private String Name = null;

	public DebugLogger log = new DebugLogger("VariableKeeper");

	public VariableKeeper() {
		hashIndex = new HashMap<Node, ArrayList<VarNode>>();
		// linkedData = new ArrayList<ArrayList<VarNode>>();
	}

	public VariableKeeper(Node node, ArrayList<VarNode> linkedData) {
		hashIndex = new HashMap<Node, ArrayList<VarNode>>();
		hashIndex.put(node, linkedData);
	}

	public VariableKeeper(ArrayList<Object> nodeList) {
		hashIndex = new HashMap<Node, ArrayList<VarNode>>();
		// linkedData = new ArrayList<ArrayList<VarNode>>();
		InitializeWithNodeList(nodeList);
	}

	public VariableKeeper CreateByMerge(VariableKeeper var) {
		VariableKeeper result = new VariableKeeper();
		VariableKeeper thisDummy = this.clone();
		VariableKeeper varDummy = var.clone();
		result.hashIndex.putAll(thisDummy.hashIndex);
		result.hashIndex.putAll(varDummy.hashIndex);
		// result.linkedData.addAll(thisDummy.linkedData);
		// result.linkedData.addAll(varDummy.linkedData);
		return result;
	}

	protected void MergeVK(VariableKeeper vk) {
		this.hashIndex.putAll(vk.hashIndex);
	}

	public void PrintAllVars() {
		// if (this instanceof VariableKeeperExtended) {
		// for (ArrayList<Node> nlist : ((VariableKeeperExtended)
		// this).groupList) {
		// for (Node node : nlist) {
		// try {
		// System.out.println(DOMPrinter.printXML(node));
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// }
		//
		// } else {
		for (Node node : hashIndex.keySet()) {
			try {
				System.out.println(DOMPrinter.printXML(node));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// }
	}

	public VariableKeeper clone() {
		VariableKeeper newVar = new VariableKeeper();
		// clone name
		if (this.Name == null)
			newVar.Name = null;
		else
			newVar.Name = new String(this.Name);
		// clone linked data
		// newVar.linkedData = new ArrayList<ArrayList<VarNode>>();
		// for (ArrayList<VarNode> varlist : this.linkedData) {
		// ArrayList<VarNode> tmpList = new ArrayList<VarNode>();
		// for (VarNode varNode : varlist) {
		// tmpList.add(varNode.clone());
		// }
		// newVar.linkedData.add(tmpList);
		// }
		// clone hashIndex
		newVar.hashIndex = new HashMap<Node, ArrayList<VarNode>>();
		for (Node node : hashIndex.keySet()) {
			ArrayList<VarNode> tmpList = new ArrayList<VarNode>();
			for (VarNode varNode : hashIndex.get(node)) {
				tmpList.add(varNode.clone());
			}
			newVar.hashIndex.put(node, tmpList);
		}
		return newVar;
	}

	public ArrayList<VarNode> GetLinkData(Node n) {
		return hashIndex.get(n);
	}

	public void AddNodeWithLink(Node n, ArrayList<VarNode> linkData) {
		// linkedData.add(linkData);
		hashIndex.put(n, linkData);
		// hashIndex.put(n, linkedData.get(linkedData.indexOf(linkData)));
	}

	public Set<Node> GetNodes() {
		return hashIndex.keySet();
	}

	public int size() {
		return hashIndex.size();
	}

	public void RemoveNode(Node node) {
		ArrayList<Node> tmpNodes = new ArrayList<Node>();
		for (Node node2 : GetNodes()) {
			if (NodeProcessor.CheckEQ(node, node2)) {
				tmpNodes.add(node2);
			}
		}
		for (Node node2 : tmpNodes) {
			// ArrayList<VarNode> removeLinkList = hashIndex.get(node2);
			// int i = linkedData.indexOf(removeLinkList);
			// linkedData.remove(removeLinkList);
			hashIndex.remove(node2);
		}

	}

	public void Subtract(VariableKeeper sub) {
		for (Node node : sub.hashIndex.keySet()) {
			RemoveNode(node);
			// ArrayList<VarNode> varNodeList = sub.hashIndex.get(node);
			// for (VarNode varNode : varNodeList) {
			// RemoveNode(varNode.node);
			// }
		}
	}

	public VariableKeeper Intersect(VariableKeeper var) {
		VariableKeeper clone1 = clone();
		VariableKeeper clone2 = clone();
		clone1.Subtract(var);
		clone2.Subtract(clone1);
		return clone2;
	}

	public VariableKeeper Intersect(VariableKeeper var1, VariableKeeper var2) {
		VariableKeeper result = new VariableKeeper();
		for (Node node1 : var1.GetNodes()) {
			for (Node node2 : var2.GetNodes()) {
				if (NodeProcessor.CheckEQ(node1, node2)) {
					result.AddNodeWithLink(node1, var1.GetLinkData(node1));
					ArrayList<VarNode> varNodeList1 = var1.GetLinkData(node1);
					ArrayList<VarNode> varNodeList2 = var2.GetLinkData(node2);
					if (varNodeList1 != null && varNodeList2 != null) {
						int size1 = varNodeList1.size();
						int size2 = varNodeList2.size();
						int bound = (size1 < size2 ? size1 : size2);
						for (int i = 2; i < bound; i++) {
							Node node1Parent = varNodeList1.get(size1 - i).node;
							Node node2Parent = varNodeList2.get(size2 - i).node;
							if (!node1Parent.isSameNode(node2Parent)) {
								result.AddNodeWithLink(node2,
										var2.GetLinkData(node2));
							}
						}
					}
				}
			}
		}

		return result;
	}

	// this method may need to optimize for performance
	public VariableKeeper DisJoint(VariableKeeper var, int operation) {
		// VariableKeeper result = new VariableKeeper();
		VariableKeeper result2 = FindDifferent(this, var, operation);
		// System.err.println("test1:\n");
		// FindDifferent(this, var, operation).PrintAllVars();
		VariableKeeper finalResult = result2.CreateByMerge(FindDifferent(var,
				this, operation));
		// System.err.println("test2:\n");
		// FindDifferent(var, this, operation);
		return finalResult;
	}

	private VariableKeeper FindDifferent(VariableKeeper main,
			VariableKeeper compare, int operation) {
		VariableKeeper result = new VariableKeeper();

		for (Node mainNode : main.hashIndex.keySet()) {
			boolean uniqueFlag = true;
			for (Node compareNode : compare.hashIndex.keySet()) {
				if (operation == EQ_DISJOINT) {
					if (NodeProcessor.CheckEQ(mainNode, compareNode)) {
						uniqueFlag = false;
						break;
					}
				} else if (operation == IS_DISJOINT) {
					if (mainNode.isSameNode(compareNode)) {
						uniqueFlag = false;
						break;
					}
				} else {
					log.ErrorLog("Unknow operation type in DisJoint!");
					return null;
				}
			}
			if (uniqueFlag) {
				result.AddNodeWithLink(mainNode, main.GetLinkData(mainNode));
			}
		}
		return result;
	}

	public ArrayList<ArrayList<VarNode>> GetWholeLinkData() {
		ArrayList<ArrayList<VarNode>> linkedData = new ArrayList<ArrayList<VarNode>>();
		for (ArrayList<VarNode> varNodeList : hashIndex.values()) {
			linkedData.add(varNodeList);
		}
		return linkedData;
	}

	public ArrayList<VarNode> GetVarNodeList() {
		// if (this.Name == null || this.Name == "") {
		// return new ArrayList<VarNode>();
		// }
		ArrayList<VarNode> result = new ArrayList<VarNode>();
		for (ArrayList<VarNode> varList : hashIndex.values()) {
			// assert (varList.get(varList.size() - 1).name.equals(this.Name));
			if (varList.size() > 0)
				result.add(varList.get(varList.size() - 1));
		}
		return result;
	}

	public void DebugPrintInfo() {
		log.DebugLog("Printing VariableKeeper information:");

	}

	/**
	 * simply add nodes to hashmap with value null
	 * 
	 * @param list
	 * @return
	 */
	public boolean InitializeWithNodeList(ArrayList<Object> list) {
		if (hashIndex.size() > 0) {
			log.ErrorLog("SimpleAddNodeList should only be used on empty VK!\n "
					+ "This VK already has link data!");
			return false;
		}
		for (Object n : list) {
			if (n instanceof Node) {
				ArrayList<VarNode> tmpList = new ArrayList<VarNode>();
				VarNode tmpNode = new VarNode("", (Node) n);
				tmpList.add(tmpNode);
				// linkedData.add(tmpList);
				hashIndex.put((Node) n, tmpList);
			} else {
				log.ErrorLog(" in SimpleAddNodeList, encountered none node type in list! ");
				return false;
			}
		}
		return true;
	}

	public boolean HasLink() {
		if (hashIndex.values().size() <= 0) {
			return false;
		}
		return true;
	}

	public String GetName() {
		return this.Name;
	}

	public void SetName(String name) {
		log.RegularLog("in SetName, old name is:" + this.Name
				+ "; new name will be" + name);
		if (name != null)
			this.Name = new String(name);
		else
			this.Name = null;
		for (ArrayList<VarNode> vn : hashIndex.values()) {
			if (vn != null) {
				if (name != null) {
					vn.get(vn.size() - 1).name = new String(name);
				} else {
					vn.get(vn.size() - 1).name = null;
				}
			}
		}
	}

	/**
	 * TODO: to be implemented!
	 * 
	 * @param oldVariable
	 * @param oldNodeToBeExtended
	 * @param data
	 */
	public void ExtendSchema(VariableKeeper oldVariable,
			Node oldNodeToBeExtended, ArrayList<Node> data) {

	}

	/**
	 * Add one tuple to the data
	 * 
	 * @param tuple
	 *            to be added
	 * @return true if success, false if failed
	 */
	public boolean AddTuple(ArrayList<VarNode> tuple) {
		// if (data == null) {
		// data = new ArrayList<ArrayList<VarNode>>();
		// data.add(tuple);
		// } else {
		// /**
		// * added tuple should have the same size; tuple size can only be
		// * great than schema size by 1 or equal to schema size, since the
		// * last column to be added to a schema is out side of current XQ
		// * node
		// */
		// if (tuple.size() != data.get(0).size()
		// || tuple.size() > schema.size() + 1
		// || tuple.size() < schema.size()) {
		// // data have different number of columns! Illegal!
		// return false;
		// }
		// }
		return true;
	}

	public boolean AppendColumn(String varName) {
		// if (data != null) {
		// /**
		// * appending a column can only be done after the data of that column
		// * has been added.
		// */
		// if (schema.size() + 1 != data.get(0).size()) {
		// return false;
		// }
		// }
		// schema.add(varName);
		return true;
	}

	/**
	 * 
	 * @param variable
	 *            name of the to be filtered variable
	 * @param filter
	 *            the node values that should be removed
	 */
	public void FilterTuple(String variable, ArrayList<Node> filter) {

	}

	public XContext ConverToContext() {
		XContext context = new XContext();
		for (VarNode varnode : GetVarNodeList()) {
			context.PutNodeWithLinkData(varnode, GetLinkData(varnode.node));
		}
		return context;
	}

	public VariableKeeper GroupByTag(String tagName, DocumentImpl doc) {
		// get all the distinct node type names
		Set<String> tagNames = new HashSet<String>();
		for (Node node : GetNodes()) {
			tagNames.add(node.getNodeName());
		}

		return null;
	}

	public XContext GenerateKeepContext() {
		XContext result = new XContext();

		Set<String> keepVarName = new HashSet<String>();
		if (size() > 0 && GetVarNodeList() != null) {
			for (ArrayList<VarNode> vnlist : hashIndex.values()) {
				for (VarNode varNode : vnlist) {
					if (varNode.name != null && varNode.name != "")
						keepVarName.add(varNode.name);
				}
			}
		}

		for (String string : keepVarName) {
			VariableKeeper vk = new VariableKeeper();
			for (ArrayList<VarNode> vnlist : hashIndex.values()) {
				for (VarNode varNode : vnlist) {
					if (varNode.name.equals(string)) {
						if (this.GetLinkData(varNode.node) == null) {
							ArrayList<VarNode> newlink = new ArrayList<VarNode>();
							newlink.add(varNode);
							vk.AddNodeWithLink(varNode.node, newlink);
						} else {
							vk.AddNodeWithLink(varNode.node,
									this.GetLinkData(varNode.node));
						}
					}
				}
			}
			result.Extend(string, vk);
		}
		return result;
	}

}
