import XQuery.*;
import java.io.StringReader;
import java.lang.Integer;

public class Run implements XQueryParserTreeConstants {
	static String query = "(./ACT)";

	public static void main(String[] args)
        // --------------------------------------
    {
        String test = 
            "for $a in doc(\"j_caesar.xml\")//ACT, \n"
            + "$sc in $a//SCENE, \n"
            + "$sp in $sc/SPEECH \n"
            + "where $sp/LINE/text() = \"Et tu, Brute! Then fall, Caesar.\" \n"
            + "return <who>{$sp/SPEAKER/text()}</who> \n"
            + "  <when>{<act>{$a/title/text()}</act> \n"
            + "<scene>{$sc/title/text()}</scene>} \n" + "       <when> \n";
        String test2 = "for $s in document(\"j_caesar.xml\")//SPEAKER \n"
            + "return <speaks>{<who>{$s/text()}</who>, \n"
            + "                for $a in document(\"j_caesar.xml\")//ACT\n"
            + "                where some $s1 in $a//SPEAKER satisfies $s1 eq $s\n"
            + "                return <when>{$a/title/text()}</when>}\n"
            + "</speaks>\n";

        String test3 = "for $A in document(\"test.xml\")/AS\n"
            + "where $A = \"test\"\n" + "return <a>{$A}</a>\n";

        String test4 = "<result>\n"
            + "{\n"
            + "for $a in document(\"j_caesar.xml\")//ACT,\n"
            + "    $sc in $a//SCENE,\n"
            + "    $sp in $sc/SPEECH\n"
            + "where $sp/LINE/text() = \"Et tu, Brute! Then fall, Caesar.\"\n"
            + "return <who>{$sp/SPEAKER/text()}</who>,\n"
            + "       <when>{\n" + "	      <act>{$a/title/text()}</act>,\n"
            + "              <scene>{$sc/title/text()}</scene>\n"
            + "       	     } </when>\n" + "}\n" + "</result>\n";

        String test5 = "for $s in document(\"j_caesar.xml\")//SPEAKER\n"
            + "return <speaks>{<who>{$s/text()}</who>,\n"
            + "                for $a in document(\"j_caesar.xml\")//ACT\n"
            + "                where some $s1 in $a//SPEAKER satisfies $s1 eq $s\n"
            + "                return <when>{$a/title/text()}</when>}\n"
            + "       </speaks>\n";

        String testAP = "doc(\"j_caesar.xml\")//ACT";
        new Run().runQuery(testAP);
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
            root.jjtAccept(visitor, null);

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
