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
class LayeBool extends LayeObject
{
   static final LayeBool BOOL_TRUE  = new LayeBool(true);
   static final LayeBool BOOL_FALSE = new LayeBool(false);
   
   public final boolean value;
   
   private LayeBool(boolean value)
   {
      this.value = value;
   }
   
   @Override
   public String toString()
   {
      return(value ? "true" : "false");
   }
   
   @Override
   public int hashCode()
   {
      return(value ? 1231 : 1237);
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return(this == obj);
   }
   
   @Override
   public boolean toBool()
   {
      return(value);
   }
}
