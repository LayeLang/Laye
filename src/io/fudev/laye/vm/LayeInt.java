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

import io.fudev.laye.LayeException;
import lombok.EqualsAndHashCode;

/**
 * @author Sekai Kyoretsuna
 */
public final @EqualsAndHashCode(callSuper = false) 
class LayeInt extends LayeObject
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

   public static final LayeInt IM1 = valueOf(-1L);
   public static final LayeInt I0  = valueOf(0L);
   public static final LayeInt I1  = valueOf(1L);
   public static final LayeInt I2  = valueOf(2L);
   public static final LayeInt I3  = valueOf(3L);
   public static final LayeInt I4  = valueOf(4L);
   public static final LayeInt I5  = valueOf(5L);
   
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
   
   @Override
   public boolean isNumeric(LayeVM vm)
   {
      return(true);
   }
   
   @Override
   public long longValue(LayeVM vm)
   {
      return(value);
   }
   
   @Override
   public double doubleValue(LayeVM vm)
   {
      return(value);
   }
   
   @Override
   public boolean toBool(LayeVM vm)
   {
      return(value != 0L);
   }
   
   @Override
   public boolean compareEquals(LayeVM vm, LayeObject that)
   {
      if (!that.isNumeric(vm))
      {
         return(false);
      }
      return(value == that.doubleValue(vm));
   }
   
   @Override
   public LayeObject prefix(LayeVM vm, String op)
   {
      switch (op)
      {
         case "+": return(this);
         case "-": return(valueOf(-value));
         case "~": return(valueOf(~value));
      }
      return(super.prefix(vm, op));
   }
   
   @Override
   public LayeObject infix(LayeVM vm, String op, LayeObject that)
   {
      if (that.isNumeric(vm))
      {
         switch (op)
         {
            case "+":
            {
               if (that instanceof LayeInt)
               {
                  return(valueOf(value + ((LayeInt)that).value));
               }
               else if (that instanceof LayeFloat)
               {
                  return(new LayeFloat(value + ((LayeFloat)that).value));
               }
            } break;
            case "-":
            {
               if (that instanceof LayeInt)
               {
                  return(valueOf(value - ((LayeInt)that).value));
               }
               else if (that instanceof LayeFloat)
               {
                  return(new LayeFloat(value - ((LayeFloat)that).value));
               }
            } break;
            case "*":
            {
               if (that instanceof LayeInt)
               {
                  return(valueOf(value * ((LayeInt)that).value));
               }
               else if (that instanceof LayeFloat)
               {
                  return(new LayeFloat(value * ((LayeFloat)that).value));
               }
            } break;
            case "/":
            {
               if (that instanceof LayeInt)
               {
                  return(valueOf(value / ((LayeInt)that).value));
               }
               else if (that instanceof LayeFloat)
               {
                  return(new LayeFloat(value / ((LayeFloat)that).value));
               }
            } break;
            case "//":
            {
               if (that instanceof LayeInt)
               {
                  return(valueOf(value / ((LayeInt)that).value));
               }
               else if (that instanceof LayeFloat)
               {
                  return(valueOf((long)(value / ((LayeFloat)that).value)));
               }
            } break;
            case "%":
            {
               if (that instanceof LayeInt)
               {
                  return(valueOf(value % ((LayeInt)that).value));
               }
               else if (that instanceof LayeFloat)
               {
                  return(new LayeFloat(value % ((LayeFloat)that).value));
               }
            } break;
            case "^":
            {
               if (that instanceof LayeInt)
               {
                  // TODO(sekai): make sure this works, it should but make sure.
                  long pow = ((LayeInt)that).value;
                  boolean negative = pow < 0L;
                  if (negative)
                  {
                     pow = -pow;
                  }
                  if (pow == 0L)
                  {
                     return(valueOf(1L));
                  }
                  long result = 1L;
                  while (pow-- > 0L)
                  {
                     result *= value;
                  }
                  if (negative)
                  {
                     return(new LayeFloat(1.0 / result));
                  }
                  return(valueOf(result));
               }
               else if (that instanceof LayeFloat)
               {
                  return(new LayeFloat(Math.pow(value, ((LayeFloat)that).value)));
               }
            } break;
            case "&":
            {
               if (that instanceof LayeInt)
               {
                  return(valueOf(value & ((LayeInt)that).value));
               }
            } break;
            case "|":
            {
               if (that instanceof LayeInt)
               {
                  return(valueOf(value | ((LayeInt)that).value));
               }
            } break;
            case "~":
            {
               if (that instanceof LayeInt)
               {
                  return(valueOf(value ^ ((LayeInt)that).value));
               }
            } break;
            case "<<":
            {
               if (that instanceof LayeInt)
               {
                  return(valueOf(value << ((LayeInt)that).value));
               }
            } break;
            case ">>":
            {
               if (that instanceof LayeInt)
               {
                  return(valueOf(value >> ((LayeInt)that).value));
               }
            } break;
            case ">>>":
            {
               if (that instanceof LayeInt)
               {
                  return(valueOf(value >>> ((LayeInt)that).value));
               }
            } break;
            case "<":
            {
               if (that instanceof LayeInt)
               {
                  return(value < ((LayeInt)that).value ? TRUE : FALSE);
               }
               else if (that instanceof LayeFloat)
               {
                  return(value < ((LayeFloat)that).value ? TRUE : FALSE);
               }
            } break;
            case "<=":
            {
               if (that instanceof LayeInt)
               {
                  return(value <= ((LayeInt)that).value ? TRUE : FALSE);
               }
               else if (that instanceof LayeFloat)
               {
                  return(value <= ((LayeFloat)that).value ? TRUE : FALSE);
               }
            } break;
            case ">":
            {
               if (that instanceof LayeInt)
               {
                  return(value > ((LayeInt)that).value ? TRUE : FALSE);
               }
               else if (that instanceof LayeFloat)
               {
                  return(value > ((LayeFloat)that).value ? TRUE : FALSE);
               }
            } break;
            case ">=":
            {
               if (that instanceof LayeInt)
               {
                  return(value >= ((LayeInt)that).value ? TRUE : FALSE);
               }
               else if (that instanceof LayeFloat)
               {
                  return(value >= ((LayeFloat)that).value ? TRUE : FALSE);
               }
            } break;
            case "<=>":
            {
               if (that instanceof LayeInt)
               {
                  long result = value - ((LayeInt)that).value;
                  return(valueOf(result < 0L ? 1L : (result > 0L ? 1L : 0L)));
               }
               else if (that instanceof LayeFloat)
               {
                  double result = value - ((LayeFloat)that).value;
                  return(valueOf(result < 0.0 ? 1L : (result > 0.0 ? 1L : 0L)));
               }
            } break;
            case "<>": return(new LayeString(toString() + that.toString()));
            case "->":
            {
               if (that instanceof LayeInt)
               {
                  LayeList result = new LayeList();
                  long start = value, dest = ((LayeInt)that).value;
                  if (start < dest)
                  {
                     for (; start < dest; start++)
                     {
                        result.append(valueOf(start));
                     }
                  }
                  else
                  {
                     for (; start > dest; start--)
                     {
                        result.append(valueOf(start));
                     }
                  }
                  return(result);
               }
            } break;
            default: return(super.infix(vm, op, that));
         }
      }
      // FIXME(sekai): add type name
      throw new LayeException(vm, 
            "Attempt to perform infix operation '%s' on type with type.", op);
   }
}
