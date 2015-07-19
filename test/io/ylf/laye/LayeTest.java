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

import java.io.IOException;

import io.ylf.laye.file.ScriptFile;
import io.ylf.laye.lexical.FileLexer;
import io.ylf.laye.lexical.Location;
import io.ylf.laye.lexical.Token;
import io.ylf.laye.lexical.TokenStream;
import io.ylf.laye.log.DetailLogger;

/**
 * @author Sekai Kyoretsuna
 */
public final class LayeTest
{
   public static void main(String[] args) throws IOException
   {
      DetailLogger logger = new DetailLogger();
      
      // Create all of the objects that we'll need here.
      ScriptFile scriptFile = ScriptFile.fromFile("./examples/hello_world.laye");
      
      FileLexer lexer = new FileLexer(logger);
      
      // Do all of the things!
      TokenStream tokens = lexer.getTokens(scriptFile);
      
      if (logger.getErrorCount() > 0)
      {
         System.err.printf("Token generation failed with %d %s and %d %s.\n",
               logger.getWarningCount(), logger.getWarningCount() == 1 ? "warning" : "warnings",
               logger.getErrorCount(), logger.getErrorCount() == 1 ? "error" : "errors");
         return;
      }
      
      System.out.printf("Code generation completed with %d %s and %d %s.\n",
            logger.getWarningCount(), logger.getWarningCount() == 1 ? "warning" : "warnings",
            logger.getErrorCount(), logger.getErrorCount() == 1 ? "error" : "errors");
      
      System.out.println();
      
      for (Token token : tokens)
      {
         System.out.println(token);
      }
   }
   
   private LayeTest()
   {
   }
}
