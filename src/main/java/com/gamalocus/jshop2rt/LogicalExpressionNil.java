package com.gamalocus.jshop2rt;

/** Each empty logical expression at compile time is represented as an instance
 *  of this class.
 *
 *  @author Okhtay Ilghami
 *  @author <a href="http://www.cs.umd.edu/~okhtay">http://www.cs.umd.edu/~okhtay</a>
 *  @version 1.0.3
*/
public class LogicalExpressionNil extends LogicalExpression
{
  private static final long serialVersionUID = 1314572605685241565L;

  /** This class does not need any initialization code, therefore, this
   *  function simply returns an empty <code>String</code>.
  */
  @Override
  public String getInitCode(String label)
  {
    return "";
  }

  protected LogicalExpression getNNF(boolean negated) {
	  if (negated) {
		  return new LogicalExpressionNegation(this);
	  } else {
		  return this;
	  }
  }
  /** This class does not need to propagate the variable count, therefore, this
   *  function does nothing.
  */
  @Override
  protected void propagateVarCount(int varCount)
  {
  }

  /** This function produces the Java code to create a
   *  <code>PreconditionNil</code> object that represents this empty logical
   *  expression at run time.
  */
  @Override
  public String toCode(String label)
  {
    return "new PreconditionNil("+ getVarCount() + ")";
  }
}
