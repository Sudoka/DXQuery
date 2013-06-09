package XQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import org.w3c.dom.Node;

public class XContext {

	public DebugLogger log = new DebugLogger("XContext");

	private HashMap<String, VariableKeeper> context = null;

	private HashMap<Node, Integer> removeStatusIndicator = null;

	public XContext() {
		context = new HashMap<String, VariableKeeper>();
		removeStatusIndicator = new HashMap<Node, Integer>();
	}

	public XContext clone() {
		XContext newContext = new XContext();
		newContext.context = new HashMap<String, VariableKeeper>();
		for (String varName : this.context.keySet()) {
			String newName = new String(varName);
			newContext.context.put(newName, this.context.get(varName).clone());
		}
		newContext.removeStatusIndicator = new HashMap<Node, Integer>();
		for (Node node : this.removeStatusIndicator.keySet()) {
			Integer newInteger = new Integer(removeStatusIndicator.get(node));
			newContext.removeStatusIndicator.put(node, newInteger);
		}
		return newContext;
	}

	public VariableKeeper Zip() {
		ArrayList<Node> nodeList = new ArrayList<Node>();
		ArrayList<ArrayList<VarNode>> nodeTuple = new ArrayList<ArrayList<VarNode>>();
		VariableKeeper result = new VariableKeeper();
		for (VariableKeeper vk : context.values()) {
			for (Node n : vk.GetNodes()) {
				nodeList.add(n);
				nodeTuple.add(vk.GetLinkData(n));
			}
		}
		for (int i = 0; i < nodeList.size(); i++) {
			result.AddNodeWithLink(nodeList.get(i), nodeTuple.get(i));
		}
		return result;
	}

	public void Remove(String varName) {
		context.remove(varName);
	}

	public void Extend(String var, VariableKeeper value) {
		value.SetName(var);
		context.put(var, value);
	}

	public VariableKeeper Lookup(String var) {
		// here we assume that, there is not variable in the context binds to
		// null
		return context.get(var);
	}

	public Set<String> GetVarNames() {
		return context.keySet();
	}

	private void AddIndicator(Node node) {
		// increase the indicator by 1
		if (!removeStatusIndicator.containsKey(node)) {
			removeStatusIndicator.put(node, 1);
		} else {
			Integer indicator = removeStatusIndicator.get(node);
			removeStatusIndicator.put(node, ++indicator);
		}
	}

	public void RecursiveRemoveVariableKeeper(VariableKeeper vk) {

		// traverse vk and establish removeStatus hashmap
		for (Node node : vk.GetNodes()) {
			// if the node directly appears in the vk, it should be deleted no
			// matter how many children it has
			ArrayList<VarNode> LinkedData = vk.GetLinkData(node);
			VarNode lastNode = LinkedData.get(LinkedData.size() - 1);
			String varName = lastNode.name;
			// log.ErrorLog("node:" + node.getNodeName() + "\n"
			// + node.getNodeValue() + "\n" + "VarNode:" + lastNode.name
			// + " " + lastNode.node.getNodeName() + "\n"
			// + lastNode.node.getNodeValue());
			assert (lastNode.node == node);
			if (context.containsKey(varName)) {
				context.remove(varName);
			}
			for (int i = 0; i < LinkedData.size() - 1; i++) {
				VarNode removeNode = LinkedData.get(i);
				AddIndicator(removeNode.node);
			}
		}

		ArrayList<String> keySet = new ArrayList<String>();
		for (String string : context.keySet()) {
			keySet.add(new String(string));
		}
		// traverse and remove nodes
		for (String varName : keySet) {
			// for each variable binding
			VariableKeeper VK = context.get(varName).clone();
			for (Node node : VK.GetNodes()) {
				// for each node binded to a variable
				Integer indicator = removeStatusIndicator.get(node);
				if (indicator != null && indicator > 0) {
					if (indicator > 0) {
						context.get(varName).RemoveNode(node);
						AddIndicator(node.getParentNode());
					}
				}
			}
		}
	}

	public void DebugPrintAllBingdings() {
		log.DebugLog(" Debugging infromation from XContext!!!\n"
				+ "--------------------------------------------");
		for (String varName : GetVarNames()) {
			System.out.println("Var Name:"
					+ varName
					+ "; bind to:<"
					+ Lookup(varName).size()
					+ "> "
					+ Lookup(varName).GetVarNodeList().get(0).node
							.getNodeName()
					+ "; with link length:"
					+ Lookup(varName).GetLinkData(
							Lookup(varName).GetVarNodeList().get(0).node)
							.size());
		}
	}

	public void Subtract(VariableKeeper VK) {
		for (ArrayList<VarNode> varNodeList : VK.GetWholeLinkData()) {
			for (VarNode varnode : varNodeList) {
				if (varnode.name != null && context.containsKey(varnode.name))
					context.get(varnode.name).RemoveNode(varnode.node);
			}
		}
	}

	public void Subtract(XContext subContext) {
		for (String str : subContext.context.keySet()) {
			VariableKeeper vk1 = context.get(str);
			VariableKeeper vk2 = subContext.context.get(str);
			vk1.Subtract(vk2);
		}
	}

	public void Union(XContext con) {
		for (String varName : con.context.keySet()) {
			VariableKeeper thisVK = context.get(varName);
			if (context.containsKey(varName)) {
				for (VarNode varNode : thisVK.GetVarNodeList()) {
					context.get(varName).AddNodeWithLink(varNode.node,
							thisVK.GetLinkData(varNode.node));
				}
			} else {
				context.put(varName, con.context.get(varName));
			}
		}
	}

	public void Union(VariableKeeper vk) {
		for (VarNode varnode : vk.GetVarNodeList()) {
			if (context.containsKey(varnode.name)) {
				context.get(varnode.name).AddNodeWithLink(varnode.node,
						vk.GetLinkData(varnode.node));
			} else {
				VariableKeeper newVK = new VariableKeeper();
				newVK.AddNodeWithLink(varnode.node,
						vk.GetLinkData(varnode.node));
				Extend(varnode.name, newVK);
			}
		}
	}

	public void Intersect(XContext con) {
		ArrayList<String> varNameList = new ArrayList<String>();
		for (String string : context.keySet()) {
			varNameList.add(new String(string));
		}
		ArrayList<String> remove = new ArrayList<String>();
		for (String string : varNameList) {
			if (!con.context.containsKey(string)) {
				context.remove(string);
				remove.add(string);
			}
		}
		varNameList.removeAll(remove);
		for (String string : varNameList) {
			context.get(string).Subtract(con.context.get(string));
		}
	}

	public void Intersect(VariableKeeper vk) {
		XContext subResult = clone();
		XContext toSub = vk.ConverToContext();
		subResult.Subtract(toSub);
		this.Subtract(subResult);
	}

	// attentsion, linkdata must already contains information of node
	public void PutNodeWithLinkData(VarNode node, ArrayList<VarNode> linkData) {
		if (context.containsKey(node.name)) {
			context.get(node.name).AddNodeWithLink(node.node, linkData);
		} else {
			VariableKeeper newVK = new VariableKeeper();
			newVK.AddNodeWithLink(node.node, linkData);
			Extend(node.name, newVK);
		}
	}
}
