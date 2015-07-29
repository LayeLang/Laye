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

import java.util.HashMap;

import io.fudev.laye.lexical.Token;
import lombok.EqualsAndHashCode;

/**
 * @author Sekai Kyoretsuna
 */
public final @EqualsAndHashCode
class Identifier
{
   private static final HashMap<String, Identifier> idents = new HashMap<>();
   
   public static Identifier get(String image)
   {
      if (!isIdentifier(image))
      {
         return(null);
      }
      Identifier result = idents.get(image);
      if (result == null)
      {
         result = new Identifier(image);
         idents.put(image, result);
      }
      return(result);
   }
   
   public static boolean isIdentifier(String image)
   {
      if (image == null || image.length() == 0 || image.equals("_"))
      {
         return(false);
      }
      if (!isIdentifierStart(image.charAt(0)))
      {
         return(false);
      }
      for (int i = 1; i < image.length(); i++)
      {
         if (!isIdentifierPart(image.charAt(i)))
         {
            return(false);
         }
      }
      return(true);
   }
 
   public static boolean isIdentifierStart(int codepoint)
   {
      return(codepoint == '_' || !(Character.isWhitespace(codepoint) || 
             Token.isReservedCharacter(codepoint) || Operator.isOperatorChar(codepoint) ||
             Character.isDigit(codepoint)));
   }
   
   public static boolean isIdentifierPart(int c)
   {
      return(Character.isDigit(c) || isIdentifierStart(c));
   }
   
   public final String image;
   
   private Identifier(String image)
   {
      this.image = image;
   }
   
   @Override
   public String toString()
   {
      return(image);
   }
}
