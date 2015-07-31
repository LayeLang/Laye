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
package io.fudev.laye.ast;

/**
 * @author Sekai Kyoretsuna
 */
public
interface IASTVisitor
{
   void visit(AST ast);

   void visit(NodeVariableDef node);

   void visit(NodeNullLiteral node);

   void visit(NodeBoolLiteral node);

   void visit(NodeIntLiteral node);

   void visit(NodeFloatLiteral node);

   void visit(NodeStringLiteral node);

   void visit(NodePrefixExpression node);

   void visit(NodeInfixExpression node);

   void visit(NodeScope node);

   void visit(NodeFunctionDef node);

   void visit(NodeAssignment node);

   void visit(NodeIdentifier node);

   void visit(NodeInvoke node);

   void visit(NodeList node);

   void visit(NodeTuple node);

   void visit(NodeLoadIndex node);

   void visit(NodeIf node);

   void visit(NodeNot node);

   void visit(NodeAnd node);
}