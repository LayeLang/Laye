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
package io.ylf.laye;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;

import io.ylf.laye.analyze.SemanticAnalyzer;
import io.ylf.laye.ast.AST;
import io.ylf.laye.debug.ASTViewer;
import io.ylf.laye.file.ScriptFile;
import io.ylf.laye.lexical.Lexer;
import io.ylf.laye.lexical.TokenStream;
import io.ylf.laye.log.DetailLogger;
import io.ylf.laye.parse.Parser;
import io.ylf.laye.symbol.SymbolTable;

/**
 * @author Sekai Kyoretsuna
 */
public final 
class LayeTest
{
   public static void main(String[] args) throws IOException
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
      SemanticAnalyzer analyzer = new SemanticAnalyzer(logger);

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
      
      // ===== Perform semantic analysis
      
      SymbolTable symbols = analyzer.analyze(ast);
      
      if (logger.getErrorCount() > 0)
      {
         logger.flush();
         System.err.printf("Semantic analysis failed with %d %s and %d %s.\n",
               logger.getWarningCount(), logger.getWarningCount() == 1 ? "warning" : "warnings",
               logger.getErrorCount(), logger.getErrorCount() == 1 ? "error" : "errors");
         return;
      }
      
      // Finished! :D
      
      logger.flush();
      
      System.out.printf("Code generation completed with %d %s and %d %s.\n",
            logger.getWarningCount(), logger.getWarningCount() == 1 ? "warning" : "warnings",
            logger.getErrorCount(), logger.getErrorCount() == 1 ? "error" : "errors");
      
      System.out.println();
      viewer.visit(ast);
   }
   
   private LayeTest()
   {
   }
}
