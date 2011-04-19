/*
 * All content copyright (c) 2003-2008 Terracotta, Inc., except as may otherwise be noted in a separate copyright notice.  All rights reserved.
 */

/* Generated By:JJTree: Do not edit this line. D:/aw/cvs_aw/aspectwerkz4/src/main/org/codehaus/aspectwerkz/expression/ast\ExpressionParserVisitor.java */

package com.tc.aspectwerkz.expression.ast;

public interface ExpressionParserVisitor {
  public Object visit(SimpleNode node, Object data);

  public Object visit(ASTRoot node, Object data);

  public Object visit(ASTExpression node, Object data);

  public Object visit(ASTAnd node, Object data);

  public Object visit(ASTOr node, Object data);

  public Object visit(ASTNot node, Object data);

  public Object visit(ASTPointcutReference node, Object data);

  public Object visit(ASTExecution node, Object data);

  public Object visit(ASTCall node, Object data);

  public Object visit(ASTSet node, Object data);

  public Object visit(ASTGet node, Object data);

  public Object visit(ASTHandler node, Object data);

  public Object visit(ASTWithin node, Object data);

  public Object visit(ASTWithinCode node, Object data);

  public Object visit(ASTStaticInitialization node, Object data);

  public Object visit(ASTClassPattern node, Object data);

  public Object visit(ASTCflow node, Object data);

  public Object visit(ASTCflowBelow node, Object data);

  public Object visit(ASTArgs node, Object data);

  public Object visit(ASTHasMethod node, Object data);

  public Object visit(ASTHasField node, Object data);

  public Object visit(ASTTarget node, Object data);

  public Object visit(ASTThis node, Object data);

  public Object visit(ASTIf node, Object data);

  public Object visit(ASTMethodPattern node, Object data);

  public Object visit(ASTConstructorPattern node, Object data);

  public Object visit(ASTFieldPattern node, Object data);

  public Object visit(ASTParameter node, Object data);

  public Object visit(ASTArgParameter node, Object data);

  public Object visit(ASTAttribute node, Object data);

  public Object visit(ASTModifier node, Object data);
}
