package Optimizer;

import java.util.ArrayList;

import XQuery.DebugLogger;

public abstract class AbstractBaseCondNode {
	private ArrayList<String> baseBinding;
	public DebugLogger log;

	public ArrayList<String> getBaseBinding() {
		return baseBinding;
	};

	public void setBaseBinding(ArrayList<String> bindingList) {
		if (baseBinding == null) {
			baseBinding = new ArrayList<String>();
		}
		baseBinding.addAll(bindingList);
	}

	public abstract void printThisNode();

	public int nodeType;
	public static final int SPLITNODE = 1;
	public static final int OPERATENODE = 2;
}
