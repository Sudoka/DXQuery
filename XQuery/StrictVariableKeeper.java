package XQuery;

/**
 * this is to ensure that a VariableKeeper(VK) has a strict format. A VK is
 * strict when the last Varnode in the linkeddata all have the same node name
 * 
 * @author QDX
 * 
 */
public interface StrictVariableKeeper {
	public void Strictlize();
}
