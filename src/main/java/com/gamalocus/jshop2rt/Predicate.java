package com.gamalocus.jshop2rt;

import java.io.Serializable;

/** Each predicate, both at compile time and at run time, is an instance of
 *  this class.
 *
 *  @author Okhtay Ilghami
 *  @author <a href="http://www.cs.umd.edu/~okhtay">http://www.cs.umd.edu/~okhtay</a>
 *  @version 1.0.3
*/
public class Predicate extends CompileTimeObject implements Serializable
{
  private static final long serialVersionUID = 8536248072640628513L;

  /**
   * Name space that the head belongs to. Used for string constant lookup.
   */
  public enum Namespace
  {
	  PRIMITIVE_TASK_ATOM,
	  COMPOUND_TASK_ATOM,
	  LOGICAL_PREDICATE
  }

  /** The index of the constant symbol that is the head of this predicate.
  */
  private final int head;

  /** The argument list of this predicate.
  */
  private final Term param;

  /** The number of variables in this predicate. Used to return a binding of
   *  the appropriate size.
  */
  private int varCount;

  /** In case this predicate is a variable symbol and not a real atom, this
   *  represents its index. Otherwise, its value will be -1.
  */
  private final int varIdx;
  
  /** Temporary string used to "save" the name of the predicate for {@link #toString()}
   */
  private String asString = null;

  /**
   * A unique ID to identify this predicate.  Used in conjunction with JSHOP2GUI to allow
   * it to discern exactly which task atom is being referenced at every step of the plan
   * finding process.
   */
  private static int staticID = 0;

  private final int ID;
  
  /** To initialize this predicate.
   *
   *  @param headIn
   *          the head of the predicate.
   *  @param varCountIn
   *          number of the variables of the predicate.
   *  @param paramIn
   *          the argument list of the predicate.
  */
  public Predicate(int headIn, int varCountIn, Term paramIn)
  {
    head = headIn;
    varCount = varCountIn;
    param = paramIn;

    //-- This is a real predicate, so set 'varIdx' to -1.
    varIdx = -1;
    ID = staticID++;
  }

  /** To initialize this predicate when it is not a real predicate but a
   *  variable symbol.
   *
   *  @param varIdxIn
   *          the index of the variable symbol that represents this predicate.
   *  @param varCountIn
   *          number of the variables of the predicate.
  */
  public Predicate(int varIdxIn, int varCountIn)
  {
    head = -1;
    varCount = varCountIn;
    param = TermList.NIL;

    varIdx = varIdxIn;
    
    // Not real, so no real ID.
    ID = -1;
  }

  /** To apply a binding to this predicate.
   *
   *  @param bindings
   *          the binding to be applied.
   *  @return
   *          the resulting predicate.
  */
  public Predicate applySubstitution(Term[] bindings)
  {
    //-- If this predicate is a variable symbol,
    if (isVar())
    {
      //-- If the variable symbol is not bound to anything, just return this
      //-- predicate itself.
      if (bindings[varIdx] == null)
        return this;

      //-- Find out the list this variable symbol is mapped to and make up the
      //-- predicate out of that list.
      return ((TermList)bindings[varIdx]).toPredicate(varCount);
    }

    //-- Apply the binding and return the result.
    return new Predicate(head, varCount, param.bind(bindings));
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + head;
    result = prime * result + param.hashCode();
    return result;
  }

  /** Whether or not the argument list of another predicate is equal to the
   *  the argument list of this predicate.
   *
   *  @param t
   *          the argument list of the other predicate.
   *  @return
   *          <code>true</code> if the two argument lists are equal,
   *          <code>false</code> otherwise.
   */
  @Override
  public boolean equals(Object t)
  {
    if (isVar())
      return false;

    if (t instanceof Term)
    {
      return param.equals(t);
    }
    else if (t instanceof Predicate)
    {
      return head == ((Predicate)t).head &&
        param.equals(((Predicate)t).param);
    }
    return false;
  }

  /** To find a unifier that unifies the argument list of another predicate
   *  with the argument list of this predicate.
   *
   *  @param t
   *          the argument list of the other predicate.
   *  @return
   *          the binding that unifies the two argument lists in case they are
   *          unifiable, <code>null</code> otherwise.
  */
  public Term[] findUnifier(Term t)
  {
    //-- First, make an empty binding of the appropriate size.
    Term[] retVal = new Term[varCount];

    //-- If this predicate is a variable, just map the variable to the whole
    //-- argument list and return.
    if (isVar())
    {
      retVal[varIdx] = t;
      return retVal;
    }

    if (param.findUnifier(t, retVal))
      return retVal;

    return null;
  }

  /** To get the head of this predicate.
   *
   *  @return
   *          the head of this predicate.
  */
  public int getHead()
  {
    //-- TODO: What to do when Predicate is a variable?
    return head;
  }

  /** To get the parameter list of this predicate.
   *
   *  @return
   *          the parameter list of this predicate.
  */
  public Term getParam()
  {
    //-- TODO: What to do when Predicate is a variable?
    return param;
  }

  /** To get the number of variables for this predicate.
   *
   *  @return
   *          the number of variables for this predicate.
  */
  public int getVarCount()
  {
    return varCount;
  }

  /** To check if this predicate is ground (i.e., has no variables).
   *
   *  @return
   *          <code>true</code> if this predicate is ground, <code>false</code>
   *          otherwise.
  */
  public boolean isGround()
  {
    if (isVar())
      return false;

    return param.isGround();
  }

  /** To check if this predicate is a variable symbol or a real predicate.
   *
   *  @return
   *          <code>true</code> if this predicate is a variable symbol,
   *          <code>false</code> if it is a real predicate.
  */
  public boolean isVar()
  {
    return (varIdx != -1);
  }

  /** To set the number of variables for this predicate.
   *
   *  @param varCountIn
   *          the number of variables for this predicate.
  */
  public void setVarCount(int varCountIn)
  {
    varCount = varCountIn;
  }

  /** This function produces Java code to create this predicate.
  */
  public String toCode(String label)
  {
    if (isVar())
      return "new Predicate(" + varIdx + ", " + varCount + ")";

    return "new Predicate(" + head + ", " + varCount + ", " + param.toCode(label) + ")";
  }

  /** This function returns a printable <code>String</code> representation of
   *  this predicate. This function is used to print the predicates known to be
   *  logical atoms, because the indexes in this predicate representing
   *  constant symbols are mapped back by default to <code>String</code>
   *  representations of the constant symbols in logical atoms.
   *  
   *  FIXME Let the predicate store its own name and name-space.
   *
   *  @param 
   *          constants Depends on whether the predicate is a 
   *          primitive task atom, a compound task atom, or simply a predicate.
   *          The constants should be {@link Domain#primitiveTasks}, 
   *          {@link Domain#compoundTasks} and {@link Domain#constants}, 
   *          respectively.
   *
   *  @return
   *          the <code>String</code> representation of this predicate.
  */
  public String toString(Domain domain, Namespace namespace)
  {
    //-- If this predicate is a variable symbol, just print it as a variable.
    if (isVar())
      return asString = "VAR" + varIdx;

    String headName = "";
    switch (namespace)
    {
    case COMPOUND_TASK_ATOM:
    	headName = domain.compoundTasks[head];
    	break;
    case PRIMITIVE_TASK_ATOM:
    	headName = domain.primitiveTasks[head];
    	break;
    case LOGICAL_PREDICATE:
    	headName = domain.getTermConstant(head).toString();
    	break;
    }
    
	//-- Find out what the String representation of the head of this predicate
    //-- is.
    String s = "(" + headName;

    //-- If the argument list is a list term (which it should be usually):
    if (param instanceof TermList)
      if (param.isNil())
        //-- Converting (a . NIL) to (a).
        return  asString = s + ")";
      else
        //-- Converting (a . (b)) to (a b).
        return asString = s + " " + ((TermList)param).getList().toString() + ")";
    //-- If the argument list is not a list term (which should not happen
    //-- usually, but there is no reason to assume that it will not happen.
    else
      return asString = s + "." + param + ")";
  }

  /**
   * Returns the unique identifier for this predicate
   * @return
   *  an integer representing the unique ID
   */
  public int getID() {
    return ID;
  }
  
  @Override
  public String toString()
  {
    if(asString != null)
      return asString;
    return "["+this.getClass().getSimpleName()+":"+head+"]";
  }
}
