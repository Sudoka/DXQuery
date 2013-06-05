package XQuery;

import java.util.HashMap;

public class XContext {

	public DebugLogger log = new DebugLogger("XContext");

	private HashMap<String, VariableKeeper> context = null;

	public XContext() {

	}

	public void Extend(String var, VariableKeeper value) {
		context.put(var, value);
	}

	public VariableKeeper Lookup(String var) {
		return context.get(var);
	}
}
