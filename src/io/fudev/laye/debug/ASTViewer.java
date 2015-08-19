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
package io.fudev.laye.debug;

import java.io.PrintStream;
import java.util.Objects;

import io.fudev.laye.ast.*;

/**
 * @author Sekai Kyoretsuna
 */
public
class ASTViewer
   implements IASTVisitor
{
   private PrintStream out;
   
   private int tabs = 0;
   
   public ASTViewer(PrintStream out)
   {
      if (out == null)
      {
         throw new NullPointerException();
      }
      this.out = out;
   }
   
   private String getTabs()
   {
      assert(tabs >= 0);
      if (tabs == 0)
      {
         return("");
      }
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < tabs; i++)
      {
         builder.append("\t");
      }
      return(builder.toString());
   }
   
   private void tprint(String output)
   {
      assert(output != null);
      out.print(getTabs() + output);
   }
   
   private void tprint(Object output)
   {
      out.print(getTabs() + Objects.toString(output));
   }

   private void tprintln(String output)
   {
      assert(output != null);
      out.println(getTabs() + output);
   }

   private void tprint()
   {
      out.print(getTabs());
   }
   
   private void tprintln(Object output)
   {
      out.println(getTabs() + Objects.toString(output));
   }
   
   private void print(String output)
   {
      assert(output != null);
      out.print(output);
   }
   
   private void print(Object output)
   {
      out.print(Objects.toString(output));
   }
   
   private void println(String output)
   {
      assert(output != null);
      out.println(output);
   }
   
   private void println(Object output)
   {
      out.println(Objects.toString(output));
   }
   
   private void println()
   {
      out.println();
   }

   @Override
   public void visit(AST ast)
   {
      ast.forEach(node ->
      {
         tprint();
         node.accept(this);
         println();
      });
   }
   
   @Override
   public void visit(NodeVariableDef node)
   {
      if (node.names.size() > 1)
      {
         println("VAR (");
         tabs++;
         node.forEach(pair ->
         {
            tprint(pair.a);
            print(" = ");
            pair.b.accept(this);
            println("");
         });
         tabs--;
         tprint(")");
      }
      else
      {
         print("VAR ");
         print(node.names.get(0));
         print(" = ");
         node.values.get(0).accept(this);
      }
   }
   
   @Override
   public void visit(NodeNullLiteral node)
   {
      print("null");
   }
   
   @Override
   public void visit(NodeBoolLiteral node)
   {
      print(node.value ? "true" : "false");
   }

   @Override
   public void visit(NodeIntLiteral node)
   {
      print(node.value);
   }

   @Override
   public void visit(NodeFloatLiteral node)
   {
      print(node.value);
   }

   @Override
   public void visit(NodeStringLiteral node)
   {
      print("'");
      print(node.value);
      print("'");
   }

   @Override
   public void visit(NodePrefixExpression node)
   {
      print(node.operator.image);
      print("(");
      node.expression.accept(this);
      print(")");
   }

   @Override
   public void visit(NodeInfixExpression node)
   {
      print("(");
      node.left.accept(this);
      print(") ");
      print(node.operator.image);
      print(" (");
      node.right.accept(this);
      print(")");
   }

   @Override
   public void visit(NodeScope node)
   {
      println("{");
      tabs++;
      node.body.forEach(child -> {
         tprint();
         child.accept(this);
         println();
      });
      tabs--;
      tprint("}");
   }

   @Override
   public void visit(NodeFunctionDef node)
   {
      print("FUNCTION ");
      print(node.name);
      print(" (");
      for (int i = 0; i < node.data.params.size(); i++)
      {
         if (i > 0)
         {
            print(", ");
         }
         print(node.data.params.get(i));
      }
      if (node.data.vargs)
      {
         print("..");
      }
      print(") ");
      node.data.body.accept(this);
   }

   @Override
   public void visit(NodeFunction node)
   {
      print("FUNCTION (");
      for (int i = 0; i < node.data.params.size(); i++)
      {
         if (i > 0)
         {
            print(", ");
         }
         print(node.data.params.get(i));
      }
      if (node.data.vargs)
      {
         print("..");
      }
      print(") ");
      node.data.body.accept(this);
   }

   @Override
   public void visit(NodeAssignment node)
   {
      node.left.accept(this);
      print(" = ");
      node.right.accept(this);
      print("");
   }

   @Override
   public void visit(NodeIdentifier node)
   {
      print(node.value);
   }
   
   @Override
   public void visit(NodeInvoke node)
   {
      node.target.accept(this);
      if (node.args.size() > 0)
      {
         println(" (");
         tabs++;
         node.args.forEach(arg ->
         {
            tprint();
            arg.accept(this);
            println();
         });
         tabs--;
         tprint(")");
      }
      else
      {
         println(" ()");
      }
   }
   
   @Override
   public void visit(NodeList node)
   {
      if (node.values.size() > 0)
      {
         println("[");
         tabs++;
         node.values.forEach(arg ->
         {
            tprint();
            arg.accept(this);
            println();
         });
         tabs--;
         tprint("]");
      }
      else
      {
         println("[]");
      }
   }
   
   @Override
   public void visit(NodeTuple node)
   {
      println("(");
      tabs++;
      node.values.forEach(arg ->
      {
         tprint();
         arg.accept(this);
         println();
      });
      tabs--;
      tprint(")");
   }

   @Override
   public void visit(NodeLoadIndex node)
   {
      print("(");
      node.target.accept(this);
      print(")[");
      node.index.accept(this);
      print("]");
   }

   @Override
   public void visit(NodeLoadField node)
   {
      print("(");
      node.target.accept(this);
      print(").");
      print(node.index);
   }
   
   @Override
   public void visit(NodeIf node)
   {
      print("IF ");
      node.condition.accept(this);
      print(" ");
      node.pass.accept(this);
      if (node.fail != null)
      {
         println();
         tprint("ELSE ");
         node.fail.accept(this);
      }
   }
   
   @Override
   public void visit(NodeNot node)
   {
      print("NOT ");
      node.expression.accept(this);
   }
   
   @Override
   public void visit(NodeAnd node)
   {
      node.left.accept(this);
      print(" AND ");
      node.right.accept(this);
   }
   
   @Override
   public void visit(NodeOr node)
   {
      node.left.accept(this);
      print(" OR ");
      node.right.accept(this);
   }
   
   @Override
   public void visit(NodeWhile node)
   {
      print("WHILE");
      node.condition.accept(this);
      print(" ");
      node.pass.accept(this);
      if (node.initialFail != null)
      {
         println();
         tprint("ELSE ");
         node.initialFail.accept(this);
      }
   }
   
   @Override
   public void visit(NodeMatch node)
   {
      node.match.accept(this);
      println(" MATCH {");
      tabs++;
      for (int i = 0; i < node.cases.size(); i++)
      {
         tprint();
         node.cases.get(i).accept(this);
         print(": ");
         node.paths.get(i).accept(this);
         println();
      }
      tabs--;
      tprint("}");
   }
   
   @Override
   public void visit(NodeReference node)
   {
      print("REF ");
      node.expression.accept(this);
   }
   
   @Override
   public void visit(NodeDereference node)
   {
      print("DEREF ");
      node.expression.accept(this);
   }
   
   @Override
   public void visit(NodeWildcard node)
   {
      print("_");
   }
   
   @Override
   public void visit(NodeNewInstance node)
   {
      print("new ");
      node.target.accept(this);
      if (node.ctor != null)
      {
         print(":");
         print(node.ctor);
      }
      if (node.args.size() > 0)
      {
         println(" (");
         tabs++;
         node.args.forEach(arg ->
         {
            tprint();
            arg.accept(this);
            println();
         });
         tabs--;
         tprint(")");
      }
      else
      {
         println(" ()");
      }
   }
}
