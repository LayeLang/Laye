/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Sekai Kyoretsuna
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.fudev.laye;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;

import io.fudev.laye.ast.AST;
import io.fudev.laye.codegen.FunctionCompiler;
import io.fudev.laye.debug.ASTViewer;
import io.fudev.laye.file.ScriptFile;
import io.fudev.laye.lexical.Lexer;
import io.fudev.laye.lexical.TokenStream;
import io.fudev.laye.log.DetailLogger;
import io.fudev.laye.parse.Parser;
import io.fudev.laye.struct.FunctionPrototype;
import io.fudev.laye.vm.LayeClosure;
import io.fudev.laye.vm.LayeFunction;
import io.fudev.laye.vm.LayeObject;
import io.fudev.laye.vm.LayeVM;

/**
 * @author Sekai Kyoretsuna
 */
public final 
class LayeTest
{
   public static void main(String[] unused) throws IOException
   {
      // Create output directory
      File outputFolder = new File("./output");
      outputFolder.mkdir();
      
      // Clear our output directory:
      File[] outputFiles = outputFolder.listFiles();
      if (outputFiles != null && outputFiles.length != 0)
      {
         Arrays.asList(outputFiles).forEach(file -> file.delete());
      }
      
      // Get the time as a string
      final ZonedDateTime time = ZonedDateTime.now();
      final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("u-M-d HH.mm.ss.SSS")
            .toFormatter();
      
      final String timeString = time.format(formatter);
      
      // Create new files for info/errors
      File infoDest = new File(String.format("./output/info_log %s.txt", timeString));
      File errorDest = new File(String.format("./output/error_log %s.txt", timeString));

      PrintStream infoPrintStream = new PrintStream(infoDest);
      PrintStream errorPrintStream = new PrintStream(errorDest);
      
      DetailLogger logger = new DetailLogger(infoPrintStream, errorPrintStream);
      
      // Create all of the objects that we'll need here.
      ScriptFile scriptFile = ScriptFile.fromFile("./examples/test.laye", "UTF-16");
      
      Lexer lexer = new Lexer(logger);
      Parser parser = new Parser(logger);

      FunctionCompiler compiler = new FunctionCompiler(logger, null);
      LayeVM vm = new LayeVM();
      
      ASTViewer viewer = new ASTViewer(System.out);
      
      // Do all of the things!
      
      // ==== Lex the input file
      
      TokenStream tokens = lexer.getTokens(scriptFile);
      
      if (logger.getErrorCount() > 0)
      {
         logger.flush();
         System.err.printf("Token generation failed with %d %s and %d %s.\n",
               logger.getWarningCount(), logger.getWarningCount() == 1 ? "warning" : "warnings",
               logger.getErrorCount(), logger.getErrorCount() == 1 ? "error" : "errors");
         return;
      }
      
//      tokens.forEach(System.out::println);
//      System.out.println();
      
      // ===== Parse the tokens
      
      AST ast = parser.getAST(tokens);
      
      if (logger.getErrorCount() > 0)
      {
         logger.flush();
         System.err.printf("Syntax tree generation failed with %d %s and %d %s.\n",
               logger.getWarningCount(), logger.getWarningCount() == 1 ? "warning" : "warnings",
               logger.getErrorCount(), logger.getErrorCount() == 1 ? "error" : "errors");
         return;
      }
      
      // ===== Compile the program
      
      ast.accept(compiler);
      FunctionPrototype proto = compiler.builder.build();
      LayeClosure closure = new LayeClosure(proto);
      
      // Finished! :D
      
      logger.flush();
      
      System.out.printf("Code generation completed with %d %s and %d %s.\n\n",
            logger.getWarningCount(), logger.getWarningCount() == 1 ? "warning" : "warnings",
            logger.getErrorCount(), logger.getErrorCount() == 1 ? "error" : "errors");
      
//      viewer.visit(ast);

      vm.state.store("PrintLn", new LayeFunction((__, thisObject, args) ->
      {
         StringBuilder result = new StringBuilder();
         for (int i = 0; i < args.length; i++)
         {
            if (i > 0)
            {
               result.append(' ');
            }
            result.append(args[i]);
         }
         System.out.println(result.toString());
         return LayeObject.NULL;
      }));
      vm.invoke(closure, null);
      LayeObject main = vm.state.load("Main");
      vm.invoke(main, null);
   }
   
   private LayeTest()
   {
   }
}
