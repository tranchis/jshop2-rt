package com.gamalocus.jshop2rt;

import java.util.Iterator;

/** Each list both at compile time and at run time, is an instance of this
 *  class.
 *
 *  @author Okhtay Ilghami
 *  @author <a href="http://www.cs.umd.edu/~okhtay">http://www.cs.umd.edu/~okhtay</a>
 *  @version 1.0.3
*/
public class TermList extends Term implements Iterable<Term>
{
  private static final long serialVersionUID = -6353584488914400906L;

  /** The list this term is representing.
  */
  private final List list;

  /** The <code>NIL</code> term.
  */
  public static TermList NIL = new TermList(null);

  /** To initialize this list term.
   *
   *  @param listIn
   *          the list this term represents.
  */
  public TermList(List listIn)
  {
    list = listIn;
  }

  /** To initialize this list term.
   *
   *  @param headIn
   *          the head of the list this term represents.
   *  @param tailIn
   *          the tail of the list this term represents.
  */
  public TermList(Term headIn, Term tailIn)
  {
    list = new List(headIn, tailIn);
  }

  /** To apply a given binding to the list this term represents.
  */
  public Term bind(Term[] binding)
  {
    if (list != null)
      return new TermList(list.bindList(binding));
    else
      return NIL;
  }

  /** Whether or not another term is equivalent to this list term.
  */
  public boolean equals(Object t)
  {
    //-- 't' is not a list.
    if (!(t instanceof TermList))
      return false;

    //-- Both terms are representing NIL, so they are equal.
    if (list == null && ((TermList)t).list == null)
      return true;

    //-- Only one of the terms represents NIL, so they are not equal.
    if (list == null || ((TermList)t).list == null)
      return false;

    //-- Compare the lists.
    return (list.equals(((TermList)t).list));
  }
  
  @Override
  public int hashCode()
  {
	return list != null ? list.hashCode() : 0;
  }

/** Find a unifier between this list and another given term.
  */
  public boolean findUnifier(Term t, Term[] binding)
  {
    //-- If 't' is a variable symbol, ignore it.
    if (t instanceof TermVariable)
      return true;

    //-- If 't' is not a list, it can not be unified with this term.
    if (!(t instanceof TermList))
      return false;

    //-- If this list represents NIL, the only way to unify 't' with it would
    //-- be for 't' to represent NIL too.
    if (list == null)
      return (((TermList)t).list == null);

    //-- Unify the lists.
    return list.findUnifierList(((TermList)t).list, binding);
  }

  /** To get the list this term represents.
   *
   *  @return
   *          the list this term represents.
  */
  public List getList()
  {
    return list;
  }
  
  public int size()
  {
	  return list != null ? list.size() : 0;
  }

  public Iterator<Term> iterator()
  {
  	return new List.ListIterator(list);
  }

  /** Whether or not there are any variables in the list this term represents.
  */
  public boolean isGround()
  {
    return (list == null || list.isGroundList());
  }

  /** This function produces Java code to create this list term.
  */
  public String toCode(String label)
  {
    if (list == null)
      return "TermList.NIL";

    return "new TermList(" + list.getHead().toCode(label) + ", " +
           list.getTail().toCode(label) + ")";
  }

  /** This function converts this list term to a predicate.
   *
   *  @param varCount
   *          number of variables in the resulting predicate.
   *  @return
   *          the resulting predicate.
  */
  public Predicate toPredicate(int varCount)
  {
    return new Predicate(((TermConstant)list.getHead()).getIndex(), varCount,
                         list.getTail());
  }

  /** This function is used to print this list term.
  */
  @Override
  public String toString()
  {
    if (list == null)
      return "NIL";

    return "(" + list + ")";
  }
}
