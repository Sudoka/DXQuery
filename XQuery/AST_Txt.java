/* Generated By:JJTree: Do not edit this line. AST_Txt.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST_,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package XQuery;

public
class AST_Txt extends SimpleNode {
  public AST_Txt(int id) {
    super(id);
  }

  public AST_Txt(XQueryParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(XQueryParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=bb7bdc9b284f567351df7b3ff389ec3f (do not edit this line) */
