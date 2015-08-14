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
package io.fudev.laye.process;

import static io.fudev.laye.log.LogMessageID.ERROR_INVALID_BLOCK;

import io.fudev.collections.List;
import io.fudev.laye.ast.*;
import io.fudev.laye.log.DetailLogger;

/**
 * @author Sekai Kyoretsuna
 */ // TODO(sekai): Constant folding!
public
class ASTProcessor
{
   private final DetailLogger logger;
   
   public ASTProcessor(DetailLogger logger)
   {
      this.logger = logger;
   }
   
   public AST process(AST node)
   {
      AST result = new AST();
      node.children.forEach(child ->
      {
         if (child instanceof NodeExpression)
         {
            ((NodeExpression)child).isResultRequired = false;
         }
         ASTNode value = child.accept(this);
         if (value != null)
         {
            result.children.append(value);
         }
      });
      return(result);
   }

   public ASTNode process(NodeScope node)
   {
      NodeScope result = new NodeScope(node.location);
      result.isResultRequired = node.isResultRequired;
      int size = node.body.size();
      for (int i = 0; i < size; i++)
      {
         ASTNode child = node.body.get(i);
         if (child instanceof NodeExpression)
         {
            ((NodeExpression)child).isResultRequired = i == size - 1 ?
                  node.isResultRequired : false;
            //System.out.println(child + ": " + ((NodeExpression)child).isResultRequired);
         }
         ASTNode value = child.accept(this);
         if (value != null)
         {
            result.body.append(value);
         }
      }
      if (!(result.body.get(result.body.size() - 1) instanceof NodeExpression)
            && node.isResultRequired)
      {
         logger.logError(node.body.get(size - 1).location, ERROR_INVALID_BLOCK,
               "Expression expected to end expression-block, got statement.");
      }
      if (result.body.size() == 1)
      {
         return(result.body.get(0));
      }
      return(result);
   }
   
   public ASTNode process(NodeVariableDef node)
   {
      NodeVariableDef result = new NodeVariableDef(node.location);
      result.names = node.names;
      result.values = node.values.map(value -> (NodeExpression)value.accept(this));
      result.isResultRequired = node.isResultRequired;
      return(node);
   }
   
   public ASTNode process(NodeNullLiteral node)
   {
      return(node);
   }
   
   public ASTNode process(NodeBoolLiteral node)
   {
      return(node);
   }
   
   public ASTNode process(NodeIntLiteral node)
   {
      return(node);
   }
   
   public ASTNode process(NodeFloatLiteral node)
   {
      return(node);
   }
   
   public ASTNode process(NodeStringLiteral node)
   {
      return(node);
   }
   
   public ASTNode process(NodePrefixExpression node)
   {
      NodePrefixExpression result = new NodePrefixExpression(node.location,
            (NodeExpression)node.expression.accept(this), node.operator);
      result.isResultRequired = node.isResultRequired;
      return(result);
   }
   
   public ASTNode process(NodeInfixExpression node)
   {
      NodeInfixExpression result = new NodeInfixExpression(node.location,
            (NodeExpression)node.left.accept(this), (NodeExpression)node.right.accept(this),
            node.operator);
      result.isResultRequired = node.isResultRequired;
      return(result);
   }
   
   public ASTNode process(NodeFunctionDef node)
   {
      NodeExpression body = (NodeExpression)node.data.body.accept(this);
      if (body == node.data.body)
      {
         return(node);
      }
      else
      {
         NodeFunctionDef result = new NodeFunctionDef(node.location);
         result.data = new FunctionData(node.data);
         result.name = node.name;
         result.data.body = body;
         return(result);
      }
   }
   
   public ASTNode process(NodeFunction node)
   {
      NodeExpression body = (NodeExpression)node.data.body.accept(this);
      if (body == node.data.body)
      {
         return(node);
      }
      else
      {
         NodeFunction result = new NodeFunction(node.location);
         result.isResultRequired = node.isResultRequired;
         result.data = new FunctionData(node.data);
         result.data.body = body;
         return(result);
      }
   }
   
   public ASTNode process(NodeAssignment node)
   {
      NodeAssignment result = new NodeAssignment(node.location,
            (NodeExpression)node.left.accept(this), (NodeExpression)node.right.accept(this));
      result.isResultRequired = node.isResultRequired;
      return(result);
   }
   
   public ASTNode process(NodeIdentifier node)
   {
      return(node);
   }
   
   public ASTNode process(NodeInvoke node)
   {
      List<NodeExpression> args = node.args.map(arg -> (NodeExpression)arg.accept(this));
      NodeInvoke result = new NodeInvoke(node.location,
            (NodeExpression)node.target.accept(this), args);
      result.isResultRequired = node.isResultRequired;
      return(result);
   }
   
   public ASTNode process(NodeList node)
   {
      List<NodeExpression> values = node.values.map(arg -> (NodeExpression)arg.accept(this));
      NodeList result = new NodeList(node.location, values);
      result.isResultRequired = node.isResultRequired;
      return(result);
   }
   
   public ASTNode process(NodeTuple node)
   {
      List<NodeExpression> values = node.values.map(arg -> (NodeExpression)arg.accept(this));
      NodeTuple result = new NodeTuple(node.location, values);
      result.isResultRequired = node.isResultRequired;
      return(result);
   }
   
   public ASTNode process(NodeLoadIndex node)
   {
      NodeLoadIndex result = new NodeLoadIndex(node.location,
            (NodeExpression)node.target.accept(this), (NodeExpression)node.index.accept(this));
      result.isResultRequired = node.isResultRequired;
      return(result);
   }
   
   public ASTNode process(NodeLoadField node)
   {
      NodeLoadField result = new NodeLoadField(node.location,
            (NodeExpression)node.target.accept(this), node.index);
      result.isResultRequired = node.isResultRequired;
      return(result);
   }

   public ASTNode process(NodeIf node)
   {
      node.pass.isResultRequired = node.isResultRequired;
      if (node.fail != null)
      {
         node.fail.isResultRequired = node.isResultRequired;
      }
      // FIXME(sekai): only accept values if they're used, save time.
      NodeExpression pass = (NodeExpression)node.pass.accept(this);
      NodeExpression fail = node.fail == null && node.isResultRequired ?
            new NodeNullLiteral(null) : (NodeExpression)node.fail.accept(this);
      if (node.condition instanceof NodeNullLiteral)
      {
         return(fail);
      }
      else if (node.condition instanceof NodeBoolLiteral)
      {
         return((((NodeBoolLiteral)node.condition).value ? pass : fail));
      }
      else if (node.condition instanceof NodeIntLiteral)
      {
         return((((NodeIntLiteral)node.condition).value.toBool(null) ? pass : fail));
      }
      else if (node.condition instanceof NodeFloatLiteral)
      {
         return((((NodeFloatLiteral)node.condition).value.toBool(null) ? pass : fail));
      }
      else if (node.condition instanceof NodeStringLiteral)
      {
         return(pass);
      }
      else
      {
         NodeIf result = new NodeIf(node.location);
         result.condition = (NodeExpression)node.condition.accept(this);
         result.pass = pass;
         result.fail = fail;
         result.isResultRequired = node.isResultRequired;
         return(result);
      }
   }

   public ASTNode process(NodeNot node)
   {
      // TODO(sekai): check special cases
      NodeNot result = new NodeNot(node.location,
            (NodeExpression)node.expression.accept(this));
      result.isResultRequired = node.isResultRequired;
      return(result);
   }

   public ASTNode process(NodeAnd node)
   {
      // TODO(sekai): check special cases
      NodeAnd result = new NodeAnd(node.location, (NodeExpression)node.left.accept(this),
            (NodeExpression)node.right.accept(this));
      result.isResultRequired = node.isResultRequired;
      return(result);
   }

   public ASTNode process(NodeOr node)
   {
      // TODO(sekai): check special cases
      NodeOr result = new NodeOr(node.location, (NodeExpression)node.left.accept(this),
            (NodeExpression)node.right.accept(this));
      result.isResultRequired = node.isResultRequired;
      return(result);
   }
   
   public ASTNode process(NodeWhile node)
   {
      boolean isResultRequired = node.isResultRequired;
      node.pass.isResultRequired = isResultRequired;
      if (node.initialFail != null)
      {
         node.initialFail.isResultRequired = isResultRequired;
      }
      // FIXME(sekai): only accept values if they're used, save time.
      NodeExpression pass = (NodeExpression)node.pass.accept(this);
      NodeExpression initialFail = node.initialFail == null ? null :
         (NodeExpression)node.initialFail.accept(this);
      if (node.condition instanceof NodeNullLiteral)
      {
         return(initialFail);
      }
      else if (node.condition instanceof NodeBoolLiteral)
      {
         return((((NodeBoolLiteral)node.condition).value ? pass : initialFail));
      }
      else if (node.condition instanceof NodeIntLiteral)
      {
         return((((NodeIntLiteral)node.condition).value.toBool(null) ? pass : initialFail));
      }
      else if (node.condition instanceof NodeFloatLiteral)
      {
         return((((NodeFloatLiteral)node.condition).value.toBool(null) ? pass : initialFail));
      }
      else if (node.condition instanceof NodeStringLiteral)
      {
         return(pass);
      }
      else
      {
         NodeWhile result = new NodeWhile(node.location, (NodeExpression)node.condition.accept(this),
               pass, initialFail);
         result.isResultRequired = isResultRequired;
         return(result);
      }
   }
   
   public ASTNode process(NodeMatch node)
   {
      NodeMatch result = new NodeMatch(node.location, (NodeExpression)node.match.accept(this));
      for (int i = 0; i < node.cases.size(); i++)
      {
         NodeExpression _case = (NodeExpression)node.cases.get(i).accept(this);
         NodeExpression path = (NodeExpression)node.paths.get(i).accept(this);
         path.isResultRequired = node.isResultRequired;
         result.addCase(_case, path);
      }
      result.isResultRequired = node.isResultRequired;
      return(result);
   }
   
   public ASTNode process(NodeReference node)
   {
      NodeReference result = new NodeReference(node.location,
            (NodeExpression)node.expression.accept(this));
      result.isResultRequired = node.isResultRequired;
      return(result);
   }
   
   public ASTNode process(NodeDereference node)
   {
      NodeDereference result = new NodeDereference(node.location,
            (NodeExpression)node.expression.accept(this));
      result.isResultRequired = node.isResultRequired;
      return(result);
   }
   
   public ASTNode process(NodeWildcard node)
   {
      return(node);
   }
}
