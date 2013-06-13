package Optimizer;

import XQuery.DebugLogger;

public class OperateNode extends AbstractBaseCondNode implements
		OperateNodeInterface {

	private String stringConstant = null;

	public OperateNode() {
		log = new DebugLogger("OperateNode");
	}

	@Override
	public String getStringConstant() {
		return stringConstant;
	}

	@Override
	public void setStringConstant(String stringConstant) {
		this.stringConstant = new String(stringConstant);
	}

	@Override
	public void printThisNode() {
		log.DebugLog("base binding:" + getBaseBinding().get(0) + "\n"
				+ "string constant" + stringConstant);
	}

}
