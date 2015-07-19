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

package io.ylf.laye.lexical;

/**
 * @author Sekai Kyoretsuna
 */
public class Token
{
   public static enum Type
   {
      KEYWORD,
      IDENTIFIER,
      OPERATOR,
      INT_LITERAL,
      FLOAT_LITERAL,
      STRING_LITERAL,
      
      ASSIGN("="),
      WILDCARD("_"),
      OPEN_BRACE("("),
      CLOSE_BRACE(")"),
      OPEN_SQUARE_BRACE("["),
      CLOSE_SQUARE_BRACE(")"),
      OPEN_CURLY_BRACE("{"),
      CLOSE_CURLY_BRACE("}"),
      SEMI_COLON(";"),
      COLON(":"),
      COMMA(","),
      DOT(".");
      
      public final String image;
      
      private Type()
      {
         this.image = null;
      }
      
      private Type(String image)
      {
         this.image = image;
      }
   }
   
   public final Type type;
   
   /**
    * Data stored for each token. In the case of keywords, operators, and identifiers, this is 
    * an instance of Keyword, Operator, or Identifier respectively representing the image of the
    * token. In the case of literals, this represents the literal object as a LayeObject.
    * This should never be null.
    */
   public final Object data;
   public final Location location;
   
   public Token(Type type, Location location)
   {
      assert(type != null);
      assert(location != null);
      this.type = type;
      this.data = type.image;
      this.location = location;
      assert(data != null);
   }
   
   public Token(Type type, Object data, Location location)
   {
      assert(type != null);
      assert(data != null);
      assert(location != null);
      this.type = type;
      this.data = data;
      this.location = location;
   }
   
   @Override
   public String toString()
   {
      return data.toString();
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((data == null) ? 0 : data.hashCode());
      result = prime * result + ((location == null) ? 0 : location.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
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
      if (!(obj instanceof Token))
      {
         return false;
      }
      Token other = (Token) obj;
      if (data == null)
      {
         if (other.data != null)
         {
            return false;
         }
      }
      else if (!data.equals(other.data))
      {
         return false;
      }
      if (location == null)
      {
         if (other.location != null)
         {
            return false;
         }
      }
      else if (!location.equals(other.location))
      {
         return false;
      }
      if (type != other.type)
      {
         return false;
      }
      return true;
   }
}
