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
class LayeJObject
   extends LayeObject
{
   final Object instance;
   
   LayeJObject(LayeJTypeDef typedef)
   {
      Object instance = null;
      try
      {
         instance = typedef._class.newInstance();
      }
      catch (InstantiationException | IllegalAccessException e)
      {
         e.printStackTrace();
      }
      this.instance = instance;
   }
   
   @Override
   public String toString()
   {
      return("JavaObject:TODO"); // TODO(kai): toString() for LayeJObjects
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((instance == null) ? 0 : instance.hashCode());
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
      if (!(obj instanceof LayeJObject))
      {
         return false;
      }
      LayeJObject other = (LayeJObject) obj;
      if (instance == null)
      {
         if (other.instance != null)
         {
            return false;
         }
      }
      else if (!instance.equals(other.instance))
      {
         return false;
      }
      return true;
   }
}
