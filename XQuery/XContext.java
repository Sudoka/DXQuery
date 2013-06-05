package XQuery;

import java.util.HashMap;

public class XContext {

	public DebugLogger log = new DebugLogger("XContext");

	private HashMap<String, VariableKeeper> context = null;

	public XContext() {
		context = new HashMap<String, VariableKeeper>();
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
