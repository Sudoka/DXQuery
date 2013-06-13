package Optimizer;

import java.util.Set;

public class Partition {

	public Set<String> varNameSet;
	public Set<OperateNode> operateNodeSet;

	public Partition(Set<String> varSet, Set<OperateNode> nodeSet) {
		varNameSet = varSet;
		operateNodeSet = nodeSet;
	}

}
