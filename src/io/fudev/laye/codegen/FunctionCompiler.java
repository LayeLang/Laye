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
import io.fudev.laye.log.LogMessageID;
import io.fudev.laye.struct.FunctionPrototype;
import io.fudev.laye.struct.Identifier;

/**
 * @author Sekai Kyoretsuna
 */
public
class FunctionCompiler implements IASTVisitor
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
      for (int i = 0; i < node.names.size(); i++)
      {
         Identifier name = node.names.get(i);
         builder.defineVariable(name);
         node.values.get(i).accept(this);
         builder.visitSetVariable(name);
         if (i < node.names.size() - 1 || !node.isResultRequired)
         {
            builder.opPop();
         }
      }
   }
   
   @Override
   public void visit(NodeNullLiteral node)
   {
      if (node.isResultRequired)
      {
         builder.opNLoad();
      }
   }
   
   @Override
   public void visit(NodeBoolLiteral node)
   {
      if (node.isResultRequired)
      {
         builder.opBLoad(node.value);
      }
   }
   
   @Override
   public void visit(NodeIntLiteral node)
   {
      if (node.isResultRequired)
      {
         builder.opILoad(node.value);
      }
   }
   
   @Override
   public void visit(NodeFloatLiteral node)
   {
      if (node.isResultRequired)
      {
         builder.opFLoad(node.value);
      }
   }
   
   @Override
   public void visit(NodeStringLiteral node)
   {
      if (node.isResultRequired)
      {
         builder.opCLoad(builder.addConstant(node.value));
      }
   }
   
   @Override
   public void visit(NodePrefixExpression node)
   {
      node.expression.accept(this);
      builder.opPrefix(node.operator);
      if (!node.isResultRequired)
      {
         builder.opPop();
      }
   }
   
   @Override
   public void visit(NodeInfixExpression node)
   {
      node.left.accept(this);
      node.right.accept(this);
      builder.opInfix(node.operator);
      if (!node.isResultRequired)
      {
         builder.opPop();
      }
   }
   
   @Override
   public void visit(NodeScope node)
   {
      builder.startScope();
      node.body.forEach(child -> child.accept(this));
      builder.endScope();
      // NOTE: the last node in the scope should handle the isResultRequired for us.
   }
   
   @Override
   public void visit(NodeFunctionDef node)
   {
      builder.defineVariable(node.name);
      handleFunctionData(node.data);
      builder.visitSetVariable(node.name);
      builder.opPop();
   }
   
   @Override
   public void visit(NodeAssignment node)
   {
      if (node.left instanceof NodeIdentifier)
      {
         node.right.accept(this);
         builder.visitSetVariable(((NodeIdentifier)node.left).value);
      }
      else if (node.left instanceof NodeLoadIndex)
      {
         NodeLoadIndex left = ((NodeLoadIndex)node.left);
         left.target.accept(this);
         left.index.accept(this);
         node.right.accept(this);
         builder.opStoreIndex();
      }
      else
      {
         logger.logErrorf(node.location, LogMessageID.ERROR_INVALID_ASSIGNMENT,
               "invalid assignment left side %s.", node.left.getClass().getSimpleName());
      }
      if (!node.isResultRequired)
      {
         builder.opPop();
      }
   }
   
   @Override
   public void visit(NodeIdentifier node)
   {
      builder.visitGetVariable(node.value);
   }
   
   @Override
   public void visit(NodeInvoke node)
   {
      if (node.target instanceof NodeLoadIndex)
      {
         NodeLoadIndex load = (NodeLoadIndex)node.target;
         load.target.accept(this);
         load.index.accept(this);
         node.args.forEach(arg -> arg.accept(this));
         builder.opInvokeMethod(node.args.size());
      }
      else
      {
         node.target.accept(this);
         node.args.forEach(arg -> arg.accept(this));
         builder.opInvoke(node.args.size());
      }
      if (!node.isResultRequired)
      {
         builder.opPop();
      }
   }
   
   @Override
   public void visit(NodeList node)
   {
      node.values.forEach(element -> element.accept(this));
      builder.opList(node.values.size());
      // NOTE: We could just not generate the list, but function calls can reside in them.
      if (!node.isResultRequired)
      {
         builder.opPop();
      }
   }
   
   @Override
   public void visit(NodeTuple node)
   {
      node.values.forEach(element -> element.accept(this));
      builder.opTuple(node.values.size());
      // NOTE: We could just not generate the list, but calls/assignments can reside in them.
      if (!node.isResultRequired)
      {
         builder.opPop();
      }
   }
   
   @Override
   public void visit(NodeLoadIndex node)
   {
      node.target.accept(this);
      node.index.accept(this);
      builder.opLoadIndex();
   }
   
   @Override
   public void visit(NodeIf node)
   {
      node.condition.accept(this);
      // TODO(sekai): check for the NOT keyword and do a jumpTrue otherwise.
      // TODO(sekai): check ==, !=
      int jump = builder.opJumpFalse(0);
      node.pass.accept(this);
      int ifEnd;
      // TODO(sekai): when we process the AST, we should check for cases where we need to add a null literal as the fail case.
      if (node.fail != null)
      {
         ifEnd = builder.opJump(0);
         node.fail.accept(this);
         int elseEnd = builder.currentInsnPos();
         builder.setOp_C(ifEnd, elseEnd + 1);
      }
      else
      {
         ifEnd = builder.currentInsnPos();
         if (node.isResultRequired)
         {
            new NodeNullLiteral(null).accept(this);
            int elseEnd = builder.currentInsnPos();
            builder.setOp_C(ifEnd, elseEnd + 1);
         }
      }
      builder.setOp_C(jump, ifEnd + 1);
   }
   
   @Override
   public void visit(NodeNot node)
   {
      node.expression.accept(this);
      builder.opNot();
   }
   
   @Override
   public void visit(NodeAnd node)
   {
      node.left.accept(this);
      int and = builder.opBoolAnd(0);
      node.right.accept(this);
      builder.setOp_C(and, builder.currentInsnPos() + 1);
   }
   
   @Override
   public void visit(NodeOr node)
   {
      node.left.accept(this);
      int and = builder.opBoolOr(0);
      node.right.accept(this);
      builder.setOp_C(and, builder.currentInsnPos() + 1);
   }
}
