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
public
class LayeComposition
   extends LayeObject
{
   private LayeObject first, second;
   
   public LayeComposition(LayeObject first, LayeObject second)
   {
      this.first = first;
      this.second = second;
   }
   
   @Override
   public String toString()
   {
      return("Composition:TODO"); // TODO(kai): toString() for compositions
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((first == null) ? 0 : first.hashCode());
      result = prime * result + ((second == null) ? 0 : second.hashCode());
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
      if (!(obj instanceof LayeComposition))
      {
         return false;
      }
      LayeComposition other = (LayeComposition) obj;
      if (first == null)
      {
         if (other.first != null)
         {
            return false;
         }
      }
      else if (!first.equals(other.first))
      {
         return false;
      }
      if (second == null)
      {
         if (other.second != null)
         {
            return false;
         }
      }
      else if (!second.equals(other.second))
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
      return(second.invoke(vm, thisObject, first.invoke(vm, thisObject, args)));
   }
}
