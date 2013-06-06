package XQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

public class VariableKeeper {
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

	/**
	 * simply add nodes to hashmap with value null
	 * 
	 * @param list
	 * @return
	 */
	public boolean SimpleAddNodeList(ArrayList<Object> list) {
		if (linkedData != null && linkedData.size() > 0
				&& linkedData.get(0).size() > 0) {
			log.DebugLog("Calling SimpleAddNodeList into an none empty linkedData!");
		}
		for (Object n : list) {
			if (n instanceof Node) {
				hashIndex.put((Node) n, null);
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
		if(name != null)
			this.Name = new String(name);
		else 
			this.Name = null;
		for (ArrayList<VarNode> vn : linkedData) {
			if(name != null)
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

	}

	public VarNode(String name, Node node) {
		this.name = new String(name);
		this.node = node;
	}

}