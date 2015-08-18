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
package io.fudev.laye.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @author Sekai Kyoretsuna
 */
public
class ScriptFile
{
   /**
    * Creates a {@code ScriptFile} using this platform's default {@link Charset}.
    * The file is obtained through {@link Class#getResourceAsStream(String)}.
    * 
    * @param resourcePath
    *    The path to the resource file
    * @return
    *    A {@code ScriptFile} that can read the resource with the default {@code Charset}
    */
   public static ScriptFile fromResource(String resourcePath)
   {
      return(new ScriptFile(resourcePath, true, Charset.defaultCharset()));
   }

   /**
    * Creates a {@code ScriptFile} using the {@link Charset} obtained by
    * {@link Charset#forName(String)} when passed the {@code encodingName}.
    * The file is obtained through {@link Class#getResourceAsStream(String)}.
    * 
    * @param resourcePath
    *    The path to the resource file
    * @param encodingName
    *    The name of the {@code Charset} to use when reading this file
    * @return
    *    A {@code ScriptFile} that can read the resource with the acquired {@code Charset}
    */
   public static ScriptFile fromResource(String resourcePath, String encodingName)
   {
      return(new ScriptFile(resourcePath, true, Charset.forName(encodingName)));
   }

   /**
    * Creates a {@code ScriptFile} using the given {@link Charset}.
    * The file is obtained through {@link Class#getResourceAsStream(String)}.
    * 
    * @param resourcePath
    *    The path to the resource file
    * @param encoding
    *    The {@code Charset} to use when reading this file
    * @return
    *    A {@code ScriptFile} that can read the resource with the given {@code Charset}
    */
   public static ScriptFile fromResource(String resourcePath, Charset encoding)
   {
      return(new ScriptFile(resourcePath, true, encoding));
   }

   public static ScriptFile fromFile(String filePath)
   {
      return(new ScriptFile(filePath, false, Charset.defaultCharset()));
   }

   public static ScriptFile fromFile(String filePath, String encodingName)
   {
      return(new ScriptFile(filePath, false, Charset.forName(encodingName)));
   }

   public static ScriptFile fromFile(String filePath, Charset encoding)
   {
      return(new ScriptFile(filePath, false, encoding));
   }
   
   /**
    * The path to the file.
    */ // TODO(kai): more information.
   public final String path;
   // TODO(kai): enums? I don't like having a bool here.
   private final boolean isResource;
   
   /**
    * The Charset to use when reading the file input.
    */
   public final Charset encoding;
   
   public ScriptFile(String path, boolean isResource, Charset encoding)
   {
      this.path = path;
      this.isResource = isResource;
      this.encoding = encoding;
   }

   public InputStreamReader read() throws IOException
   {
      if (isResource)
      {
         return(new InputStreamReader(ScriptFile.class.getResourceAsStream(path), encoding));
      }
      return(new InputStreamReader(new FileInputStream(new File(path)), encoding));
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((encoding == null) ? 0 : encoding.hashCode());
      result = prime * result + (isResource ? 1231 : 1237);
      result = prime * result + ((path == null) ? 0 : path.hashCode());
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
      if (!(obj instanceof ScriptFile))
      {
         return false;
      }
      ScriptFile other = (ScriptFile) obj;
      if (encoding == null)
      {
         if (other.encoding != null)
         {
            return false;
         }
      }
      else if (!encoding.equals(other.encoding))
      {
         return false;
      }
      if (isResource != other.isResource)
      {
         return false;
      }
      if (path == null)
      {
         if (other.path != null)
         {
            return false;
         }
      }
      else if (!path.equals(other.path))
      {
         return false;
      }
      return true;
   }
}
