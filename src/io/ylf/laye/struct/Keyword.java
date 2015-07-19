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

/**
 * @author Sekai Kyoretsuna
 */
public class Keyword
{
   private static final HashMap<String, Keyword> keywords = new HashMap<String, Keyword>();
   
   public static final String STR_AND = "and";
   public static final String STR_OR = "or";
   public static final String STR_XOR = "xor";
   public static final String STR_NOT = "not";
   public static final String STR_TYPEOF = "typeof";
   public static final String STR_REF = "ref";
   public static final String STR_DEREF = "deref";

   public static final String STR_TRUE = "true";
   public static final String STR_FALSE = "false";
   public static final String STR_NULL = "null";

   public static final String STR_IF = "if";
   public static final String STR_EL = "el";
   public static final String STR_ITER = "iter";
   public static final String STR_TO = "to";
   public static final String STR_BY = "by";
   public static final String STR_EACH = "each";
   public static final String STR_IN = "in";
   public static final String STR_WHILE = "while";
   public static final String STR_TAKE = "take";
   public static final String STR_MATCH = "match";

   public static final String STR_EXIT = "exit";
   public static final String STR_RET = "ret";
   public static final String STR_CONT = "cont";
   public static final String STR_BREAK = "break";
   public static final String STR_RESUME = "resume";
   public static final String STR_YIELD = "yield";

   public static final String STR_VAR = "var";
   public static final String STR_THIS = "this";
   public static final String STR_SELF = "self";
   public static final String STR_BASE = "base";

   public static final String STR_FN = "fn";
   public static final String STR_TYPE = "type";
   public static final String STR_USE = "use";
   public static final String STR_FROM = "from";
   public static final String STR_IMPORT = "import";
   
   public static final Keyword AND = new Keyword(STR_AND);
   public static final Keyword OR = new Keyword(STR_OR);
   public static final Keyword XOR = new Keyword(STR_XOR);
   public static final Keyword NOT = new Keyword(STR_NOT);
   public static final Keyword TYPEOF = new Keyword(STR_TYPEOF);
   public static final Keyword REF = new Keyword(STR_REF);
   public static final Keyword DEREF = new Keyword(STR_DEREF);

   public static final Keyword TRUE = new Keyword(STR_TRUE);
   public static final Keyword FALSE = new Keyword(STR_FALSE);
   public static final Keyword NULL = new Keyword(STR_NULL);

   public static final Keyword IF = new Keyword(STR_IF);
   public static final Keyword EL = new Keyword(STR_EL);
   public static final Keyword ITER = new Keyword(STR_ITER);
   public static final Keyword TO = new Keyword(STR_TO);
   public static final Keyword BY = new Keyword(STR_BY);
   public static final Keyword EACH = new Keyword(STR_EACH);
   public static final Keyword IN = new Keyword(STR_IN);
   public static final Keyword WHILE = new Keyword(STR_WHILE);
   public static final Keyword TAKE = new Keyword(STR_TAKE);
   public static final Keyword MATCH = new Keyword(STR_MATCH);

   public static final Keyword EXIT = new Keyword(STR_EXIT);
   public static final Keyword RET = new Keyword(STR_RET);
   public static final Keyword CONT = new Keyword(STR_CONT);
   public static final Keyword BREAK = new Keyword(STR_BREAK);
   public static final Keyword RESUME = new Keyword(STR_RESUME);
   public static final Keyword YIELD = new Keyword(STR_YIELD);

   public static final Keyword VAR = new Keyword(STR_VAR);
   public static final Keyword THIS = new Keyword(STR_THIS);
   public static final Keyword SELF = new Keyword(STR_SELF);
   public static final Keyword BASE = new Keyword(STR_BASE);

   public static final Keyword FN = new Keyword(STR_FN);
   public static final Keyword TYPE = new Keyword(STR_TYPE);
   public static final Keyword USE = new Keyword(STR_USE);
   public static final Keyword FROM = new Keyword(STR_FROM);
   public static final Keyword IMPORT = new Keyword(STR_IMPORT);

   public static boolean exists(String image)
   {
      assert(image != null);
      return keywords.containsKey(image);
   }
   
   public static Keyword get(String image)
   {
      assert(image != null);
      return keywords.get(image);
   }
   
   public final String image;
   
   private Keyword(String image)
   {
      assert(image != null && !keywords.containsKey(image));
      this.image = image;
      keywords.put(image, this);
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
      if (!(obj instanceof Keyword))
      {
         return false;
      }
      Keyword other = (Keyword) obj;
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
