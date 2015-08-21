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
package io.fudev.laye.struct;

import java.util.Arrays;

/**
 * @author Sekai Kyoretsuna
 */
public
class FunctionPrototype
{
   public int[] code = {};
   public int numParams = 0;
   public boolean vargs = false;
   public int maxLocals = 0;
   public int maxStackSize = 0;
   public Object[] consts = null;
   public OuterValueInfo[] outerValues = null;
   public FunctionPrototype[] nestedClosures = null;
   public TypePrototype[] definedTypes = null;
   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode(code);
      result = prime * result + Arrays.hashCode(consts);
      result = prime * result + Arrays.hashCode(definedTypes);
      result = prime * result + maxLocals;
      result = prime * result + maxStackSize;
      result = prime * result + Arrays.hashCode(nestedClosures);
      result = prime * result + numParams;
      result = prime * result + Arrays.hashCode(outerValues);
      result = prime * result + (vargs ? 1231 : 1237);
      return result;
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
      if (!(obj instanceof FunctionPrototype))
      {
         return false;
      }
      FunctionPrototype other = (FunctionPrototype) obj;
      if (!Arrays.equals(code, other.code))
      {
         return false;
      }
      if (!Arrays.equals(consts, other.consts))
      {
         return false;
      }
      if (!Arrays.equals(definedTypes, other.definedTypes))
      {
         return false;
      }
      if (maxLocals != other.maxLocals)
      {
         return false;
      }
      if (maxStackSize != other.maxStackSize)
      {
         return false;
      }
      if (!Arrays.equals(nestedClosures, other.nestedClosures))
      {
         return false;
      }
      if (numParams != other.numParams)
      {
         return false;
      }
      if (!Arrays.equals(outerValues, other.outerValues))
      {
         return false;
      }
      if (vargs != other.vargs)
      {
         return false;
      }
      return true;
   }
}
