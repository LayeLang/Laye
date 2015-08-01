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

/**
 * @author Sekai Kyoretsuna
 */
public final
class OuterValue
{
   private LayeObject[] values;
   private int index;
   
   public OuterValue(LayeObject[] stack, int index)
   {
      values = stack;
      this.index = index;
   }
   
   public @Override String toString()
   {
      return "[" + index + "/" + values.length + "] " + values[index];
   }
   
   public LayeObject getValue()
   {
      return values[index];
   }
   
   public void setValue(LayeVM vm, LayeObject value)
   {
      LayeObject temp;
      if ((temp = values[index]) instanceof LayeReference)
      {
         ((LayeReference)temp).store(vm, value);
      }
      values[index] = value;
   }
   
   public void close()
   {
      final LayeObject[] old = values;
      values = new LayeObject[] { old[index] };
      old[index] = null;
      index = 0;
   }
   
   public int getIndex()
   {
      return index;
   }
}
