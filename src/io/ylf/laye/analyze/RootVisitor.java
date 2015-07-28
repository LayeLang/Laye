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
package io.ylf.laye.analyze;

import io.ylf.laye.ast.*;
import io.ylf.laye.log.DetailLogger;
import io.ylf.laye.symbol.SymbolTable;
import lombok.RequiredArgsConstructor;

/**
 * @author Sekai Kyoretsuna
 */
public @RequiredArgsConstructor
class RootVisitor implements ASTVisitor
{
   private final DetailLogger logger;
   private final SymbolTable symbols;
   
   @Override
   public void visit(AST ast)
   {
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
   }
   
   @Override
   public void visit(NodeFunctionDef node)
   {
   }
}