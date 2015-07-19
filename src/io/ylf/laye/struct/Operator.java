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
package io.ylf.laye.struct;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Sekai Kyoretsuna
 */
public final class Operator
{
   private static final Map<String, Operator> operators = new HashMap<String, Operator>();
   
   public static final int DEFAULT_PRECEDENCE = 7;
   
   public static final Operator PLUS = get("+");
   public static final Operator MINUS = get("-");
   public static final Operator TIMES = get("*", 8);
   public static final Operator DIVIDE = get("/", 8);
   public static final Operator INT_DIVIDE = get("//", 8);
   public static final Operator MODULO = get("%", 8);
   public static final Operator POWER = get("^");
   public static final Operator AND = get("&", 3);
   public static final Operator OR = get("|", 1);
   public static final Operator XOR_COMPL = get("~", 2);
   public static final Operator LSHIFT = get("<<", 6);
   public static final Operator RSHIFT = get(">>", 6);
   public static final Operator URSHIFT = get(">>>", 6);
   public static final Operator EQUALTO = get("==", 4);
   public static final Operator NEQUALTO = get("!=", 4);
   public static final Operator LESS = get("<", 5);
   public static final Operator LESSEQ = get("<=", 5);
   public static final Operator GREATER = get(">", 5);
   public static final Operator GREATEREQ = get(">=", 5);
   public static final Operator COMP3 = get("<=>", 5);
   public static final Operator CONCAT = get("<>");
   
   public static Operator get(String image)
   {
      return get(image, DEFAULT_PRECEDENCE);
   }
   
   /**
    * If an operator with the given image already exists, the precedence is ignored.
    * @param image
    * @param precedence
    * @return
    */
   public static Operator get(String image, int precedence)
   {
      assert(precedence > 0);
      if (!isOperatorImage(image))
      {
         return null;
      }
      Operator result = operators.get(image);
      if (result == null)
      {
         result = new Operator(image, precedence);
         operators.put(image, result);
      }
      return result;
   }
   
   public static boolean isOperatorImage(String image)
   {
      if (image == null || image.equals("="))
      {
         return false;
      }
      for (char c : image.toCharArray())
      {
         if (!isOperatorChar(c))
         {
            return false;
         }
      }
      return true;
   }
   
   public static boolean isOperatorChar(char c)
   {
      return "~!@$%^&*-+=\\|<>/?".indexOf(c) != -1;
   }
   
   public final String image;
   
   /**
    * The precedence of this operator, only applying to infix operations.
    */
   public final int precedence;
   
   private Operator(String image, int precedence)
   {
      this.image = image;
      this.precedence = precedence;
   }
   
   @Override
   public String toString()
   {
      return image;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((image == null) ? 0 : image.hashCode());
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
      if (!(obj instanceof Operator))
      {
         return false;
      }
      Operator other = (Operator) obj;
      if (image == null)
      {
         if (other.image != null)
         {
            return false;
         }
      }
      else if (!image.equals(other.image))
      {
         return false;
      }
      return true;
   }
}
