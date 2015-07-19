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
package io.ylf.laye.symbol;

import io.ylf.laye.struct.Identifier;
import net.fudev.faxlib.collections.List;

/**
 * Represents any code block in Laye, even local scopes.
 * 
 * @author Sekai Kyoretsuna
 */
public class LexicalScope
{
   public final LexicalScope parent;
   private final List<LexicalScope> children = new List<LexicalScope>();
   
   private final List<Symbol> symbols = new List<Symbol>();
   
   public LexicalScope(LexicalScope parent)
   {
      this.parent = parent;
   }
   
   /**
    * Adds a child scope to this scope.
    * @param child
    */
   public void addChildScope(LexicalScope child)
   {
      assert(child != null);
      children.append(child);
   }
   
   /**
    * @return A copy of the children list.
    */
   public List<LexicalScope> getChildren()
   {
      return new List<LexicalScope>(children);
   }
   
   public void addSymbol(Symbol.Type type, Identifier name, int index)
   {
      symbols.append(new Symbol(type, name, index));
   }
   
   public Symbol getSymbol(Identifier name)
   {
      LexicalScope scope = this;
      while (scope != null)
      {
         for (Symbol symbol : symbols)
         {
            if (symbol.name == name)
            {
               return symbol;
            }
         }
         scope = scope.parent;
      }
      return null;
   }
}
