/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 */

/* Generated By:JJTree: Do not edit this line. ASTPointcutReference.java */

package com.tc.aspectwerkz.expression.ast;

public class ASTPointcutReference extends SimpleNode {
  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (name.endsWith("(")) {
      // we have a pointcut reference with a signature
      this.name = name.substring(0, name.length() - 1);
    } else {
      this.name = name;
    }
  }

  public String name;

  public ASTPointcutReference(int id) {
    super(id);
  }

  public ASTPointcutReference(ExpressionParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor. *
   */
  public Object jjtAccept(ExpressionParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
