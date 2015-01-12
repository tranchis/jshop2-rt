package com.gamalocus.jshop2rt;

import antlr.Token;

/** All the objects at compile time are instances of classes that are derived
 *  from this abstract class.
 *
 *  @author Okhtay Ilghami
 *  @author <a href="http://www.cs.umd.edu/~okhtay">http://www.cs.umd.edu/~okhtay</a>
 *  @version 1.0.3
*/
public abstract class CompileTimeObject
{
  /** The line in the domain source file where this object starts.
   */
  protected int lineFrom = -1;
  
  /** The column in the domain source file where this object starts.
   */
  protected int columnFrom = -1;
  
  /** The line in the domain source file where this object ends.
   */
  protected int lineTo = -1;
  
  /** The column in the domain source file where this object ends.
   */
  protected int columnTo = -1;
  
  /** The new line character in the platform JSHOP2 is running on.
  */
  final static String endl = System.getProperty("line.separator");

  /** This abstract function produces the Java code needed to implement this
   *  compile time element.
   * @param label 
   *            Descriptive label for the element, as seen from its parent. Used for comments.
   *            For small elements, this makes no sense, and <code>null</code> may be passed.
   *  @return
   *          the produced code as a <code>String</code>.
  */
  public abstract String toCode(String label);
  
  public void setSourcePos(int lineFrom, int columnFrom, int lineTo, int columnTo)
  {
    this.lineFrom = lineFrom;
    this.columnFrom = columnFrom;
    this.lineTo = lineTo;
    this.columnTo = columnTo;
  }
  
  public void setSourcePos(Token lineFrom, Token lineTo)
  {
  	setSourcePos(lineFrom.getLine(), lineFrom.getColumn(), lineTo.getLine(), lineTo.getColumn());
  }
  
  public void setSourcePos(CompileTimeObject atomChild)
  {
	  setSourcePos(atomChild.lineFrom, atomChild.columnFrom, atomChild.lineTo, atomChild.columnTo);
  }

  public String getSourcePosForComment()
  {
    if(lineFrom != -1)
    {
       return "Source: (line:"+lineFrom+", col:"+columnFrom+") to (line:"+lineTo+", col:"+columnTo+")";
    }
    return "The source position was not specified. (class: "+this.getClass().getCanonicalName()+")";
  }
  
  public String getSourcePosForToString()
  {
    if(lineFrom != -1)
    {
      return "[line:"+lineFrom+","+columnFrom+" to "+lineTo+","+columnTo+"]";
    }
    return "[unknown source pos]";
  }
}
