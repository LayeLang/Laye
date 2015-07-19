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
public final class Identifier
{
   private static final HashMap<String, Identifier> idents = new HashMap<>();
   
   public static Identifier get(String image)
   {
      if (!isIdentifier(image))
      {
         return null;
      }
      Identifier result = idents.get(image);
      if (result == null)
      {
         result = new Identifier(image);
         idents.put(image, result);
      }
      return result;
   }
   
   public static boolean isIdentifier(String image)
   {
      if (image == null || image.length() == 0 || image.equals("_"))
      {
         return false;
      }
      if (!isIdentifierStart(image.charAt(0)))
      {
         return false;
      }
      for (int i = 1; i < image.length(); i++)
      {
         if (!isIdentifierPart(image.charAt(i)))
         {
            return false;
         }
      }
      return true;
   }
   
   public static boolean isIdentifierStart(char c)
   {
      return c == '_' || Character.isLetter(c);
   }
   
   public static boolean isIdentifierPart(char c)
   {
      return isIdentifierStart(c) || Character.isDigit(c);
   }
   
   public final String image;
   
   private Identifier(String image)
   {
      this.image = image;
   }
   
   // ===== toString, hashCode, and equals

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
      if (!(obj instanceof Identifier))
      {
         return false;
      }
      Identifier other = (Identifier) obj;
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
