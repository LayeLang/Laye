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
class LayeFunction
   extends LayeComposable
{
   public static @FunctionalInterface
   interface Callback
   {
      LayeObject invoke(LayeVM vm, LayeObject thisObject, LayeObject[] args);
   }
   
   public final Callback callback;
   
   public LayeFunction(Callback callback)
   {
      this.callback = callback;
   }
   
   @Override
   public String toString()
   {
      return ("function:TODO");
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      return prime + ((callback == null) ? 0 : callback.hashCode());
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof LayeFunction))
      {
         return false;
      }
      LayeFunction other = (LayeFunction) obj;
      if (callback == null)
      {
         if (other.callback != null)
         {
            return false;
         }
      }
      else if (!callback.equals(other.callback))
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
      return(callback.invoke(vm, thisObject, args));
   }
}
