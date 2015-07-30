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
package io.fudev.laye.vm;

import java.util.Arrays;

import io.fudev.laye.LayeException;
import lombok.EqualsAndHashCode;

/**
 * @author Sekai Kyoretsuna
 */
public @EqualsAndHashCode(callSuper = false)
class LayeTuple extends LayeObject
{
   private final LayeObject[] values;
   
   public LayeTuple(LayeObject... values)
   {
      this.values = Arrays.copyOf(values, values.length);
   }
   
   @Override
   public String toString()
   {
      StringBuilder result = new StringBuilder().append('(');
      for (int i = 0; i < values.length; i++)
      {
         if (i > 0)
         {
            result.append(", ");
         }
         result.append(values[i].toString());
      }
      return(result.append(')').toString());
   }

   @Override
   public LayeObject load(LayeVM vm, LayeObject key)
   {
      if (key instanceof LayeInt)
      {
         long index =  ((LayeInt)key).value;
         if (index < 0 || index >= values.length)
         {
            throw new LayeException(vm, "Index %d out of bounds.", index);
         }
         return(values[(int)index]);
      }
      return(super.load(vm, key));
   }
}
