import XQuery.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.Integer;
import java.util.ArrayList;

import sun.util.logging.resources.logging;

class test {
	String str;
	ArrayList<ArrayList<String>> strMatrix;
}

public class Run implements XQueryParserTreeConstants {
	static String query = "(./ACT)";

	public static void main(String[] args)
	// --------------------------------------
	{
//		DebugLogger.MasterControl = false;
//		 DebugLogger.MasterRegularLog = false;
//		 DebugLogger.MasterDebugLog = false;

		String testcase = "";
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream("testcase.txt");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				testcase += (strLine+"\n");
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

		new Run().runQuery(testcase);
	}

	void runQuery(String queryStr)
	// ------------------------------
	{
		XQueryParser parser = new XQueryParser(new StringReader(queryStr));

		try {
			System.out.println("\nquery = " + queryStr + "\n");
			AST_Root root = parser.query();

			// dump the result tree to the console for debug purposes

			root.dump("");

			System.out.println();

			XQProcessVisitor visitor = new XQProcessVisitor();
			visitor.log.SetObjectControl(false, false, true);
			visitor.optimize = true;
			VariableKeeper res = (VariableKeeper) root.jjtAccept(visitor, null);
			System.out.println("=============final result=============");
			res.PrintAllVars();

			// here's where we actually walk the tree we've so painfully
			// constructed and derive an answer to our query

			int answer = eval((SimpleNode) root.jjtGetChild(0));

			System.out.println("\nanswer = " + answer);
		} catch (ParseException e) {
			System.out.print(e.getMessage());
			System.out.println();
		} catch (TokenMgrError tke) {
			System.out.print(tke.getMessage());
			System.out.println();
		}
	}

	int eval(SimpleNode node)
	// -------------------------
	{
		int result;

		// each node stores an id which uniquely identifies its type
		// jjtree names and stores these enums in an interface
		// named XQueryParserTreeConstants by JJTree
		int id = node.getId();
		String nodeName = node.toString();
		System.out.println(nodeName);

		return 0;
		// lhs = (SimpleNode) node.jjtGetChild(0);
		// rhs = (SimpleNode) node.jjtGetChild(1);

		// System.out.println( "eval(): evaluating AST nodetype "
		// + id + " [" + nodeName + "]" );

		/* switch( id ) */
		// {
		// case JJTPLUS : return eval( lhs ) + eval( rhs );
		// case JJTMINUS : return eval( lhs ) - eval( rhs );
		// case JJTMULT : return eval( lhs ) * eval( rhs );

		// case JJTINT : return Integer.parseInt( node.getText() );

		// default :

		// throw new java.lang.IllegalArgumentException(
		// "didn't understand the query, man!" );
		/* } */
	}
}
