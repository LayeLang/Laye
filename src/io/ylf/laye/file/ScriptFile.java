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
package io.ylf.laye.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.EqualsAndHashCode;

/**
 * @author Sekai Kyoretsuna
 */
public @EqualsAndHashCode
class ScriptFile
{
   public static ScriptFile fromResource(String resourcePath)
   {
      return(new ScriptFile(resourcePath, true));
   }

   public static ScriptFile fromFile(String filePath)
   {
      return(new ScriptFile(filePath, false));
   }
   
   public final String path;
   // TODO(sekai): enums? I don't like having a bool here.
   private final boolean isResource;
   
   private ScriptFile(String path, boolean isResource)
   {
      this.path = path;
      this.isResource = isResource;
   }
   
   public InputStream read() throws IOException
   {
      if (isResource)
      {
         return(ScriptFile.class.getResourceAsStream(path));
      }
      return(new FileInputStream(new File(path)));
   }
}
