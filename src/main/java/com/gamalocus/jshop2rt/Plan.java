package com.gamalocus.jshop2rt;

import java.io.Serializable;
import java.util.LinkedList;

/** This class represent a plan as a <code>LinkedList</code> of ground
 *  instances of operators.
 *
 *  @author Okhtay Ilghami
 *  @author <a href="http://www.cs.umd.edu/~okhtay">http://www.cs.umd.edu/~okhtay</a>
 *  @version 1.0.3
*/
public class Plan implements Serializable
{
  private static final long serialVersionUID = -8781001839646254144L;

  /** The cost of the plan. */
  private final Cost cost;

  /** The new line character in the platform JSHOP2 is running on.
  */
  final static String endl = System.getProperty("line.separator");

  /** The plan as a <code>LinkedList</code> of ground instances of operators.
  */
  private final LinkedList<Predicate> ops;

  /** To initialize the plan to an empty list.
  */
  public Plan(Cost initialCost)
  {
    ops = new LinkedList<Predicate>();
    cost = initialCost;
  }

  /** This function is used by objects of this class to clone themselves.
   *
   *  @param opsIn
   *          the operators in the plan.
   *  @param costIn
   *          the cost of the plan.
  */
  private Plan(LinkedList<Predicate> opsIn, Cost costIn)
  {
    ops = opsIn;
    cost = costIn;
  }

  /** To add an operator instance to the end of the plan.
   *
   *  @param op
   *          the operator the instance of which is being added.
   *  @param binding
   *          the binding to instantiate the operator.
   *  @return
   *          the cost of the operator instance being added.
  */
  public Term addOperator(Operator op, Term[] binding)
  {
    ops.addLast(op.getHead().applySubstitution(binding));
    
    Term groundCostTerm = op.getCost(binding);
	cost.add(groundCostTerm);

    return groundCostTerm;
  }

  /** To clone an object of this class.
  */
  public Object clone()
  {
    return new Plan(new LinkedList<Predicate>(ops), (Cost)cost.clone());
  }

  /** To get the sequence of operators represented by this object.
   *
   *  @return
   *          A <code>LinkedList</code> of operator instances in this plan.
  */
  public LinkedList<Predicate> getOps()
  {
    return ops;
  }

  /** To remove the operator instance at the end of the plan.
   *
   *  @param opCost
   *          the cost of the operator instance to be removed.
  */
  public void removeOperator(Term opCost)
  {
    ops.removeLast();
    cost.remove(opCost);
  }

  /** This function returns a printable <code>String</code> representation of
   *  this plan.
   *
   *  @return
   *          the <code>String</code> representation of this plan.
  */
  public String toString(Domain domain)
  {
    //-- The value to be returned.
    String retVal = "Plan cost: " + cost + endl + endl;

    //-- Iterate over the operator instances in the plan and print them.
    for (Predicate p : ops)
      retVal += p.toString(domain, Predicate.Namespace.PRIMITIVE_TASK_ATOM) + endl;

    return retVal + "--------------------" + endl;
  }

	public Cost getCost()
	{
		return cost;
	}
}
