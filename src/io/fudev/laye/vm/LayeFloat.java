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

import lombok.EqualsAndHashCode;

/**
 * @author Sekai Kyoretsuna
 */
public final @EqualsAndHashCode(callSuper = false) 
class LayeFloat extends LayeObject
{
   public static final LayeFloat FM1 = valueOf(-1.0);
   public static final LayeFloat F0  = valueOf(0.0);
   public static final LayeFloat F1  = valueOf(1.0);
   public static final LayeFloat F2  = valueOf(2.0);
   
   public static LayeFloat valueOf(double value)
   {
      return(new LayeFloat(value));
   }
   
   public final double value;
   
   public LayeFloat(double value)
   {
      this.value = value;
   }
   
   @Override
   public String toString()
   {
      return(Double.toString(value));
   }
   
   @Override
   public boolean toBool()
   {
      return(value != 0.0f);
   }
   
   @Override
   public boolean compareEquals(LayeObject that)
   {
      if (!(that instanceof LayeFloat))
      {
         return(false);
      }
      return(this.value == ((LayeFloat)that).value);
   }
}
