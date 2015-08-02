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
package io.fudev.laye.api;

import java.io.IOException;

import io.fudev.laye.ast.AST;
import io.fudev.laye.codegen.FunctionCompiler;
import io.fudev.laye.file.ScriptFile;
import io.fudev.laye.kit.KitLaye;
import io.fudev.laye.lexical.Lexer;
import io.fudev.laye.lexical.TokenStream;
import io.fudev.laye.log.DetailLogger;
import io.fudev.laye.parse.Parser;
import io.fudev.laye.process.ASTProcessor;
import io.fudev.laye.struct.FunctionPrototype;
import io.fudev.laye.vm.LayeClosure;
import io.fudev.laye.vm.LayeObject;
import io.fudev.laye.vm.LayeVM;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Sekai Kyoretsuna
 */
public
class LayeScript
{
   private final LayeVM vm;
   
   private @Getter @Setter DetailLogger logger;
   
   public LayeScript()
   {
      this(new DetailLogger());
   }
   
   public LayeScript(DetailLogger logger)
   {
      this.logger = logger;
      vm = new LayeVM();
      vm.state.registerKit("Laye", new KitLaye(vm));
      vm.state.useAll("Laye");
   }

   private void logDetails(ScriptFile file, String name)
   {
      if (logger.getErrorCount() > 0)
      {
         logger.flush();
         System.err.printf("%s (for %s) failed with %d %s and %d %s.\n", name, file.path,
               logger.getWarningCount(), logger.getWarningCount() == 1 ? "warning" : "warnings",
               logger.getErrorCount(), logger.getErrorCount() == 1 ? "error" : "errors");
         System.exit(1);
      }
   }
   
   public LayeClosure compile(ScriptFile file) throws IOException
   {
      Lexer lexer = new Lexer(logger);
      Parser parser = new Parser(logger);
      ASTProcessor processor = new ASTProcessor(logger);

      FunctionCompiler compiler = new FunctionCompiler(logger, null);
      
      // ==== Lex the input file
      TokenStream tokens = lexer.getTokens(file);
      logDetails(file, "Token generation");
      
      // ===== Parse the tokens
      AST ast = parser.getAST(tokens);
      logDetails(file, "Syntax tree generation");
      
      // ===== TODO(sekai): Semantic Analysis
      
      // ===== Final AST processing
      ast = processor.process(ast);
      logDetails(file, "AST processing");
      
      // ===== Compile the program
      ast.accept(compiler);
      FunctionPrototype proto = compiler.builder.build();
      
      logger.flush();
      return(new LayeClosure(proto));
   }
   
   public LayeObject invoke(LayeClosure closure)
   {
      vm.invoke(closure, null);
      LayeObject main = vm.state.load("Main");
      if (main != LayeObject.NULL)
      {
         return(vm.invoke(main, null));
      }
      return(LayeObject.NULL);
   }
   
   public LayeObject doFile(ScriptFile file) throws IOException
   {
      return(invoke(compile(file)));
   }
}
