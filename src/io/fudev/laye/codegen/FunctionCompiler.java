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
package io.fudev.laye.codegen;

import io.fudev.laye.ast.*;
import io.fudev.laye.log.DetailLogger;
import io.fudev.laye.struct.FunctionPrototype;

/**
 * @author Sekai Kyoretsuna
 */
public
class FunctionCompiler implements ASTVisitor
{
   public final DetailLogger logger;
   public FunctionPrototypeBuilder builder;
   
   public FunctionCompiler(DetailLogger logger, FunctionPrototypeBuilder parent)
   {
      this.logger = logger;
      builder = new FunctionPrototypeBuilder(parent);
   }
   
   private void handleFunctionData(FunctionData data)
   {
      FunctionCompiler compiler = new FunctionCompiler(logger, builder);
      data.params.forEach(param -> compiler.builder.addParameter(param));
      compiler.builder.vargs = data.vargs;
      
      data.body.accept(compiler);
      
      FunctionPrototype proto = compiler.builder.build();
      builder.opClosure(proto);
   }
   
   @Override
   public void visit(AST ast)
   {
      ast.children.forEach(node -> node.accept(this));
   }
   
   @Override
   public void visit(NodeVariableDef node)
   {
   }
   
   @Override
   public void visit(NodeNullLiteral node)
   {
   }
   
   @Override
   public void visit(NodeIntLiteral node)
   {
   }
   
   @Override
   public void visit(NodeFloatLiteral node)
   {
   }
   
   @Override
   public void visit(NodeStringLiteral node)
   {
      // TODO(sekai): cases like this should get checked when we scan the AST.
      if (node.isResultRequired)
      {
         builder.opCLoad(builder.addConstant(node.value));
      }
   }
   
   @Override
   public void visit(NodePrefixExpression node)
   {
   }
   
   @Override
   public void visit(NodeInfixExpression node)
   {
   }
   
   @Override
   public void visit(NodeScope node)
   {
      builder.startScope();
      node.body.forEach(child -> child.accept(this));
      builder.endScope();
   }
   
   @Override
   public void visit(NodeFunctionDef node)
   {
      builder.defineVariable(node.name);
      handleFunctionData(node.data);
      builder.visitSetVariable(node.name);
      //builder.opPop();
   }
   
   @Override
   public void visit(NodeAssignment node)
   {
   }
   
   @Override
   public void visit(NodeIdentifier node)
   {
      // TODO(sekai): cases like this should get checked when we scan the AST.
      if (node.isResultRequired)
      {
         builder.visitGetVariable(node.value);
      }
   }
   
   @Override
   public void visit(NodeInvoke node)
   {
      node.target.accept(this);
      node.args.forEach(arg -> arg.accept(this));
      builder.opInvoke(node.args.size());
      if (!node.isResultRequired)
      {
         builder.opPop();
      }
   }
   
   @Override
   public void visit(NodeList node)
   {
   }
   
   @Override
   public void visit(NodeTuple node)
   {
   }
   
   @Override
   public void visit(NodeLoadIndex node)
   {
   }
   
   @Override
   public void visit(NodeStoreIndex node)
   {
   }
}
