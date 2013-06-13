package XQuery;

import org.w3c.dom.Node;

public class VarNode {
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
		String nameClone = null;
		if (this.name != null)
			nameClone = new String(this.name);
		return new VarNode(nameClone, this.node);
	}

	public boolean equals(VarNode v) {
		assert ((v.node.isSameNode(node)) == (v.node == node));
		if (v.name.equals(this.name) && NodeProcessor.CheckEQ(v.node, node)) {
			return true;
		}
		return false;
	}
}