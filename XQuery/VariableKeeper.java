package XQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

public class VariableKeeper {
	// hashmap used as index for hash join
	private HashMap<Node, ArrayList<Node>> hashIndex = null;
	// variable name and it's corresponding position in data
	private ArrayList<String> schema = null;
	// data tuples
	private ArrayList<ArrayList<Node>> data = null;
	
	public DebugLogger log = new DebugLogger("VariableKeeper");

	public VariableKeeper(){
		
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
		ArrayList<Node> oldTuple = oldVariable.hashIndex
				.get(oldNodeToBeExtended);

	}

	/**
	 * Add one tuple to the data
	 * 
	 * @param tuple
	 *            to be added
	 * @return true if success, false if failed
	 */
	public boolean AddTuple(ArrayList<Node> tuple) {
		if (data == null) {
			data = new ArrayList<ArrayList<Node>>();
			data.add(tuple);
		} else {
			/**
			 * added tuple should have the same size; tuple size can only be
			 * great than schema size by 1 or equal to schema size, since the
			 * last column to be added to a schema is out side of current XQ
			 * node
			 */
			if (tuple.size() != data.get(0).size()
					|| tuple.size() > schema.size() + 1
					|| tuple.size() < schema.size()) {
				// data have different number of columns! Illegal!
				return false;
			}
		}
		return true;
	}

	public boolean AppendColumn(String varName) {
		if (data != null) {
			/**
			 * appending a column can only be done after the data of that column
			 * has been added.
			 */
			if (schema.size() + 1 != data.get(0).size()) {
				return false;
			}
		}
		schema.add(varName);
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
