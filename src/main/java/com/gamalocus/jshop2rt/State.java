package com.gamalocus.jshop2rt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import com.gamalocus.jshop2rt.Predicate.Namespace;



/** This class is used to represent the current state of the world.
 * 
 * FIXME Implement more of the optimizations suggested in 
 * Russell, Norvig (2003). Artificial Intelligence: a Modern Approach. Ch. 9.
 *
 *  @author Okhtay Ilghami
 *  @author <a href="http://www.cs.umd.edu/~okhtay">http://www.cs.umd.edu/~okhtay</a>
 *  @version 1.0.3
 */
public class State implements Serializable
{
  /** This class implements an iterator with data members that can keep track of
   *  where the algorithm is in terms of bindings found so far so that when the
   *  next binding is needed it can be calculated correctly. This class is needed
   *  because an atom can be satisfied either by looking for bindings at the
   *  current state of the world, or by using an axiom.
   *
   *  @author Okhtay Ilghami
   *  @author <a href="http://www.cs.umd.edu/~okhtay">http://www.cs.umd.edu/~okhtay</a>
   *  @version 1.0.3
  */
  public class MyIterator
  {
    /** The axiom being used right now. If none is used (i.e., we are still
     *  looking for the atom in the current state of the world) the value of
     *  this variable is <code>null</code>.
    */
    private Axiom ax;

    /** When an axiom is being used, this variable holds the binding that unifies
     *  the head of the axiom and the atom being proved.
    */
    private Term[] binding;

    /** Whether or not at least one satisfier has been found for the current
     *  branch of the current axiom. As soon as it becomes <code>true</code>,
     *  further branches of the axiom will not be considered.
    */
    private boolean found;

    /** When looking at the current state of the world, this variable represents
     *  the index of the corresponding <code>Vector</code>, when using an axiom
     *  to prove an atom, this variable represents which branch of that axiom is
     *  being used.
    */
    private int index;

    /** When an axiom is being used, this variable acts as an iterator over all
     *  the possible satisfiers of the precondition of the current branch of the
     *  current axiom.
    */
    private Precondition pre;

    /** The <code>Vector</code> in the current state of the world that represents
     *  the atoms for which we are trying to find satisfiers.
    */
    private final Vector<Term> vec;

    /** Which of the (possibly several) axioms that can be used to prove a
     *  certain atom is being used right now. If none is being used (i.e., we are
     *  still looking for the atom in the current state of the world), it is set
     *  to -1.
    */
    private int whichAxiom;
    
    /**
     * The predicate we are matching.
     */
    private final Predicate p; 

    /** To initialize this iterator.
     *
     *  @param vecIn
     *          The <code>Vector</code> in the current state of the world that
     *          represents the atoms for which we are trying to find satisfiers.
    */
    private MyIterator(Predicate pIn, Vector<Term> vecIn)
    {
      //-- Initially, no axiom is being considered.
      ax = null;

      //-- Reset the index over the Vector of atoms to be considered.
      index = 0;

      //-- Initially, no axiom precondition is being considered.
      pre = null;

      vec = vecIn;

      //-- Initially, no axiom is being considered.
      whichAxiom = -1;
      
      p = pIn;
    }

    /** This function returns the bindings that can satisfy a given precondition
     *  one-by-one.
     *  
     *  This is a convenience function in the case where the predicate is not updated
     *  during matching, i.e. by calling {@link Predicate#applySubstitution(Term[])}.
     *
     *  @param me
     *          the iterator that keeps track of where we are with the satisfiers
     *          so that the next time this function is called, we can take off
     *          where we stopped last time.
     *  @return
     *          the next binding as an array of terms indexed by the indices of
     *          the variable symbols in the given predicate.
     */
    public Term[] nextBinding()
    {
      return nextBinding(p);
    }
    
    /** This function returns the bindings that can satisfy a given precondition
     *  one-by-one.
     *
     *  @param p
     *          the predicate to be satisfied.
     *  @param me
     *          the iterator that keeps track of where we are with the satisfiers
     *          so that the next time this function is called, we can take off
     *          where we stopped last time.
     *  @return
     *          the next binding as an array of terms indexed by the indices of
     *          the variable symbols in the given predicate.
     */
    public Term[] nextBinding(Predicate p)
    {
      Term[] nextB;

      Term[] retVal;

      Term t;

      //-- If we are still looking into the atoms to prove the predicate (i.e.,
      //-- we have not started looking into the axioms),
      if (whichAxiom == -1)
      {
        //-- Iterate over the appropriate Vector to find atoms that can satisfy
        //-- the given predicate.
        // FIXME Speed up by creating map from first argument to predicate and 
        // getting the right predicate(s) in constant time whenever the first 
        // argument in p is ground. 
        while (index < vec.size())
        {
          t = (Term)vec.get(index++);
          retVal = p.findUnifier(t);

          //-- If this atom can satisfy the given predicate, return the binding
          //-- that unifies the two.
          if (retVal != null)
            return retVal;
        }

        //-- We have already looked at all the atoms that could possibly satisfy
        //-- the predicate. From now on, we will look at the axioms only.
        whichAxiom = 0;
      }

      while (true)
      {
        //-- If we need to look at a new axiom,
        while (ax == null)
        {
          //-- If there are no more axioms to be looked at, return null.
          if (p.getHead() >= axioms.length || whichAxiom == axioms[p.getHead()].length)
            return null;

          //-- Try the next axiom whose head matches the head of the given
          //-- predicate.
          ax = axioms[p.getHead()][whichAxiom++];

          //-- Try to unify the axiom's head with the predicate.
          binding = ax.unify(p);

          //-- If the two can not be unified,
          if (binding == null)
            //-- Try to look for the next axiom.
            ax = null;
          else
          {
            //-- Start with the first branch of this axiom.
            index = 0;
            //-- No branch has been satisfied yet, so set this variable to false.
            found = false;
          }
        }

        //-- Iterate on all the branches of this axiom.
        for (; index < ax.getBranchSize(); index++)
        {
          //-- If this is the first time this branch is considered, get the
          //-- iterator for the precondition of this branch.
          if (pre == null)
            pre = ax.getIterator(State.this, binding, index);

          //-- Try the next satisfier for the precondition of this branch of this
          //-- axiom. If there is a next satisfier,
          while ((nextB = pre.nextBinding(State.this)) != null)
          {
            //-- Merge the two bindings.
            Term.merge(nextB, binding);

            //-- Calculate the instance of the axiom we are using.
            Predicate groundAxiomHead = ax.getHead().applySubstitution(nextB);

            //-- Try to unify the axiom and the predicate.
            retVal = p.findUnifier(groundAxiomHead.getParam());

            //-- If there is such unifier, return it.
            if (retVal != null)
            {
              //-- The further branches of this axiom must NOT be considered even
              //-- if this branch fails because there has been at least one
              //-- satisfier for this branch of the axiom. Set this variable to
              //-- true to prevent the further branches of this axiom from being
              //-- considered.
              found = true;

              return retVal;
            }
          }

          //-- Try the next branch of this axiom.
          pre = null;

          //-- According to the semantics of the axiom branches in JSHOP2, second
          //-- branch is considered only when there is no binding for the first
          //-- branch, the third branch is considered only when there is no
          //-- binding for the first and second branches, etc. Therefore, if one
          //-- of the branches of this axiom has already returned a satisfier,
          //-- the other branches should be ignored.
          if (found)
            break;
        }

        //-- Try the next axiom.
        ax = null;
      }
    }
  }
  
  public class SetAndList<T> implements Iterable<T>
  {
    private final HashMap<T, Integer> map;
    private final Vector<T> list;
    
    public SetAndList()
    {
      this.map = new HashMap<T, Integer>();
      this.list = new Vector<T>();
    }

    @SuppressWarnings("unchecked")
    public SetAndList(SetAndList<T> other)
    {
      this.map = (HashMap<T, Integer>) other.map.clone();
      this.list = (Vector<T>) other.list.clone();
    }

    public boolean add(T e)
    {
      if (!map.containsKey(e))
      {
        map.put(e, list.size());
        list.add(e);
        return true;
      }
      return false;
    }

    public int remove(Object o)
    {
      final Integer index = map.remove(o);
      if (index != null)
      {
        // FIXME The indices in the map are not updated on removal. Can be solved
        // by using a linked list with dancing links rather than a vector.
        //final int i = index.intValue();
        final int i = list.indexOf(o);
        list.remove(i);
        return i;
      }
      return -1;
    }

    public List<T> getList()
    {
      return list;
    }
    
    public Vector<T> getVector()
    {
      if (list instanceof Vector)
      {
        return (Vector<T>) list;
      }
      return new Vector<T>(list);
    }

    /**
     * Iterator that keeps set updated on removal.
     */
    public Iterator<T> iterator()
    {
      return new Iterator<T>() {

        private final Iterator<T> listIterator = list.iterator();
        private T lastReturned;
        
        public boolean hasNext()
        {
          return listIterator.hasNext();
        }

        public T next()
        {
          lastReturned = listIterator.next();
          return lastReturned;
        }

        public void remove()
        {
          map.remove(lastReturned);
          listIterator.remove();
        }
      };
    }

    public void add(int i, T e)
    {
      if (!map.containsKey(e))
      {
        map.put(e, i);
        list.add(i, e);
        return;
      }
      
      throw new IllegalArgumentException(String.format("Element %s is already in the list.", e));
    }
 }


  private static final long serialVersionUID = 8760465415287827089L;

  /** The atoms in the current state of the world as an array of
   *  <code>Vector</code>s. The array is indexed by the possible heads (i.e.,
   *  the constant symbol that comes first) of the possible predicates.
   */
  private final Map<Integer, SetAndList<Term>> atoms = 
    new HashMap<Integer, SetAndList<Term>>();
  
  /** Log of elements added during planning. 
   */
  private final Map<Predicate, String> addedAtoms = new HashMap<Predicate, String>();

  /** Log of elements removed during planning. 
   */
  private final Map<Predicate, String> removedAtoms = new HashMap<Predicate, String>();
  
  /**
   * If true, we will log additions and removals.
   */
  private boolean logChanges = false;

  /** The axioms in the domain description as a two-dimensional array. The
   *  array is indexed first by the head of the predicates each axiom can prove
   *  and second by the axioms themselves.
   */
  private final Axiom[][] axioms;

  /** The protections in the current state of the world as an array of
   *  <code>Vector</code>s. The array is indexed by the heads of protected
   *  predicates.
   */
  private final Map<Integer, Vector<NumberedPredicate>> protections = 
    new HashMap<Integer, Vector<NumberedPredicate>>();

  /** To initialize the state of the world.
   *
   *  @param size
   *          the number of possible heads of predicates (i.e., the number of
   *          constant symbols that can come first in a predicate).
   *  @param axiomsIn
   *          the axioms in the domain description as a two-dimensional array.
   *          The array is indexed first by the head of the predicates each
   *          axiom can prove and second by the axioms themselves.
   */
  public State(Axiom[][] axiomsIn)
  {
    axioms = axiomsIn;
  }

  @Override
  public Object clone()
  {
    State copy = new State(axioms);

    for (Entry<Integer, SetAndList<Term>> e : atoms.entrySet())
    {
      copy.atoms.put(e.getKey(), new SetAndList<Term>(e.getValue()));
    }

    for (Entry<Integer, Vector<NumberedPredicate>> e : protections.entrySet())
    {
      copy.protections.put(e.getKey(), new Vector<NumberedPredicate>(e.getValue()));
    }

    return copy;
  }
  
  /**
   * Get description of location of first caller outside of the given border class.
   */
  private static String getCallLocation(Class<?> border)
  {
    StackTraceElement stack[] = new Throwable().getStackTrace();
    final String borderName = border.getName();
    
    // Forward the pointer until we find the border.
    int i;
    for (i = 0; i < stack.length && 
      !stack[i].getClassName().equals(borderName); ++i) {}

    // Forward the pointer past the border.
    for (i = 0; i < stack.length && 
      stack[i].getClassName().equals(borderName); ++i) {}
    
    return i == stack.length ? "unknown position" :
      String.format("%s.%s(%s:%d)", 
          stack[i].getClassName(), 
          stack[i].getMethodName(),
          stack[i].getFileName(),
          stack[i].getLineNumber());
  }

  public boolean isLoggingEnabled()
  {
    return logChanges;
  }

  public void setLoggingEnabled(boolean logChanges)
  {
    this.logChanges = logChanges;
  }

  /**
   * Reset to state just before logging was enabled.
   */
  public void reset()
  {
    // Avoid causing concurrent modifications.
    logChanges = false;
    
    for (Predicate p : addedAtoms.keySet())
    {
      del(p);
    }
    addedAtoms.clear();
    
    for (Predicate p : removedAtoms.keySet())
    {
      add(p);
    }
    removedAtoms.clear();
  }
  
  public ArrayList<String> getModifications(Domain domain)
  {
    ArrayList<String> mods = new ArrayList<String>();
    for (Entry<Predicate, String> e : addedAtoms.entrySet())
    {
      mods.add("+" + e.getKey().toString(domain, Namespace.LOGICAL_PREDICATE) + " at " + e.getValue());
    }
    for (Entry<Predicate, String> e : removedAtoms.entrySet())
    {
      mods.add("-" + e.getKey().toString(domain, Namespace.LOGICAL_PREDICATE) + " at " + e.getValue());
    }
    Collections.sort(mods, new Comparator<String>() {
      public int compare(String o1, String o2)
      {
        return o1.substring(1).compareTo(o2.substring(1));
      }
    });
    return mods;
  }
  
  /**
   * @return Argument lists of all predicates with the given head.
   */
  public Collection<Term> getArguments(int head)
  {
    final SetAndList<Term> tails = atoms.get(head);
    if (tails != null)
    {
      return Collections.unmodifiableCollection(tails.getList());
    }
    else
    {
      return Collections.emptySet();
    }
  }

  /**
   * @return Argument lists of all predicates with the given head.
   */
  public Collection<Term> getArguments(Term head)
  {
    return getArguments(((TermConstant)head).getIndex());
  }

  /** To add a predicate to the current state of the world.
   *
   *  @param p
   *          the predicate to be added.
   *  @return
   *          <code>true</code> if the predicate was added (i.e., it was not
   *          already in the current state of the world), <code>false</code>
   *          otherwise.
   */
  public boolean add(Predicate p)
  {
    //-- Find the right Vector to add this predicate to.
    SetAndList<Term> tails =  atoms.get(p.getHead());
    if (tails == null)
    {
      tails = new SetAndList<Term>();
      atoms.put(p.getHead(), tails);
    }
    
    //-- First look for the predicate in the Vector. If it is already there,
    //-- do nothing and return false.
    //-- Otherwise: Add the predicate and return true.
    final boolean result = tails.add(p.getParam());

    if (logChanges && result && removedAtoms.remove(p) == null)
    {
      addedAtoms.put(p, getCallLocation(State.class));
    }

    return result;
  }

  /** To protect a given predicate in the current state of the world.
   *
   *  @param p
   *          the predicate to be protected.
   *  @return
   *          this function always returns <code>true</code>.
   */
  public boolean addProtection(Predicate p)
  {
    // -- First, find the appropriate Vector to add the protection to.
    Vector<NumberedPredicate> tails =  protections.get(p.getHead());
    if (tails == null)
    {
      tails = new Vector<NumberedPredicate>();
      protections.put(p.getHead(), tails);
    }

    // -- If the predicate is already protected, just increase the protection
    // -- counter.
    for (NumberedPredicate np : tails) {
      if (p.equals(np.getParam())) {
        np.inc();
        return true;
      }
    }

    // -- If this is the first time this predicate is being protected, add it
    //-- to the Vector.
    tails.add(new NumberedPredicate(p));
    return true;
  }

  /** To empty the world state.
   */
  public void clear()
  {
    atoms.clear();
    protections.clear();
  }

  /** To delete a predicate from the current state of the world.
   *
   *  @param p
   *          the predicate to be deleted.
   *  @return
   *          the index of the predicate that was deleted in the
   *          <code>Vector</code> if the predicate was deleted (i.e., it
   *          existed in the current state of the world), -1 otherwise. This
   *          index is used in case of a backtrack to undo this deletion by
   *          inserting the deleted predicate right back where it used to be.
   */
  public int del(Predicate p)
  {
    Term t;

    //-- Find the right Vector to delete this predicate from.
    SetAndList<Term> vec = atoms.get(p.getHead());
    if (vec == null)
    {
      return -1;
    }
    
    //-- If predicate is found, delete it and return its index.
    //-- Otherwise: There was nothing to delete, so return -1.
    final int result = vec.remove(p.getParam());
    
    if (logChanges && result != -1 && addedAtoms.remove(p) == null)
    {
      removedAtoms.put(p, getCallLocation(State.class));
    }

    return result;
  }

  /** To unprotect a given predicate.
   *
   *  @param p
   *          the predicate to be unprotected.
   *  @return
   *          <code>true</code> if the protected is unprotected successfully,
   *          <code>false</code> otherwise (i.e., when the predicate was not
   *          protected before).
   */
  public boolean delProtection(Predicate p)
  {
    NumberedPredicate np;

    //-- First, find the appropriate Vector to delete the protection from.
    Vector<NumberedPredicate> tails = protections.get(p.getHead());
    if (tails == null)
    {
      return false;
    }

    Iterator<NumberedPredicate> e = tails.iterator();

    //-- Look for the protection.
    while (e.hasNext())
    {
      np = e.next();

      //-- If it is found,
      if (p.equals(np.getParam()))
      {
        //-- Decrease the protection counter for this predicate.
        if (!np.dec())
          //-- If the counter drops to zero, remove the protection completely.
          e.remove();

        return true;
      }
    }

    //-- Nothing was there to delete, so return false.
    return false;
  }

  /** To check if a predicate is protected.
   *
   *  @param p
   *          the predicate to be checked.
   *  @return
   *          <code>true</code> if the predicate is protected,
   *          <code>false</code> otherwise.
   */
  public boolean isProtected(Predicate p)
  {
    //-- First, find the appropriate Vector to look for the protection.
    Vector<NumberedPredicate> tails = protections.get(p.getHead());
    if (tails == null)
    {
      return false;
    }

    //-- Iterate over the Vector to find the protection.
    for (NumberedPredicate np : tails)
    {
      if (p.equals(np.getParam()))
        return true;
    }

    return false;
  }

  /** To initialize and return the appropriate iterator when looking
   *  for ways to satisfy a given predicate.
   *
   *  @param head
   *          the index of the constant symbol that is the head of the
   *          predicate (i.e., that comes first in the predicate).
   *  @return
   *          the iterator to be used to find the satisfiers for this
   *          predicate.
   */
  public MyIterator iterator(Predicate p)
  {
    final int head = p.getHead();
    SetAndList<Term> tails = atoms.get(head);
    if (tails == null)
    {
      tails = new SetAndList<Term>();
      atoms.put(head, tails);
    }
    return new MyIterator(p, tails.getVector());
  }

  /** This function is used to print the current state of the world.
   */
  public void print(Domain domain)
  {
    System.out.println(toString(domain));
  }

  public String toString(Domain domain)
  {
    StringBuffer buf = new StringBuffer();
    for (Entry<Integer, SetAndList<Term>> e : atoms.entrySet())
    {
      for (Term t : e.getValue())
      {
        buf.append(new Predicate(e.getKey(), 0, t).toString(domain, Predicate.Namespace.LOGICAL_PREDICATE)).append("\n");
      }
      buf.append("\n");
    }
    buf.append("------\n");

    return buf.toString();
  }

  /**
   * Returns an ArrayList of strings that represents the state. Used in
   * conjunction with JSHOP2GUI (Added 5/28/06)
   * 
   * @return - An ArrayList<String> representing the state
   */
  public ArrayList<String> getState(Domain domain) {
    ArrayList<String> retval = new ArrayList<String>();
    for (Entry<Integer, SetAndList<Term>> e : atoms.entrySet())
    {
      for (Term t : e.getValue())
      {
        retval.add((new Predicate(e.getKey(), 0, t).toString(domain, Namespace.LOGICAL_PREDICATE)));
      }
    }
    return retval;
  }


  /**
   * This function is used, in case of a backtrack, to undo the changes that
   * were made to the current state of the world because of the backtracked
   * decision.
   * 
   * @param delAdd
   *          a 4-member array of type <code>Vector</code>. These four
   *          members are the deleted atoms, the added atoms, the deleted
   *          protections and the added protections respectively.
   */
  public void undo(Vector[] delAdd)
  {
    Iterator e;

    NumberedPredicate np;

    //-- Since when an operator is applied, first the predicates in its delete
    //-- list are deleted and then the predicates in its add list are added,
    //-- when that application is undone, first the added predicates should be
    //-- deleted and then the deleted predicates should be added.

    //-- Deleting the added predicates.
    e = delAdd[1].iterator();
    while (e.hasNext())
      del((Predicate)e.next());

    //-- Adding the deleted predicates, exactly where they were deleted from.
    for (int i = delAdd[0].size() - 1; i >= 0; i--)
    {
      np = (NumberedPredicate)delAdd[0].get(i);

      // Currently, no vectors are deleted. so this is guaranteed 
      // not to cause a NullPointerException.
      atoms.get(np.getHead()).add(np.getNumber(), np.getParam());
      
      if (logChanges && removedAtoms.remove(np.getPredicate()) == null)
      {
        addedAtoms.put(np.getPredicate(), getCallLocation(State.class));
      }
    }

    //-- Deleting the added protections.
    e = delAdd[3].iterator();
    while (e.hasNext())
      delProtection((Predicate)e.next());

    //-- Adding the deleted protections.
    e = delAdd[2].iterator();
    while (e.hasNext())
      addProtection((Predicate)e.next());
  }
}
