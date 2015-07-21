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
package io.ylf.laye.vm;

/**
 * @author Sekai Kyoretsuna
 */
public final class LayeInt extends LayeObject
{
   private static final int CACHE_LOW = -128;
   private static final int CACHE_HIGH = 127;
   
   private static final LayeInt[] CACHE = new LayeInt[CACHE_HIGH - CACHE_LOW + 1];
   
   static
   {
      long value = CACHE_LOW;
      for (int i = 0; i < CACHE.length; i++)
      {
         CACHE[i] = new LayeInt(value++);
      }
   }
   
   public static LayeInt valueOf(long value)
   {
      if (value >= CACHE_LOW && value <= CACHE_HIGH)
      {
         return(CACHE[(int) value - CACHE_LOW]);
      }
      return(new LayeInt(value));
   }
   
   public final long value;
   
   /**
    * Creates a LayeInt value. It's preferred that you use {@link #valueOf(long)}, as it accesses
    * a cache of integers in the range [-128, 127]. If you know the value is not in this range,
    * using this constructor is preferred.
    * @param value
    */
   public LayeInt(long value)
   {
      this.value = value;
   }
   
   @Override
   public String toString()
   {
      return(Long.toString(value));
   }
}
