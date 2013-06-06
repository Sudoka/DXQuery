package XQuery;

import java.util.HashMap;

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

	public void Extend(String var, VariableKeeper value) {
		value.SetName(var);
		context.put(var, value);
	}

	public VariableKeeper Lookup(String var) {
		// here we assume that, there is not variable in the context binds to
		// null
		return context.get(var);
	}
}
