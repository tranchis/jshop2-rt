package com.gamalocus.jshop2rt;

import java.util.Vector;

/** Each disjunction at compile time is represented as an instance of this
 *  class.
 *
 *  @author Okhtay Ilghami
 *  @author <a href="http://www.cs.umd.edu/~okhtay">http://www.cs.umd.edu/~okhtay</a>
 *  @version 1.0.3
*/
public class LogicalExpressionDisjunction extends LogicalExpression
{
  private static final long serialVersionUID = 8012782595759344067L;

  /** The number of objects instantiated from this class before this object was
   *  instantiated. Used to make the name of the precondition class that
   *  implements this disjunction unique.
  */
  private final int cnt;

  /** An array of logical expressions the disjunction of which is represented
   *  by this object.
  */
  private final LogicalExpression[] le;

  /** To initialize this disjunction.
   *
   *  @param leIn
   *          a <code>Vector</code> of logical expressions the disjunction of
   *          which is represented by this object. Note that we use a
   *          <code>Vector</code> rather than an array since at compile time
   *          we do not know how many disjuncts there are in this particular
   *          disjunction.
  */
  public LogicalExpressionDisjunction(Vector<LogicalExpression> leIn)
  {
    le = new LogicalExpression[leIn.size()];

    for (int i = 0; i < leIn.size(); i++)
      le[i] = leIn.get(i);

    cnt = getClassCnt();
  }

  /** This function produces Java code that implements the classes any object
   *  of which can be used at run time to represent the disjuncts of this
   *  disjunction, and the disjunction itself.
  */
  public String getInitCode(String label)
  {
    String s = "";
    int i;

    //-- First produce any code needed by the disjuncts.
    for (i = 0; i < le.length; i++)
      s += le[i].getInitCode(String.format("Disjunct #%d of %s", i, label));

    //-- The header of the class for this disjunction at run time. Note the use
    //-- of 'cnt' to make the name of this class unique.
    s += "\t/**" + endl;
    s += "\t * " + label + endl;
    s += "\t * " + getSourcePosForComment() + endl;
    s += "\t */" + endl;
    s += "\tpublic static class Precondition" + cnt + " extends Precondition" + endl;

    //-- Defining two arrays for storing the iterators for each disjunct and
    //-- the current binding.
    s += "\t{" + endl + "\t\tPrecondition[] p;" + endl + "\t\tTerm[] b;" + endl;

    //-- Defining an integer to keep track of which disjunct has already been
    //-- considered.
    s += "\t\tint whichClause;" + endl + endl;

    //-- The constructor of the class.
    s += "\t\tpublic Precondition" + cnt + "(Domain owner, Term[] unifier)" + endl + "\t\t{";

    //-- Allocate the array of iterators.
    s += endl + "\t\t\tp = new Precondition[" + le.length + "];" + endl;

    //-- For each disjunct,
    for (i = 0; i < le.length; i++)
      //-- Set the corresponding element in the array to the code that produces
      //-- that disjunct.
      s += "\t\t\tp[" + i + "] = " + le[i].toCode(String.format("Disjunct #%d of %s", i, label)) + ";" + endl + endl;

    //-- A conjucntion can be potentially satisfied more than once, so the
    //-- default for the 'isFirstCall' flag is false.
    s += "\t\t\tsetFirst(false);" + endl + "\t\t}" + endl + endl;

    //-- Define the 'bind' function.
    s += "\t\tpublic void bind(Term[] binding)" + endl + "\t\t{" + endl;

    //-- Implement the 'bind' function by:
    for (i = 0; i < le.length; i++)
      //-- Binding each disjunct in this disjunction.
      s += "\t\t\tp[" + i + "].bind(binding);" + endl;

    //-- Define the 'nextBindingHelper' function.
    s += "\t\t}" + endl + endl + "\t\tprotected Term[] nextBindingHelper(State state)" + endl;
    s += "\t\t{";

    //-- Implement the 'nextBindingHelper' function by iterating over all
    //-- disjuncts:
    s += endl + "\t\t\twhile (whichClause < " + le.length + ")" + endl;

    //-- Look for the next binding for the current disjunct.
    s += "\t\t\t{" + endl + "\t\t\t\tb = p[whichClause].nextBinding(state);" + endl;

    //-- If there is such a binding, return it.
    s += "\t\t\t\tif (b != null)" + endl + "\t\t\t\t\t return b;" + endl;

    //-- Otherwise, try the next disjunct.
    s += "\t\t\t\twhichClause++;" + endl + "\t\t\t}";

    //-- If there are no more disjuncts left, return null.
    s += endl + endl + "\t\t\treturn null;" + endl + "\t\t}" + endl + endl;
    
    //-- Implement the toString function
    s += "\t\t@Override"+endl+"\t\tpublic String toString()" + endl + "\t\t{"+endl;
    
    //-- Define toString as the label
    s += "\t\t\treturn \""+label+" "+getSourcePosForToString()+"\";"+endl;
    
    //-- Close the function definition
    s += "\t\t}" + endl;

    //-- Define the 'resetHelper' function.
    s += "\t\tprotected void resetHelper(State state)" + endl + "\t\t{" + endl;

    //-- Implement the 'resetHelper' function by resetting all the disjuncts
    //-- and set the varaible that keeps track of which disjuncts have already
    //-- been considered to 0.
    for (i = 0; i < le.length; i++)
      s += "\t\t\tp[" + i + "].reset(state);" + endl;

    return s + "\t\t\twhichClause = 0;" + endl + "\t\t}" + endl + "\t}" + endl +
           endl;
  }
  
  /** To propagate the variable count to all the logical expressions the
   *  disjunction of which this object represents.
  */
  protected void propagateVarCount(int varCount)
  {
    for (int i = 0; i < le.length; i++)
      le[i].setVarCount(varCount);
  }

  /** This function produces the Java code to create an object of the class
   *  that was implemented to represent this disjunction at run time.
  */
  public String toCode(String label)
  {
    return "new Precondition" + cnt + "(owner, unifier) /*" + label + "*/";
  }


}
