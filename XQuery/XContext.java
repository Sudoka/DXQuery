package XQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import org.w3c.dom.Node;

public class XContext {

	public DebugLogger log = new DebugLogger("XContext");

	private HashMap<String, VariableKeeper> context = null;

	public XContext() {
		context = new HashMap<String, VariableKeeper>();
	}

	public XContext clone() {
		XContext newContext = new XContext();
		newContext.context = new HashMap<String, VariableKeeper>();
		for (String varName : this.context.keySet()) {
			String newName = new String(varName);
			newContext.context.put(newName, this.context.get(varName).clone());
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

	public void RemoveVarNodeAndLinkData(VarNode node) {
		RecursiveRemove(node);
	}

	public void RecursiveRemoveVariableKeeper(VariableKeeper vk) {
		for (ArrayList<VarNode> nodeList : vk.GetWholeLinkData()) {
			for (VarNode varNode : nodeList) {
				RecursiveRemove(varNode);
			}
		}
	}

	private void RecursiveRemove(VarNode node) {
		if (node.name == null || context.get(node.name) == null) {
			//log.DebugLog("Damn no node name!!");
			return;
		} else {
			ArrayList<VarNode> linkNodes = context.get(node.name).GetLinkData(
					node.node);
			if (linkNodes != null) {
				ArrayList<VarNode> cloneList = new ArrayList<VarNode>();
				for (VarNode varNode : linkNodes) {
					cloneList.add(varNode.clone());
				}
				context.get(node.name).RemoveNode(node.node);
				for (VarNode varNode : cloneList) {
					RecursiveRemove(varNode);
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

}
