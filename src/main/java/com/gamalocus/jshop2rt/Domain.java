package com.gamalocus.jshop2rt;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/** Each domain at run time is represented as a class derived from this
 *  abstract class.
 *  
 *  FIXME Initialize most variables through constructor and tag them as final.
 *
 *  @author Okhtay Ilghami
 *  @author <a href="http://www.cs.umd.edu/~okhtay">http://www.cs.umd.edu/~okhtay</a>
 *  @version 1.0.3
 */
public abstract class Domain implements Serializable
{
  /** The axioms in this domain. The array is indexed by first the
   *  predicate each axiom can prove, and second the order the axioms that
   *  prove the same predicate appear in the domain description.
   */
  protected Axiom[][] axioms;

  /** The <code>String</code> names of compound tasks that appear in the
   *  domain description. These <code>String</code>s are only used to print the
   *  task lists, since the compound tasks are mapped to integers at compile
   *  time. The same integers are used to index this array.
   */
  public String[] compoundTasks;

  /** The <code>String</code> names of constant symbols that appear in the
   *  domain description. These <code>String</code>s are only used to print the
   *  constant symbols, since the constant symbols are mapped to integers at
   *  compile time. The same integers are used to index this array.
   */
  public String[] constants;

  /** The methods in this domain. The array is indexed by first the compound
   *  task each method can decompose, and second the order the methods that
   *  decompose the same compound task appear in the domain description.
   */
  protected Method[][] methods;

  /** The operators in this domain. The array is indexed by first the primitive
   *  task each operator can achieve, and second the order the operators that
   *  achieve the same primitive task appear in the domain description.
   */
  public Operator[][] ops;

  /** The <code>String</code> names of primitive tasks that appear in the
   *  domain description. These <code>String</code>s are only used to print the
   *  task lists, since the primitive tasks are mapped to integers at compile
   *  time. The same integers are used to index this array.
   */
  public String[] primitiveTasks;

  /** To represent the constant symbols that we already know exist, so that
   *  there will be no duplicate copies of those symbols. In other words, all
   *  constant symbols that represent the same thing in different places point
   *  to the corresponding element in this array at run time.
   *
   *  The <code>String</code> names of constant symbols that appear in the
   *  problem description are stored within the constant symbols themselves. 
   *  
   *  These <code>String</code>s are only used to print
   *  the constant symbols, since the constant symbols are mapped to integers
   *  at compile time. The same integers are used to index this map.
   *  
   *  Note: This was rewritten from an array to a map, to support a 
   *  dynamically changing set of constants.
   *  
   *  The constants with names from the constants array will always be 
   *  present in this map.
   */
  protected final Map<Integer, TermConstant> termConstants = 
    new HashMap<Integer, TermConstant>();

  /**
   * Convenience table for lookup by name.
   */
  protected final Map<String, TermConstant> termConstantsByName = 
    new HashMap<String, TermConstant>();

  /** To represent the variable symbols that we know occur in the domain
   *  description, so that there will be no duplicate copies of those symbols.
   *  In other words, all variable symbols that represent the same thing in
   *  different places point to the corresponding element in this array at run
   *  time.
   */
  private TermVariable[] termVariables;

  /**
   * Counter for unique constant ids.
   */
  private int maxTermConstantIndex = 0;

  /** To return the correponding existing variable symbol.
   *
   *  @param index
   *          the index of the variable symbol to be returned.
   *  @return
   *          the corresponding existing variable symbol.
   */
  public TermVariable getTermVariable(int index)
  {
    return termVariables[index];
  }

  /** To initialize an array of variable symbols that we know occur in the
   *  domain description, so that there will be no duplicate copies of those
   *  symbols. In other words, all variable symbols that represent the same
   *  thing in different places point to the corresponding element in this
   *  array at run time.
   *
   *  @param size
   *          the number of existing variable symbols.
   */
  public void initializeTermVariables(int size)
  {
    termVariables = new TermVariable[size];

    for (int i = 0; i < size; i++)
      termVariables[i] = new TermVariable(i);
  }

  /** To return the correponding existing constant symbol.
   *
   *  @param index
   *          the index of the constant symbol to be returned.
   *  @return
   *          the corresponding existing constant symbol, or <code>null</code> if no such symbol exists.
   */
  public TermConstant getTermConstant(int index)
  {
    TermConstant t = termConstants.get(index);
    if (t == null)
    {
      throw new NoSuchElementException(String.format("No constant with index %d.", index));
    }
    return t;
  }

  public Term getTermConstant(String name)
  {
    TermConstant t = termConstantsByName.get(name);
    if (t == null)
    {
      throw new NoSuchElementException(String.format("No constant with name %s.", name));
    }
    return t;
  }
  
  /** To initialize an array of constant symbols that we already know exist, so
   *  that there will be no duplicate copies of those symbols. In other words,
   *  all constant symbols that represent the same thing in different places
   *  point to the corresponding element in this array at run time.
   *
   *  @param size
   *          the number of existing constant symbols.
   */
  protected void initializeTermConstants()
  {
    for (int i = 0; i < constants.length; i++)
    {
      addConstant(constants[i]);
    }
  }

  /** Initialize constant symbols from different domain instance.
   * FIXME Should be available on construction, to unify the {@link TermConstant} objects between 
   * the two domain instances.
   */
  public void initializeTermConstants(Domain other)
  {
    termConstants.clear();
    termConstantsByName.clear();
    for (TermConstant t : other.termConstants.values())
    {
      termConstants.put(t.getIndex(), t);
      termConstantsByName.put(t.getName(), t);
    }
  }

  /**
   * Expands the list of problem constants by adding the given name.
   */
  public int addConstant(String name)
  {
    // Make sure it doesn't already exist.
    TermConstant t = termConstantsByName.get(name);
    if (t != null)
    {
      return t.getIndex();
    }

    int i = maxTermConstantIndex++;
    termConstants.put(i, new TermConstant(i, name));
    termConstantsByName.put(name, new TermConstant(i, name));
    return i;
  }

  /**
   * Removes the given constant from the list of problem constants.
   * If the constant is a domain constant, nothing happens.
   * 
   * @return <code>true</code> if the constant was removed, 
   * <code>false</code> if nothing happened.
   */
  public boolean removeConstant(int index)
  {
    if (index < constants.length)
    {
      return false;
    }

    final TermConstant t = termConstants.remove(index);
    if (t != null)
    {
      if (termConstantsByName.remove(t.getName()) != t)
      {
        throw new AssertionError(String.format("Constant %s existed only in index table.", 
            t.getName()));
      }
      return true;
    }

    throw new NoSuchElementException(String.format("No constant with index %d.", index));
  }

  /** This function returns the axioms in this domain.
   *
   *  @return
   *          the axioms in this domain.
   */
  public Axiom[][] getAxioms()
  {
    return axioms;
  }

  /** This function returns the <code>String</code> representation of a given
   *  constant symbol that appears in the domain description, the problem
   *  description, or both.
   *
   *  @param idx
   *          the integer equivalent of the constant symbol.
   *  @return
   *          the <code>String</code> representation of the constant symbol.
   */
  public String getConstant(int idx)
  {
    TermConstant t = termConstants.get(idx);
    if (t == null)
    {
      throw new NoSuchElementException(String.format("No constant with index %d.", idx));
    }
    return t.getName();
  }

  /**
   * Returns all defined termconstants. 
   */
  public Collection<TermConstant> getTermConstants()
  {
    return Collections.unmodifiableCollection(termConstants.values());
  }

  /** This function returns an array of the <code>String</code> representations
   *  of all the primitive tasks in the domain description. This list is used
   *  to print the plans after they are found.
   *
   *  @return
   *          the array of <code>String</code> representations of the primitive
   *          tasks in the domain.
   */
  public String[] getPrimitiveTasks()
  {
    return primitiveTasks;
  }

  public String getPrimitiveTaskName(Operator o)
  {
    return primitiveTasks[o.getHead().getHead()];
  }

  public String getCompoundTaskName(Method m)
  {
    return compoundTasks[m.getHead().getHead()];
  }

  public int getPrimitiveTaskIndex(String primitiveTaskName)
  {
    for (int i = 0; i < primitiveTasks.length; ++i)
    {
      if (primitiveTasks[i].equals(primitiveTaskName))
      {
        return i;
      }
    }
    return -1;
  }

  /**
   * Get all methods of compound task with given name.
   */
  public Method[] getMethods(String compoundTaskName)
  {
    final int i = getCompoundTaskIndex(compoundTaskName);
    if (i == -1)
    {
      throw new NoSuchElementException(String.format("No composite task with name %s.", compoundTaskName));
    }
    return methods[i];
  }

  public int getCompoundTaskIndex(String compoundTaskName)
  {
    for (int i = 0; i < compoundTasks.length; ++i)
    {
      if (compoundTasks[i].equals(compoundTaskName))
      {
        return i;
      }
    }
    return -1;
  }
}
