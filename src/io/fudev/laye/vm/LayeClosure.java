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

import io.fudev.laye.struct.FunctionPrototype;

/**
 * @author Sekai Kyoretsuna
 */
public
class LayeClosure
   extends LayeComposable
{
   public final FunctionPrototype proto;
   public OuterValue[] captures = null;

   public LayeClosure(FunctionPrototype proto)
   {
      this.proto = proto;
      this.captures = new OuterValue[proto.maxStackSize];
   }
   
   @Override
   public String toString()
   {
      return "LayeClosure:TODO"; // FIXME(sekai): give closures toString()
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + Arrays.hashCode(captures);
      result = prime * result + ((proto == null) ? 0 : proto.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (!super.equals(obj))
      {
         return false;
      }
      if (!(obj instanceof LayeClosure))
      {
         return false;
      }
      LayeClosure other = (LayeClosure) obj;
      if (!Arrays.equals(captures, other.captures))
      {
         return false;
      }
      if (proto == null)
      {
         if (other.proto != null)
         {
            return false;
         }
      }
      else if (!proto.equals(other.proto))
      {
         return false;
      }
      return true;
   }

   @Override
   public boolean isFunction(LayeVM vm)
   {
      return(true);
   }

   @Override
   public LayeObject invoke(LayeVM vm, LayeObject thisObject, LayeObject... args)
   {
      return(vm.invoke(this, thisObject, args));
   }
}
