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
package io.ylf.laye;

import java.util.HashMap;

/**
 * @author Sekai Kyoretsuna
 */
public final class LogMessageID
{
   // TODO(sekai): I think this should take descriptions, for user help.
   
   private static final HashMap<Integer, LogMessageID> ids = new HashMap<>();
   
   private static int NEXT_CODE = 0;
   
   // ===== Warnings
   
   public static final LogMessageID WARNING_FLOAT_DECOR = new LogMessageID(
         "Float Decoration",
         NEXT_CODE++);
   
   // ===== Errors

   public static final LogMessageID ERROR_ESCAPED_UNICODE_SIZE = new LogMessageID(
         "Escaped Unicode Character Size",
         NEXT_CODE++);

   public static final LogMessageID ERROR_UNFINISHED_STRING = new LogMessageID(
         "Unfinished String Literal",
         NEXT_CODE++);

   public static final LogMessageID ERROR_ILLEGAL_ESCAPE = new LogMessageID(
         "Illegal Escape Code",
         NEXT_CODE++);

   public static final LogMessageID ERROR_UNDERSCORE_IN_NUMBER = new LogMessageID(
         "Underscore in Number",
         NEXT_CODE++);

   public static final LogMessageID ERROR_DOT_AFTER_NUMBER = new LogMessageID(
         "Dot After Number",
         NEXT_CODE++);

   public static final LogMessageID ERROR_TRAILING_CHARS_IN_NUMBER = new LogMessageID(
         "Trailing Characters in Number",
         NEXT_CODE++);

   public static final LogMessageID ERROR_NUMBER_FORMAT = new LogMessageID(
         "Number Format",
         NEXT_CODE++);

   public static final LogMessageID ERROR_INVALID_IDENTIFIER_START = new LogMessageID(
         "Invalid Identifier Start",
         NEXT_CODE++);
   
   // ===== Instance
   
   public final String name;
   public final int id;
   
   private LogMessageID(String name, int id)
   {
      this.name = name;
      this.id = id;
      
      assert(ids.get(id) == null);
      ids.put(id, this);
   }
}
