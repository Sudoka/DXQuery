package XQuery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.w3c.dom.Node;

public class VariableKeeper {
	public static final int EQ_DISJOINT = 1;
	public static final int IS_DISJOINT = 2;

	// hashmap used as index for hash join
	private HashMap<Node, ArrayList<VarNode>> hashIndex = null;

	// link
	private ArrayList<ArrayList<VarNode>> linkedData = null;

	private String Name = null;

	public DebugLogger log = new DebugLogger("VariableKeeper");

	public VariableKeeper() {
		hashIndex = new HashMap<Node, ArrayList<VarNode>>();
		linkedData = new ArrayList<ArrayList<VarNode>>();
	}

	public VariableKeeper CreateByMerge(VariableKeeper var) {
		VariableKeeper result = new VariableKeeper();
		result.hashIndex.putAll(this.hashIndex);
		result.hashIndex.putAll(var.hashIndex);
		result.linkedData.addAll(this.linkedData);
		result.linkedData.addAll(var.linkedData);
		/*
		 * since this is a new variable that has not binded yet, we should set
		 * the names to null
		 */
		result.SetName(null);
		return result;
	}

	public void PrintAllVars() {
		for (Node node : hashIndex.keySet()) {
			try {
				DOMPrinter.printXML(node);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public VariableKeeper clone() {
		VariableKeeper newVar = new VariableKeeper();
		// clone name
		newVar.Name = new String(this.Name);
		// clone linked data
		newVar.linkedData = new ArrayList<ArrayList<VarNode>>();
		for (ArrayList<VarNode> varlist : this.linkedData) {
			ArrayList<VarNode> tmpList = new ArrayList<VarNode>();
			for (VarNode varNode : varlist) {
				tmpList.add(varNode.clone());
			}
			newVar.linkedData.add(tmpList);
		}
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
		hashIndex.put(n, linkData);
		linkedData.add(linkData);
	}

	public Set<Node> GetNodes() {
		return hashIndex.keySet();
	}

	public int size() {
		return hashIndex.size();
	}

	public void RemoveNode(Node node) {
		linkedData.remove(hashIndex.get(node));
		hashIndex.remove(node);
	}

	// this method may need to optimize for performance
	public ArrayList<VarNode> DisJoint(VariableKeeper var, int operation) {
		ArrayList<VarNode> result = new ArrayList<VarNode>();
		result.addAll(FindDifferent(this, var, operation));
		result.addAll(FindDifferent(var, this, operation));
		return result;
	}

	private ArrayList<VarNode> FindDifferent(VariableKeeper main,
			VariableKeeper compare, int operation) {
		ArrayList<VarNode> result = new ArrayList<VarNode>();

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
				VarNode addNode = new VarNode(main.Name, mainNode);
				result.add(addNode);
			}
		}
		return result;
	}

	public ArrayList<VarNode> GetVarNodeList() {
		if (this.Name == null || this.Name == "") {
			return null;
		}
		ArrayList<VarNode> result = new ArrayList<VarNode>();
		for (ArrayList<VarNode> varList : linkedData) {
			assert (varList.get(varList.size() - 1).name.equals(this.Name));
			result.add(varList.get(varList.size() - 1));
		}
		return result;
	}

	/**
	 * simply add nodes to hashmap with value null
	 * 
	 * @param list
	 * @return
	 */
	public boolean SimpleAddNodeList(ArrayList<Object> list) {
		if (linkedData != null && linkedData.size() > 0
				&& linkedData.get(0).size() > 0) {
			log.ErrorLog("SimpleAddNodeList should only be used on empty VK! "
					+ "This VK already has link data!");
			return false;
		}
		for (Object n : list) {
			if (n instanceof Node) {
				ArrayList<VarNode> tmpList = new ArrayList<VarNode>();
				VarNode tmpNode = new VarNode("", (Node) n);
				tmpList.add(tmpNode);
				linkedData.add(tmpList);
				hashIndex.put((Node) n, tmpList);
			} else {
				log.ErrorLog(" in SimpleAddNodeList, encountered none node type in list! ");
				return false;
			}
		}
		return true;
	}

	public boolean HasLink() {
		if (linkedData == null || linkedData.size() == 0) {
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
		for (ArrayList<VarNode> vn : linkedData) {
			if (name != null)
				vn.get(vn.size() - 1).name = new String(name);
			else {
				vn.get(vn.size() - 1).name = null;
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

}

class VarNode {
	String name = null;
	Node node = null;

	public VarNode() {
		// TODO Auto-generated constructor stub
	}

	public VarNode(String name, Node node) {
		if (name == null)
			this.name = "";
		else
			this.name = new String(name);
		this.node = node;
	}

	public VarNode clone() {
		return new VarNode(new String(this.name), this.node);
	}

	public boolean equals(VarNode v) {
		assert ((v.node.isSameNode(node)) == (v.node == node));
		if (v.name.equals(this.name) && v.node.isSameNode(node)) {
			return true;
		}
		return false;
	}
}