/* Generated By:JJTree: Do not edit this line. AST_OneSlash.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST_,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package XQuery;

public
class AST_OneSlash extends SimpleNode {
  public AST_OneSlash(int id) {
    super(id);
  }

  public AST_OneSlash(XQueryParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(XQueryParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=5f9ac2741dbe8063d91224675d9c9e61 (do not edit this line) */
