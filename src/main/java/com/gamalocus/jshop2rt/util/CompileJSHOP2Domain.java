package com.gamalocus.jshop2rt.util;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import antlr.RecognitionException;
import antlr.TokenStreamException;

/**
 * ANT task to ease automatic generation of domain Java code 
 * as part of an existing build process.
 * 
 * @author j0rg3n
 */
public class CompileJSHOP2Domain extends Task
{
  private File srcdir;
  private Class<?> domainClass;
  private File destdir;

  public void setSrcdir(File srcdir)
  {
    this.srcdir = srcdir;
  }

  public void setDomainClass(Class<?> domainClass)
  {
    this.domainClass = domainClass;
  }

  public void setDestdir(File destdir)
  {
    this.destdir = destdir;
  }
  
  @Override
  public void execute() throws BuildException
  {
    try
    {
      DomainCompiler.generateJavaSource(srcdir, domainClass, destdir);
    }
    catch (RecognitionException e)
    {
      throw new BuildException(e);
    }
    catch (TokenStreamException e)
    {
      throw new BuildException(e);
    }
    catch (IOException e)
    {
      throw new BuildException(e);
    }
  }

}
