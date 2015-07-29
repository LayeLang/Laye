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

import java.util.Iterator;

import io.fudev.laye.lexical.Location;
import io.fudev.laye.struct.Identifier;
import io.fudev.laye.util.Pair;
import net.fudev.faxlib.collections.List;

/**
 * @author Sekai Kyoretsuna
 */
public
class NodeVariableDef extends ASTNode implements Iterable<Pair<Identifier, NodeExpression>>
{
   public List<Identifier> names = new List<>();
   public List<NodeExpression> values = new List<>();
   
   public NodeVariableDef(Location location)
   {
      super(location);
   }
   
   public void addDefinition(Identifier name, NodeExpression value)
   {
      names.append(name);
      values.append(value);
   }
   
   @Override
   public void accept(ASTVisitor visitor)
   {
      visitor.visit(this);
   }

   @Override
   public Iterator<Pair<Identifier, NodeExpression>> iterator()
   {
      return(new Iterator<Pair<Identifier,NodeExpression>>()
      {
         private final List<Identifier> names = new List<>(NodeVariableDef.this.names);
         private final List<NodeExpression> values = new List<>(NodeVariableDef.this.values);

         private final int length = names.size();
         private int index = 0;
         
         @Override
         public boolean hasNext()
         {
            return(index < length);
         }

         @Override
         public Pair<Identifier, NodeExpression> next()
         {
            return(new Pair<>(names.get(index), values.get(index++)));
         }
      });
   }
}
