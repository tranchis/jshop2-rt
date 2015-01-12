package com.gamalocus.jshop2rt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gamalocus.jshop2rt.Predicate.Namespace;



/** This class represents all the variables that JSHOP2 needs every time it
 *  calls itself recursively. The reason all these variables are bundled
 *  together in one class rather than having them locally defined is to save
 *  stack space. Right now, the only thing that is stored in the stack is a
 *  pointer to this class in each recursion, and the actual data is stored in
 *  heap memory, while if these variables were just locally defined, all of
 *  them would be stored in the stack, resulting in very fast stack overflow
 *  errors.
 *
 *  @author Okhtay Ilghami
 *  @author <a href="http://www.cs.umd.edu/~okhtay">http://www.cs.umd.edu/~okhtay</a>
 *  @version 1.0.3
 */
class InternalVars implements Serializable
{
  private static final long serialVersionUID = 7569871051224688453L;

  /** The binding that unifies the head of a method or an operator with the
   *  task being achieved.
   */
  Term[] binding;

  /** An array of size 4 to store the atoms and protections that are being
   *  deleted or added to the current state of the world as a result of
   *  application of an operator, to be used in case of a backtrack over that
   *  operator.
   */
  Vector<?>[] delAdd;

  /** The iterator iterating over the <code>LinkedList</code> of the tasks
   *  that we have the option to achieve right now.
   */
  Iterator<TaskList> e;

  /** Whether or not at least one satisfier has been found for the current
   *  branch of the current method. As soon as it becomes <code>true</code>,
   *  further branches of the method will not be considered.
   */
  boolean found;

  /** The index of the method or operator being considered.
   */
  int j;

  /** The index of the branch of the current method being considered.
   */
  int k;

  /** An array of methods that can achieve the compound task being
   *  considered.
   */
  Method[] m;

  /** Next binding that satisfies the precondition of the current method or
   *  operator.
   */
  Term[] nextB;

  /** An array of operators that can achieve the primitive task being
   *  considered.
   */
  Operator[] o;

  /** An iterator over the bindings that can satisfy the precondition of the
   *  current method or operator.
   */
  Precondition p;

  /** The task atom chosen to be achieved next.
   */
  TaskAtom t;

  /** A <code>LinkedList</code> of the task atoms we have the option to
   *  achieve right now.
   */
  LinkedList<TaskList> t0;

  /** The atomic task list that represents, in the task network, the task
   *  atom that has been chosen to be achieved next.
   */
  TaskList tl;

  /** Cost of the operator that we're currently trying to apply.
   */
  Term cost;
}

/** This class is the implementation of the JSHOP2 algorithm.
 *
 * FIXME Change {@link JSHOP2#findPlans(TaskList, int, Cost)} into a constructor,
 * and mark a lot of fields as final.
 *
 *  @author Okhtay Ilghami
 *  @author <a href="http://www.cs.umd.edu/~okhtay">http://www.cs.umd.edu/~okhtay</a>
 *  @version 1.0.3
 */
public class JSHOP2 implements Serializable
{
  /**
   * State of stack frame. Corresponds to PC in regular stack frames.
   */
  private enum PC
  {
    A,
    A_1_V_T0_SIZE_IS_0,
    A_1_3_V_CHOSENTASK_IS_TASKS,
    B, A_1_1_V_CHOSENTASK_IS_NOT_TASKS, A_1_2,
    C_1_V_E_HASNEXT__, D, C_WHILE_V_E_HASNEXT__, 
    C_1_1, C_1_4, C_2, C_1_2_1, C_1_3, C_1_2_FOR_V_J___0__V_J___V_O_LENGTH__V_J___, 
    C_1_2_1_2_V_BINDING_IS_NULL, C_1_2_1_1_V_BINDING_IS_NOT_NULL, C_1_2_2_NEXT_V_J___0__V_J___V_O_LENGTH__V_J___, 
    C_1_2_1_2_WHILE_V_P_NEXTBINDING_STATE_IS_NOT_NULL, C_1_2_1_2_1_V_P_NEXTBINDING_STATE_IS_NOT_NULL, 
    C_1_2_1_3, C_1_2_1_2_1_1_V_O__V_J__APPLY_V_NEXTB__STATE__V_DELADD_, C_1_2_1_2_2, 
    C_1_2_1_2_1_3, C_1_2_1_2_1_2_1, C_1_2_1_2_1_2, C_1_5_FOR_V_J___0__V_J___V_M_LENGTH__V_J___, 
    C_1_5_1, C_1_6, C_1_5_1_1_BINDING_IS_NOT_NULL, C_1_5_2, C_1_5_1_3_BINDING_IS_NULL, 
    C_1_5_1_2_FOR_V_K___0___V_K___V_M_V_J__GETSUBS____LENGTH____NOT_V_FOUND__V_K__, C_1_5_1_2_1, 
    C_1_5_1_3, C_1_5_1_2_2_1_V_NEXTB___V_P_NEXTBINDING_STATE__IS_NOT_NULL, C_1_5_1_2_3_NEXT_V_K___0___V_K___V_M_V_J__GETSUBS____LENGTH____NOT_V_FOUND__V_K__, C_1_5_1_2_1_1_1, C_1_5_1_2_1_2, A_3, A_2, C_1_5_1_2_2_WHILE_V_NEXTB___V_P_NEXTBINDING_STATE__IS_NULL
  };

  /**
   * Stack frame.
   */
  private static class Frame extends InternalVars 
  {
    private static final long serialVersionUID = 6952881772902487869L;

    /**
     * Program counter (PC).
     */
    PC pc;

    /**
     * Parameter: Chosen task.
     */
    final TaskList chosenTask;

    /**
     * Return value.
     */
    Boolean lastCallResult;
    
    /**
     * If true, this is a leaf node, either a succesful plan, or something that could
     * not be built further upon.
     */
    boolean leaf;

    /**
     * String representation of the current task list.
     * This is for logging, as {@link InternalVars#tl} is shared between
     * the stack frames.
     */
    public String tlString;

    public Frame(TaskList chosenTask)
    {
      leaf = true;
      this.pc = PC.A;
      this.chosenTask = chosenTask;
      this.lastCallResult = null;
    }

    public String toString(Domain domain)
    {
      return String.format("PC=%s, j=%s, k=%s, tl=%s, e=%s",
          pc, j, k, tlString, e);
    }
  }
  
  private static class Stats implements Comparable<Stats>
  {
    final String val;
    int failed;
    int succeeded;
    Stats(String val)
    {
    	this.val = val;
    }
	public int compareTo(Stats o)
	{
      int ret = (failed+succeeded)-(o.failed+o.succeeded);
      if(ret == 0)
        return val.compareTo(o.val);
      return ret;
	}
  }

  private static final long serialVersionUID = 274536180602188365L;

  /**
   * We add the identity hash code to the logger name to be able to differentiate the output of several
   * planners running simultaneously.
   */
  private final Logger logger = 
    Logger.getLogger(String.format("%s.%08x", getClass().getName(), System.identityHashCode(this)));

  /**
   * Stack frames for the planner time slice.
   */
  private final Stack<Frame> stack = new Stack<Frame>();
  
  /** The plan currently being constructed.
   */
  private Plan currentPlan;

  /** The domain description for the planning problem.
   */
  private final Domain domain;

  /**
   * Maximum recursion depth
   */
  private final int recursionLimit;

  /** The plans are stored in this variable as a list of type
   *  <code>Plan</code>.
   */
  private final LinkedList<Plan> plans = new LinkedList<Plan>();
  
  /** The current state of the world.
   */
  private final State state;

  /** The task list to be achieved.
   */
  private final TaskList tasks;

  /** A boolean indicating if we should collect info about what preconditions fail and succeed.
   */
  private boolean registerFailAndSuccess = true;
  
  /** A summary of failures/successes
   */
  private Hashtable<String, Stats> summary = new Hashtable<String, Stats>();

  /** This function finds plan(s) for a given initial task list.
   * 
   * Currently found plans may be found by calling {@link #getPlans()}.
   * 
   * You must call {@link #run()} repeatedly to actually find plans.
   * 
   * FIXME Remove plan count limit: The user can do that himself 
   * by monitoring {@link #getPlans()}.
   *
   *  @param tasksIn
   *          the initial task list to be achieved.
   *  @param recursionLimitIn
   *              the maximum recursion level. This is to avoid infinite recursion. 
   *  @param initialCostIn 
   *          Cost object describing the initial cost of the plan. Must also take care of conversion
   *          from Term to internal representation.
   *  @param domainIn
   *          the planning domain.
   *  @param stateIn
   *          the initial state of the world.
   */
  public JSHOP2(TaskList tasksIn, int recursionLimitIn, Cost initialCostIn, Domain domainIn, State stateIn)
  {
    domain = domainIn;
    state = stateIn;

    //-- Initialize the current plan to an empty one.
    currentPlan = new Plan(initialCostIn);

    //-- Initialize the current task list to be achieved.
    tasks = tasksIn;

    //-- Initialize the recursion level to 0
    recursionLimit = recursionLimitIn;

    logSetGoalTasks();

    //-- Initiate the stack.
    stack.push(new Frame(tasks));
  }
  
  public boolean isActive()
  {
    return !stack.isEmpty();
  }

  public LinkedList<Plan> getPlans()
  {
    return plans;
  }
  
  /**
   * Run a single time slice.
   * @return <code>true</code> if there are more slices left.
   */
  public boolean run()
  {
    try
    {
      state.setLoggingEnabled(true);
      return runInternal();
    }
    finally
    {
      state.setLoggingEnabled(false);
    }
  }
  
  /**
   * Run a single time slice.
   * 
   * FIXME Time-slicing implementation is inefficient. Re-implement as 
   * explicitly time-sliced with basis in the algorithm as described 
   * in the original SHOP2 paper. 
   * 
   * @return <code>true</code> if there are more slices left.
   */
  private boolean runInternal()
  {
    if (stack.isEmpty())
    {
      return false;
    }
    else if (stack.size() >= recursionLimit)
    {
      StringBuffer buf = new StringBuffer();
      buf.append("Recursion limit exceeded.");
      
      if (logger.isLoggable(Level.FINEST))
      {
        buf.append(" Stack trace:");
        int level = stack.size();
        for (Frame f : stack)
        {
          buf.append("\n\t").append(level).append(": ").append(f.toString(domain));
          level--;
        }
      }
      logger.warning(buf.toString());
      
      state.reset();
      
      return false;  
    }
    
    //-- The local variables we need every time this function is called.
    final Frame v = stack.peek();
    
    switch (v.pc)
    {
    case A:
      //-- Find all the tasks that we have the option to achieve right now. This
      //-- equals to the first task in the current task list if it is ordered, or
      //-- the first task in all the subtasks of the current task list if it is
      //-- unordered. In the latter case, if there is an immediate task as the
      //-- first task of any of the subtasks, that immediate task and ONLY that
      //-- immediate task is returned.
      v.t0 = v.chosenTask.getFirst();

      //-- If there are no tasks left,
      _next(v.t0.size() == 0 ? PC.A_1_V_T0_SIZE_IS_0 : PC.B);
      // FIXME Don't break on first case, just fall through.
      break;

      //if (v.t0.size() == 0)
      //{
      case A_1_V_T0_SIZE_IS_0:
        //-- If the chosen task is not the whole task network the algorithm is
        //-- initially set to achieve, it means we have just achieved that task,
        //-- and not the whole task network. Therefore, try to achieve the rest
        //-- of the task network.
        _next(v.chosenTask != tasks ? PC.A_1_1_V_CHOSENTASK_IS_NOT_TASKS : PC.A_1_3_V_CHOSENTASK_IS_TASKS);
        break;
        
        //if (v.chosenTask != tasks)
        //{
        case A_1_1_V_CHOSENTASK_IS_NOT_TASKS:
          // (... call result ...) = findPlanHelper(tasks)
          v.tlString = "[next task]";
          _call(tasks, PC.A_1_2);
          break;
          
        case A_1_2:
          // return (... call result ...)
          _return(v.lastCallResult);
          break;
        //}
        //else
        //{
        case A_1_3_V_CHOSENTASK_IS_TASKS: 
          //-- Otherwise, add the current plan to the list of the plans for the
          //-- given task network. Note that in the case where we are looking for
          //-- more than one plan, we add a clone of the current plan to the list
          //-- rather than the current plan itself since the current plan will be
          //-- changed during the look for other plans.
          //if (planNo != 1) {
            plans.addLast((Plan)currentPlan.clone());
          //} else {
            //plans.addLast(currentPlan);
          //}
  
          if (logger.isLoggable(Level.FINE))
          {
            logger.fine(String.format("%d plans found, latest with cost %s, %d actions.",
                plans.size(), currentPlan.getCost().toString(), currentPlan.getOps().size()));
          }
  
          logPlanFoundStep();
  
          _return(true);
          break;
        //}
      //}
      
    case B:
      //-- This array of size 4 will store the atoms and protections that are
      //-- deleted from and added to the current state of the world as a result
      //-- of an operator being applied. This information is used in case a
      //-- backtrack happens over that operator to store the state of the world
      //-- to what it was before the backtracked operator was applied.
      v.delAdd = new Vector[4];

      //-- To iterate over the tasks we have the option to achieve right now.
      v.e = v.t0.iterator();

    case C_WHILE_V_E_HASNEXT__:
      //-- For each of the tasks that we have the option to achieve right now,
      _next(v.e.hasNext() ? PC.C_1_V_E_HASNEXT__ : PC.D);
      break;
      
      //while (v.e.hasNext())
      //{
      case C_1_V_E_HASNEXT__: 
        //-- Find the next option.
        v.tl = v.e.next();
        
        if (logger.isLoggable(Level.FINER))
        {
          v.tlString = v.tl.toString(domain);
        }
        
        v.t = v.tl.getTask();

        //-- Create a TRYING step for the list of plan steps
        logTryingStep(v);

        _next(v.t.isPrimitive() ? PC.C_1_1 : PC.C_1_4);
        break;
        
        //-- If that task is primitive,
        //if (v.t.isPrimitive())
        //{
        case C_1_1:
          //-- Remove the task from the task list, by replacing it with an empty
          //-- task list.
          v.tl.replace(TaskList.empty);

          //-- Find all the operators that achieve this primitive task.
          v.o = domain.ops[v.t.getHead().getHead()];
          v.j = 0; 

        case C_1_2_FOR_V_J___0__V_J___V_O_LENGTH__V_J___:
          _next(v.j < v.o.length ? PC.C_1_2_1 : PC.C_1_3);
          break;
          
          //-- For each of these operators,
          //for (v.j = 0; v.j < v.o.length; v.j++)
          //{
          case C_1_2_1: 
            //-- Find the binding that unifies the head of the operator with the
            //-- task.
            v.binding = v.o[v.j].unify(v.t.getHead());

            _next(v.binding != null ? PC.C_1_2_1_1_V_BINDING_IS_NOT_NULL : PC.C_1_2_1_2_V_BINDING_IS_NULL);
            break;
            
            //-- If there is such bindings,
            //if (v.binding != null)
            //{
            case C_1_2_1_1_V_BINDING_IS_NOT_NULL:
              if (logger.isLoggable(Level.FINEST))
              {
                logger.finest(String.format("Binding for predicate %s " +
                    "with operator %s (%s):\n\t%s", 
                    v.t.getHead().toString(domain, Namespace.PRIMITIVE_TASK_ATOM),
                    v.o[v.j].getClass().getSimpleName(),
                    v.o[v.j].getHead().toString(domain, Namespace.PRIMITIVE_TASK_ATOM),
                    toString(v.binding)));
              }

              //-- Get the iterator that iterates over all the bindings that can
              //-- satisfy the precondition for this operator.
              v.p = v.o[v.j].getIterator(state, v.binding, 0);

            case C_1_2_1_2_WHILE_V_P_NEXTBINDING_STATE_IS_NOT_NULL:
              _next((v.nextB = v.p.nextBinding(state)) != null ? 
                  PC.C_1_2_1_2_1_V_P_NEXTBINDING_STATE_IS_NOT_NULL :
                  PC.C_1_2_1_3);
              break;
              
              //-- For each such binding,
              //while ((v.nextB = v.p.nextBinding(state)) != null)
              //{
              case C_1_2_1_2_1_V_P_NEXTBINDING_STATE_IS_NOT_NULL:
                //-- Merge the two bindings.
                Term.merge(v.nextB, v.binding);

                _next(v.o[v.j].apply(v.nextB, state, v.delAdd) ? 
                    PC.C_1_2_1_2_1_1_V_O__V_J__APPLY_V_NEXTB__STATE__V_DELADD_ : 
                    PC.C_1_2_1_2_2);
                
                //-- If the operator is applicable, apply it, and,
                //if (v.o[v.j].apply(v.nextB, state, v.delAdd))
                //{
                case C_1_2_1_2_1_1_V_O__V_J__APPLY_V_NEXTB__STATE__V_DELADD_:
                  //-- Add the instance of the operator that achieved this task
                  //-- to the beginning of the plan, remembering how much it
                  //-- cost.
                  v.cost = currentPlan.addOperator(v.o[v.j], v.nextB);
                  if (logger.isLoggable(Level.FINEST))
                  {
                    logger.finest(String.format("=== %d: Adding cost %s: Total is %s.", 
                        stack.size(), v.cost, currentPlan.getCost()));
                  }

                  // TODO Branch and bound: Compare the plan cost to the current lower bound, 
                  // and stop if below. 

                  //-- Create a STATECHANGED step for the list of plan steps
                  logStateChangedStep(v);

                  // (... call result ...) = findPlanHelper(tasks)
                  _call(tasks, PC.C_1_2_1_2_1_2);
                  break;
                  
                case C_1_2_1_2_1_2:
                  //-- Remove the operator from the current plan.
                  if (logger.isLoggable(Level.FINEST))
                  {
                    logger.finest(String.format("=== %d: Removing cost %s: Total is %s.", 
                        stack.size(), v.cost, currentPlan.getCost()));
                  }
                  currentPlan.removeOperator(v.cost);
                  if (logger.isLoggable(Level.FINEST))
                  {
                    logger.finest(String.format("=== %d: Removed cost %s: Total is %s.", 
                        stack.size(), v.cost, currentPlan.getCost()));
                  }
                //}
                  
              case C_1_2_1_2_2:
                //-- Undo the changes that were the result of applying this
                //-- operator, because we are backtracking here.
                state.undo(v.delAdd);
                
                // Jump to head of while loop
                _next(PC.C_1_2_1_2_WHILE_V_P_NEXTBINDING_STATE_IS_NOT_NULL);
                break;
              //}
                
            case C_1_2_1_3:                
              _next(PC.C_1_2_2_NEXT_V_J___0__V_J___V_O_LENGTH__V_J___);
              break;                
            //}
            //else
            //{
            case C_1_2_1_2_V_BINDING_IS_NULL:
              if (logger.isLoggable(Level.FINEST))
              {
                logger.finest(String.format("No binding find for predicate %s " +
                    "with operator %s (%s).", 
                    v.t.getHead().toString(domain, Namespace.LOGICAL_PREDICATE),
                    v.o[v.j].getClass().getSimpleName(),
                    v.o[v.j].getHead().toString(domain, Namespace.PRIMITIVE_TASK_ATOM)));
              }
            //}
          case C_1_2_2_NEXT_V_J___0__V_J___V_O_LENGTH__V_J___:
            // Jump to head of for statement
            v.j++;
            _next(PC.C_1_2_FOR_V_J___0__V_J___V_O_LENGTH__V_J___);
            break;
          //}
            
       case C_1_3: 
          //-- Insert the task we chose to achieve first back where it was,
          //-- because we couldn't achieve it.
          v.tl.undo();
          _next(PC.C_2);
          break;
        //}
        //-- If that task is compound,
        //else
        //{
      case C_1_4:
          //-- Find all the methods that decompose this compound task.
          v.m = domain.methods[v.t.getHead().getHead()];
          v.j = 0;

      case C_1_5_FOR_V_J___0__V_J___V_M_LENGTH__V_J___:
          _next(v.j < v.m.length ? PC.C_1_5_1 : PC.C_1_6);
          break;
          
          //-- For each of these methods,
          //for (v.j = 0; v.j < v.m.length; v.j++)
          //{
          case C_1_5_1:
            //-- Find the binding that unifies the head of the method with the
            //-- task.
            v.binding = v.m[v.j].unify(v.t.getHead());

            _next(v.binding != null ? PC.C_1_5_1_1_BINDING_IS_NOT_NULL : PC.C_1_5_1_3_BINDING_IS_NULL);
            break;
            
            //-- If there is such binding,
            //if (v.binding != null)
            //{
            case C_1_5_1_1_BINDING_IS_NOT_NULL: 
              if (logger.isLoggable(Level.FINEST))
              {
                logger.finest(String.format("Binding for predicate %s " +
                    "with method %s (%s):\n\t%s", 
                    v.t.getHead().toString(domain, Namespace.COMPOUND_TASK_ATOM),
                    v.m[v.j].getClass().getSimpleName(),
                    v.m[v.j].getHead().toString(domain, Namespace.COMPOUND_TASK_ATOM),
                    toString(v.binding)));
              }

              //-- Initially, precondition of no branch of this method has
              //-- already been satisfied, so set this variable to false.
              v.found = false;
              v.k = 0;

            case C_1_5_1_2_FOR_V_K___0___V_K___V_M_V_J__GETSUBS____LENGTH____NOT_V_FOUND__V_K__:
              _next((v.k < v.m[v.j].getSubs().length) && !v.found ? PC.C_1_5_1_2_1 : PC.C_1_5_1_3);
              break;
              
              //-- Iterate on all the branches of this method. note the use of
              //-- 'v.found' in the condition for the 'for' loop. It is there
              //-- because of the semantics of the method branches in JSHOP2:
              //-- Second branch is considered only when there is no binding for
              //-- the first branch, the third branch is considered only when
              //-- there is no binding for the first and second branches, etc.
              //for (v.k = 0; (v.k < v.m[v.j].getSubs().length) && !v.found; v.k++)
              //{
              case C_1_5_1_2_1:
                //-- Get the iterator that iterates over all the bindings that
                //-- can satisfy the precondition for this branch of this method.
                v.p = v.m[v.j].getIterator(state, v.binding, v.k);

              case C_1_5_1_2_2_WHILE_V_NEXTB___V_P_NEXTBINDING_STATE__IS_NULL:
            	v.nextB = v.p.nextBinding(state);
            	if(registerFailAndSuccess)
            	{
	                if(v.nextB == null)
	                {
	                  registerPreconditionFailure(v.p);
	                }
	                else
	                {
	                  registerPreconditionSuccess(v.p);
	                }
            	}
                _next(v.nextB != null ? 
                    PC.C_1_5_1_2_2_1_V_NEXTB___V_P_NEXTBINDING_STATE__IS_NOT_NULL : 
                    PC.C_1_5_1_2_3_NEXT_V_K___0___V_K___V_M_V_J__GETSUBS____LENGTH____NOT_V_FOUND__V_K__);
                break;
                
                //-- For each such binding,
                //while ((v.nextB = v.p.nextBinding(state)) != null)
                //{
                case C_1_5_1_2_2_1_V_NEXTB___V_P_NEXTBINDING_STATE__IS_NOT_NULL:
                  //-- Merge the two bindings.
                  Term.merge(v.nextB, v.binding);

                  //-- Replace the decomposed task in task list with its
                  //-- decomposition according to this branch of this method.
                  v.tl.replace(v.m[v.j].getSubs()[v.k].bind(v.nextB));

                  //-- Create a REDUCED step for the list of plan steps
                  logReducedStep(v);

                  // (... call result ... ) = findPlanHelper(v.tl)
                  _call(v.tl, PC.C_1_5_1_2_1_2);
                  break;
                  
                case C_1_5_1_2_1_2:
                  //-- The further branches of this method must NOT be considered
                  //-- even if this branch fails because there has been at least
                  //-- one satisfier for this branch of the method. Set this
                  //-- variable to true to prevent the further branches of this
                  //-- method from being considered.
                  v.found = true;

                  //-- Undo the changes in the task list, because this particular
                  //-- decomposition failed.
                  v.tl.undo();
                  
                  // Jump to head of while loop
                  _next(PC.C_1_5_1_2_2_WHILE_V_NEXTB___V_P_NEXTBINDING_STATE__IS_NULL);
                  break;
                  
                //}
                
              case C_1_5_1_2_3_NEXT_V_K___0___V_K___V_M_V_J__GETSUBS____LENGTH____NOT_V_FOUND__V_K__:
                // Jump to head of for loop
                v.k++;
                _next(PC.C_1_5_1_2_FOR_V_K___0___V_K___V_M_V_J__GETSUBS____LENGTH____NOT_V_FOUND__V_K__);
                break;
              //}
                
            case C_1_5_1_3:
              _next(PC.C_1_5_2);
              break;
              
            //}
            //else
            //{
            case C_1_5_1_3_BINDING_IS_NULL:
              if (logger.isLoggable(Level.FINEST))
              {
                logger.finest(String.format("No binding find for predicate %s " +
                    "with method %s (%s).", 
                    v.t.getHead().toString(domain, Namespace.LOGICAL_PREDICATE),
                    v.m[v.j].getClass().getSimpleName(),
                    v.m[v.j].getHead().toString(domain, Namespace.COMPOUND_TASK_ATOM)));
              }
            //}
              
          case C_1_5_2:
            // Jump to head of for loop
            v.j++;
            _next(PC.C_1_5_FOR_V_J___0__V_J___V_M_LENGTH__V_J___);
            break;
          //}
            
        case C_1_6:
        //}

      case C_2:
        //-- Create a BACKTRACKING step for the list of plan steps
        logBacktrackingStep(v);
        
        //--- Log the failed, partial plan.
        logFailure(v);

        // Loop to head of while block.
        _next(PC.C_WHILE_V_E_HASNEXT__);
        break;
      //}
        
    case D:
      //-- Return false, because all the options were tried and none worked.
      _return(false);
      break;
    }
    
    return !stack.isEmpty();
  }


  private void registerPreconditionFailure(Precondition p)
  {
    Stats s = getSummary(p.toString()+"(bound: "+p.bestMatch+" conditions)");
    s.failed++;
  }
  
  private void registerPreconditionSuccess(Precondition p)
  {
    Stats s = getSummary(p.toString());
    s.succeeded++;
  }
  
  private Stats getSummary(String s)
  {
    Stats stats = summary.get(s);
    if(stats == null)
    {
      stats = new Stats(s);
      summary.put(s, stats);
    }
    return stats;
  }
  
  public void clearSummary()
  {
    summary.clear();
  }
  
  public void printSummary()
  {
    TreeSet<Stats> sorted = new TreeSet<Stats>();
    sorted.addAll(summary.values());
    for(Stats s : sorted)
    {
    	System.out.println(String.format("%4d", s.succeeded)+" of "+String.format("%4d", s.succeeded+s.failed)+" : "+s.val);
    }
  }

  /**
   * Modify stack to simulate a function call.
   * 
   * @param param The parameter passed to the function
   * @param pc Point of return, i.e. corresponding to the PC value just 
   * after the call instruction.
   */
  private void _call(TaskList param, PC pc)
  {
    stack.peek().pc = pc;
    
    // We made a call, this is no longer considered a leaf.
    stack.peek().leaf = false;
    stack.push(new Frame(param));
  }

  /**
   * Modify stack frame to simulate increase in the program counter.
   * 
   * Use this just before exiting the previous section.
   * 
   * @param pc New program counter value.
   */
  private void _next(PC pc)
  {
    stack.peek().pc = pc;
  }

  /**
   * Modify stack to simulate a <code>return</code> statement.
   * 
   * Should be followed by a <code>break</code>;
   * 
   * @param result Return value.
   */
  private void _return(boolean result)
  {
    stack.pop();
    
    if (!stack.isEmpty())
    {
      Frame top = stack.peek();
      top.lastCallResult = result;
    }
    else
    {
      // Stack completely unwound.
      // The return value is only used within recursion, so we do nothing here.
    }
  }

  public static String toString(Term[] terms)
  {
    StringBuffer buf = new StringBuffer();
    for (Term term : terms)
    {
      if (buf.length() != 0)
      {
        buf.append(" ");
      }
      buf.append(term != null ? term.toString() : "?");
    }
    return buf.toString();
  }
  
  private void logPlanStep(PlanStepInfo info)
  {
    if (logger.isLoggable(Level.FINEST))
    {
      final String msg = String.format("Plan step:\n\t%s", 
          info.toString(domain).replace("\n", "\n\t"));
      
      logger.finest(msg);
    }
  }

  private void logSetGoalTasks()
  {
    if (logger.isLoggable(Level.FINEST))
    {
      PlanStepInfo newStep = new PlanStepInfo();
      newStep.action = "SETGOALTASKS";
      newStep.children = tasks.subtasks;
      newStep.ordered = tasks.isOrdered();
      logPlanStep(newStep);
    }
  }
  
  private void logFailure(Frame v)
  {
    final Level level = Level.FINER;
    if (v.leaf && logger.isLoggable(level))
    {
      StringBuffer buf = new StringBuffer();
      buf.append("Longest plan before backtracking:");
      
      final int max = 80;
      int lineLen = buf.length();
      for (Frame frame : stack)
      {
        final String s = frame.tlString != null ? frame.tlString : "null";
        final int wordLen = s.length();
        if (lineLen != 0)
        {
          if (lineLen + wordLen > max)
          {
            buf.append("\n\t");
            lineLen = 0;
          }
          else
          {            
            buf.append(" ");
          }
        }
        buf.append(s);
        lineLen += wordLen;

        if (frame.cost != null)
        {
          // It's the same cost object, so any reference will do.
          buf.append("\n\t\tcost: ").append(frame.cost).append("\n\t");
          lineLen = 0;
        }        
      }
      buf.append("\n");

      final ArrayList<String> mods = state.getModifications(domain);
      if (!mods.isEmpty())
      {
        buf.append("\n");      
        for (String mod : mods)
        {
          buf.append("\n\t");
          buf.append(mod);
        }
        buf.append("\n");
      }
      
      logger.log(level, buf.toString());
    }
  }


  private void logBacktrackingStep(Frame v)
  {
    if (logger.isLoggable(Level.FINEST))
    {
      PlanStepInfo newStep = new PlanStepInfo();
      newStep.action = "BACKTRACKING";
      newStep.taskAtom = v.t;
      logPlanStep(newStep);
    }
  }

  private void logReducedStep(InternalVars v)
  {
    if (logger.isLoggable(Level.FINEST))
    {
      PlanStepInfo newStep = new PlanStepInfo();
      newStep.action = "REDUCED";
      newStep.taskAtom = v.t;
      newStep.children = v.tl.subtasks;
      newStep.ordered = v.m[v.j].getSubs()[v.k].isOrdered();
      newStep.method = v.m[v.j].getLabel(v.k);
      logPlanStep(newStep);
    }
  }

  private void logStateChangedStep(InternalVars v)
  {
    if (logger.isLoggable(Level.FINEST))
    {
      PlanStepInfo newStep = new PlanStepInfo();
      newStep.action = "STATECHANGED";
      newStep.taskAtom = v.t;
      newStep.delAdd = v.delAdd;
      newStep.operatorInstance = v.o[v.j].getHead()
      .applySubstitution(v.nextB).toString(getDomain(), Predicate.Namespace.PRIMITIVE_TASK_ATOM);
      logPlanStep(newStep);
    }
  }

  private void logTryingStep(InternalVars v)
  {
    if (logger.isLoggable(Level.FINEST))
    {
      PlanStepInfo newStep = new PlanStepInfo();
      newStep.action = "TRYING";
      newStep.state = state.getModifications(domain);
      newStep.taskAtom = v.t;
      logPlanStep(newStep);
    }
  }

  private void logPlanFoundStep()
  {
    if (logger.isLoggable(Level.FINEST))
    {
      PlanStepInfo newStep = new PlanStepInfo();
      newStep.planFound = true;
      logPlanStep(newStep);
    }
  }

  /**
   * This function returns the planning domain.
   * 
   * @return the current planning domain.
   */
  public Domain getDomain()
  {
    return domain;
  }

  /** This function returns the current state of the world.
   *
   *  @return
   *          the current state of the world.
   */
  public State getState()
  {
    return state;
  }
}
