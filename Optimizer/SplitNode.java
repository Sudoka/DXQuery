package Optimizer;

import java.util.ArrayList;

import XQuery.DebugLogger;

public class SplitNode extends AbstractBaseCondNode implements
		SplitNodeInterface {

	private ArrayList<String> otherBinding = null;

	public SplitNode() {
		log = new DebugLogger("SplitNode");
	}

	@Override
	public ArrayList<String> getOtherBinding() {
		return otherBinding;
	}

	@Override
	public void setOtherBinding(ArrayList<String> bindingList) {
		if (otherBinding == null) {
			otherBinding = new ArrayList<String>();
		}
		otherBinding.addAll(bindingList);
	}

	@Override
	public void printThisNode() {
		log.DebugLog("base binding:" + getBaseBinding().get(0) + "\n"
				+ "other binding" + getOtherBinding().get(0));
	}

}
