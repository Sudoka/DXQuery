package XQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VariableKeeperExtended extends VariableKeeper {

	public ArrayList<ArrayList<Node>> groupList = null;

	public VariableKeeperExtended() {
		super();
		groupList = new ArrayList<ArrayList<Node>>();
	}

	public VariableKeeperExtended(ArrayList<Object> nodeList) {
		super(nodeList);
		groupList = new ArrayList<ArrayList<Node>>();
	}

	public Set<VarNode> RecursiveFindLinkedData(ArrayList<Node> nl) {
		Set<VarNode> result = new HashSet<VarNode>();
		for (Node node : nl) {
			result.addAll(recursiveFindLinkVar(node, this));
		}
		return result;
	}

	private Set<VarNode> recursiveFindLinkVar(Node n, VariableKeeper vk) {
		Set<VarNode> set = new HashSet<VarNode>();
		if (hashIndex.containsKey(n)) {
			for (VarNode varNode : hashIndex.get(n)) {
				set.add(varNode);
			}
		}
		if (!n.hasChildNodes()) {
			return set;
		} else {
			NodeList nl = n.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				set.addAll(recursiveFindLinkVar(nl.item(i), vk));
			}
		}
		return set;
	}

	public void CreateMergeAdd(Node node, VariableKeeper vk) {
		super.MergeVK(vk);
		ArrayList<Node> addList = new ArrayList<Node>();
		addList.add(node);
		if (vk instanceof VariableKeeperExtended) {
			for (ArrayList<Node> nlist : ((VariableKeeperExtended) vk).groupList) {
				addList.addAll(nlist);
			}
		} else {
			addList.addAll(vk.hashIndex.keySet());
		}
		groupList.add(addList);
	}

	public VariableKeeperExtended clone() {
		VariableKeeper resultDummy = super.clone();
		VariableKeeperExtended result = (VariableKeeperExtended) resultDummy;
		result.groupList = new ArrayList<ArrayList<Node>>();
		for (ArrayList<Node> nlist : this.groupList) {
			ArrayList<Node> newList = new ArrayList<Node>();
			for (Node node : nlist) {
				newList.add(node);
			}
			result.groupList.add(newList);
		}
		return result;
	}

}
