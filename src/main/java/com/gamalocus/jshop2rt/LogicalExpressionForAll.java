package com.gamalocus.jshop2rt;

/** Each <code>ForAll</code> logical expression at compile time is represented
 *  as an instance of this class.
 *
 *  @author Okhtay Ilghami
 *  @author <a href="http://www.cs.umd.edu/~okhtay">http://www.cs.umd.edu/~okhtay</a>
 *  @version 1.0.3
*/
public class LogicalExpressionForAll extends LogicalExpression
{
  private static final long serialVersionUID = -2975785903806526855L;

  /** The consequence of this <code>ForAll</code> logical expression.
  */
  private final LogicalExpression consequence;

  /** The premise of this <code>ForAll</code> logical expression.
  */
  private final LogicalExpression premise;

  /** To initialize this <code>ForAll</code> logical expression.
   *
   *  @param premiseIn
   *          the premise of this <code>ForAll</code> logical expression.
   *  @param consequenceIn
   *          the consequence of this <code>ForAll</code> logical expression.
  */
  public LogicalExpressionForAll(LogicalExpression premiseIn,
                                 LogicalExpression consequenceIn)
  {
    premise = premiseIn;
    consequence = consequenceIn;
  }

  /** This function produces Java code that implements the classes any object
   *  of which can be used at run time to represent the premise and the
   *  consequence of the <code>ForAll</code> logical expression this object is
   *  representing.
  */
  public String getInitCode(String label)
  {
    return premise.getInitCode("premise of " + label) + consequence.getInitCode("consequence of " + label);
  }

  /** To propagate the variable count to the <code>ForAll</code> logical
   *  expression represented by this object.
  */
  protected void propagateVarCount(int varCount)
  {
    premise.setVarCount(varCount);
    consequence.setVarCount(varCount);
  }

  /** This function produces the Java code to create a
   *  <code>PreconditionForAll</code> object that represents this
   *  <code>ForAll</code> logical expression at run time.
  */
  public String toCode(String label)
  {
    return "new PreconditionForAll(" + premise.toCode("premise of " + label) + ", " +
           consequence.toCode("consequence of " + label) + ", " + getVarCount() + ")";
  }
}
