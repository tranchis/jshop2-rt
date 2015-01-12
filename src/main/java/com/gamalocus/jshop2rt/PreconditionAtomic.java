package com.gamalocus.jshop2rt;

import com.gamalocus.jshop2rt.State.MyIterator;

/** This class represents an iterator over all the possible bindings that can
 *  satisfy an atomic logical expression at run time.
 *
 *  @author Okhtay Ilghami
 *  @author <a href="http://www.cs.umd.edu/~okhtay">http://www.cs.umd.edu/~okhtay</a>
 *  @version 1.0.3
*/
public class PreconditionAtomic extends Precondition
{
  /** The predicate this atomic logical expression represents, after all the
   *  bindings are applied.
  */
  private Predicate boundP;

  /** The iterator this object will use to iterate over the atoms and/or axioms
   *  that can possibly unify with the predicate this object represents.
  */
  private MyIterator e;

  /** The predicate this atomic logical expression represents, without any
   *  subsequent bindings applied to it.
  */
  private final Predicate p;

  /** To initialize this atomic logical expression.
   *
   *  @param pIn
   *          the predicate this atomic logical expression represents.
   *  @param unifier
   *          the current unifier.
  */
  public PreconditionAtomic(Predicate pIn, Term[] unifier)
  {
    //-- An atomic logical expression can be potentially satisfied more than
    //-- once, so the default for the 'isFirstCall' flag is false.
    setFirst(false);

    p = pIn.applySubstitution(unifier);
  }

  /** To bind the assignment logical expression to some binding.
  */
  public void bind(Term[] binding)
  {
    boundP = boundP.applySubstitution(binding);
  }

  /** To return the next satisfier for this atomic logical expression.
  */
  protected Term[] nextBindingHelper(State state)
  {
    return e.nextBinding(boundP);
  }
  
  @Override
  public String toString()
  {
    return "PreconditionAtomic: "+this.p;
  }

  /** To reset this atomic logical expression.
  */
  protected void resetHelper(State state)
  {
    //-- Reset the iterator.
    e = state.iterator(p);

    //-- Forget all the subsequent bindings.
    boundP = p;
  }
}
