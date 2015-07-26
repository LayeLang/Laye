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

import io.ylf.laye.file.ScriptFile;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * @author Sekai Kyoretsuna
 */
public @EqualsAndHashCode
class Location
{
   /**
    * The file.
    */
   public final ScriptFile file;
   
   /**
    * The line in the file, 1-based.
    */
   public final int line;
   
   /**
    * The column in the file, 1-based.
    */
   public final int column;
   
   public Location(@NonNull ScriptFile file, int line, int column)
   {
      this.file = file;
      this.line = line;
      this.column = column;
   }
   
   @Override
   public String toString()
   {
      StringBuilder result = new StringBuilder();
      
      result.append(file.path);
      if (line >= 0)
      {
         result.append(" line ").append(line);
      }
      if (column >= 0)
      {
         result.append(" column ").append(column);
      }
      
      return(result.toString());
   }
}
