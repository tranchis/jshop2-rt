package com.gamalocus.jshop2rt.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.gamalocus.jshop2rt.Domain;
import com.gamalocus.jshop2rt.InternalDomain;

/**
 * Helper for generating java code from domain description and then compiling that java code.
 * If used together with a suitable class loader, this is suitable for runtime refresh of 
 * the planning domain.
 * 
 * @author j0rg3n
 */
public class DomainCompiler 
{
  private final static Logger logger = Logger.getLogger(DomainCompiler.class.getName());

  private DomainCompiler() {}

  /**
   * Cause regeneration of domain from domain description.
   * @param inputPath Base of location for the JSHOP2 source code.
   * @param srcOutputPath Base of location for the generated Java code.
   * @param outputPath Base of location for the generated Java class file(s).
   * @throws IOException 
   * @throws TokenStreamException 
   * @throws RecognitionException 
   */
  public static <T extends Domain> void generateJavaClass(File inputPath, 
      Class<T> domainClass, File outputPath, File srcOutputPath) throws Exception
  {
    logger.info(String.format("Class output: %s.", outputPath.getAbsolutePath()));
    File javaSource = generateJavaSource(inputPath, domainClass, srcOutputPath);

    logger.info(String.format("Compiling Java source: %s...", 
        javaSource.getAbsolutePath()));

    // NOTE: The following code requires Java 1.5.
    final Class<?> compiler;
    try
    {
      compiler = Class.forName("com.sun.tools.javac.Main");
    }
    catch (ClassNotFoundException e)
    {
      throw new IOException(String.format("Cannot recompile domain %s: No Java compiler available. " +
          "Hint: Include tools.jar in the classpath.", domainClass.getName()));
    }

    String[] args = new String[]{
        // Verbose output
        "-verbose",  
        // Debugging info
        "-g",     
        // Class output path
        "-d", outputPath.getAbsolutePath(),
        // Source file(s)
        javaSource.getAbsolutePath() };
    
    final StringWriter compilerMessages = new StringWriter();

    Object result = compiler
      .getMethod("compile", String[].class, PrintWriter.class)
      .invoke(null, args, new PrintWriter(compilerMessages));

    int status = ((Integer)result).intValue();
    if (status != 0)
    {
      throw new IOException(compilerMessages.toString());
    }
  }

  public static <T> File generateJavaSource(File inputPath,
      Class<T> domainClass, File outputPath) throws IOException, RecognitionException, TokenStreamException
  {
    logger.info(String.format("Source output: %s.", outputPath.getAbsolutePath()));

    File domainSource = new File(inputPath, domainClass.getName().replace(".", "/"));
    File javaSource = new File(outputPath, domainClass.getName().replace(".", "/") + ".java");

    logger.info(String.format("Compiling JSHOP2 source: %s...", 
        domainSource.getAbsolutePath()));

    // TODO Emit java code as string, and use a SimpleJavaFileObject subclass
    // to wrap it. 
    InternalDomain generator = new InternalDomain(domainSource, javaSource, 
        domainClass.getPackage().getName());

    generator.getParser().domain();

    FileWriter out = null;
    try
    {
      // Make sure the output path exists.
      javaSource.getParentFile().mkdirs();
      out = new FileWriter(javaSource);
      out.write(generator.getOutput());
    }
    finally
    {
      if (out != null)
      {
        out.close();
      }
    }
    
    logger.info(String.format("Wrote Java source file: %s.", javaSource.getAbsolutePath()));

    return javaSource;
  }
}
