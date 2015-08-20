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
package io.fudev.laye.kit.io;

import java.io.*;
import java.nio.charset.Charset;

import io.fudev.collections.List;
import io.fudev.laye.LayeException;
import io.fudev.laye.vm.LayeFunction;
import io.fudev.laye.vm.LayeInt;
import io.fudev.laye.vm.LayeList;
import io.fudev.laye.vm.LayeObject;
import io.fudev.laye.vm.LayeString;
import io.fudev.laye.vm.LayeTypeDef;
import io.fudev.laye.vm.LayeVM;

/**
 * @author Sekai Kyoretsuna
 */
public
class LayeFileStream
   extends LayeObject
{
   private static final LayeTypeDef TYPEDEF_FILESTREAM = new LayeTypeDef();
   
   static
   {
      TYPEDEF_FILESTREAM.addMethod("read", new LayeFunction((vm, thisObject, args) ->
      {
         try
         {
            if (args.length >= 1)
            {
               int count = args[0].checkInt(vm);
               LayeFileStream f = (LayeFileStream)thisObject;
               if (f.isBinary)
               {
                  byte[] bytes = new byte[count];
                  f.read(bytes);
                  LayeList result = new LayeList(bytes.length);
                  for (byte b : bytes)
                  {
                     result.append(LayeInt.valueOf(b & 0xFFL));
                  }
                  return(result);
               }
               else
               {
                  char[] chars = new char[count];
                  f.read(chars);
                  return(new LayeString(new String(chars)));
               }
            }
            return(LayeInt.valueOf(((LayeFileStream)thisObject).read()));
         }
         catch (Exception e)
         {
            throw new LayeException(vm, e.getMessage());
         }
      }));
      TYPEDEF_FILESTREAM.addMethod("readLine", new LayeFunction((vm, thisObject, args) ->
      {
         try
         {
            String line = ((LayeFileStream)thisObject).readLine();
            if (line == null)
            {
               return(NULL);
            }
            return(new LayeString(line));
         }
         catch (Exception e)
         {
            throw new LayeException(vm, e.getMessage());
         }
      }));
      TYPEDEF_FILESTREAM.addMethod("readLines", new LayeFunction((vm, thisObject, args) ->
      {
         try
         {
            LayeFileStream file = (LayeFileStream)thisObject;
            LayeList result = new LayeList();
            String value;
            while ((value = file.readLine()) != null)
            {
               result.append(new LayeString(value));
            }
            return(result);
         }
         catch (Exception e)
         {
            throw new LayeException(vm, e.getMessage());
         }
      }));
      TYPEDEF_FILESTREAM.addMethod("write", new LayeFunction((vm, thisObject, args) ->
      {
         if (args.length == 0)
         {
            throw new LayeException(vm, "expected value to write.");
         }
         try
         {
            if (args[0].isString(vm))
            {
               String value = args[0].checkString(vm);
               ((LayeFileStream)thisObject).write(value);
            }
            else if (args[0].isList(vm))
            {
               List<LayeObject> byteList = args[0].checkList(vm);
               byte[] bytes = new byte[byteList.size()];
               for (int i = 0; i < bytes.length; i++)
               {
                  bytes[i] = (byte)(byteList.get(i).checkInt(vm));
               }
               ((LayeFileStream)thisObject).write(bytes);
            }
            else
            {
               int value = args[0].checkInt(vm);
               ((LayeFileStream)thisObject).write(value);
            }
            return(NULL);
         }
         catch (Exception e)
         {
            throw new LayeException(vm, e.getMessage());
         }
      }));
      TYPEDEF_FILESTREAM.addMethod("close", new LayeFunction((vm, thisObject, args) ->
      {
         try
         {
            ((LayeFileStream)thisObject).close();
            return(NULL);
         }
         catch (Exception e)
         {
            throw new LayeException(vm, e.getMessage());
         }
      }));
   }
   
   private final File file;
   
   private final boolean reading;
   private final boolean writing;
   
   private final boolean isBinary;
   private final boolean append;
   
   private final Charset charset;
   
   private InputStream input = null;
   private Reader inputReader = null;
   
   private OutputStream output = null;
   private Writer outputWriter = null;
   
   public LayeFileStream(LayeVM vm, File file, String mode, Charset charset)
   {
      super(TYPEDEF_FILESTREAM);
      
      if (file.isDirectory())
      {
         throw new IllegalArgumentException("cannot open streams to a directory.");
      }
      
      this.file = file;
      this.charset = charset;
      
      if (!mode.matches("(r|w|a)b?\\+?"))
      {
         throw new LayeException(vm, "unrecognized open mode '%s'", mode);
      }
      
      isBinary = mode.contains("b");
      append = mode.contains("a");
      
      reading = mode.contains("r") || mode.contains("+");
      writing = mode.contains("w") || mode.contains("+") || append;
   }
   
   @Override
   public String toString()
   {
      return("FileStream(" + file.getPath() + ")");
   }
   
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + (append ? 1231 : 1237);
      result = prime * result + ((charset == null) ? 0 : charset.hashCode());
      result = prime * result + ((file == null) ? 0 : file.hashCode());
      result = prime * result + (isBinary ? 1231 : 1237);
      result = prime * result + (reading ? 1231 : 1237);
      result = prime * result + (writing ? 1231 : 1237);
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (!super.equals(obj))
      {
         return false;
      }
      if (!(obj instanceof LayeFileStream))
      {
         return false;
      }
      LayeFileStream other = (LayeFileStream) obj;
      if (append != other.append)
      {
         return false;
      }
      if (charset == null)
      {
         if (other.charset != null)
         {
            return false;
         }
      }
      else if (!charset.equals(other.charset))
      {
         return false;
      }
      if (file == null)
      {
         if (other.file != null)
         {
            return false;
         }
      }
      else if (!file.equals(other.file))
      {
         return false;
      }
      if (isBinary != other.isBinary)
      {
         return false;
      }
      if (reading != other.reading)
      {
         return false;
      }
      if (writing != other.writing)
      {
         return false;
      }
      return true;
   }

   private void openInput() throws IOException
   {
      if (input != null)
      {
         return; // already open
      }
      
      close(); // the current ones
      
      input = new FileInputStream(file);
      inputReader = new InputStreamReader(input, charset);
   }
   
   private void openOutput() throws IOException
   {
      if (outputWriter != null)
      {
         return; // already open
      }
      
      close(); // the current ones
      
      output = new FileOutputStream(file, append);
      outputWriter = new OutputStreamWriter(output, charset);
   }
   
   public synchronized void close() throws IOException
   {
      if (input != null)
      {
         input.close();
         input = null;
         inputReader = null;
      }
      
      if (output != null)
      {
         output.flush();
         output.close();
         output = null;
         outputWriter = null;
      }
   }
   
   public synchronized int read() throws IOException
   {
      if (!reading)
      {
         throw new IllegalStateException("this file is not available for reading.");
      }
      openInput();
      return(isBinary ? input.read() : inputReader.read());
   }
   
   public synchronized int read(byte[] buf) throws IOException
   {
      if (!reading)
      {
         throw new IllegalStateException("this file is not available for reading.");
      }
      if (!isBinary)
      {
         throw new IllegalStateException("Cannot read bytes from a non-binary file.");
      }
      openInput();
      return(input.read(buf));
   }
   
   public synchronized int read(char[] buf) throws IOException
   {
      if (!reading)
      {
         throw new IllegalStateException("this file is not available for reading.");
      }
      if (isBinary)
      {
         throw new IllegalStateException("Cannot read chars from a binary file.");
      }
      openInput();
      return(inputReader.read(buf));
   }
   
   public synchronized String readLine() throws IOException
   {
      if (!reading)
      {
         throw new IllegalStateException("this file is not available for reading.");
      }
      if (isBinary)
      {
         throw new IllegalStateException("Cannot read a line from a binary file.");
      }
      openInput();
      int c = inputReader.read();
      if (c != -1)
      {
         StringBuilder builder = new StringBuilder().appendCodePoint(c);
         while ((c = inputReader.read()) != -1 && c != '\n')
         {
            if (c == '\r')
            {
               continue;
            }
            builder.appendCodePoint(c);
         }
         return(builder.toString());
      }
      return(null);
   }
   
   public synchronized void write(int value) throws IOException
   {
      if (!writing)
      {
         throw new IllegalStateException("this file is not available for writing.");
      }
      openOutput();
      if (isBinary)
      {
         output.write(value);
      }
      else
      {
         outputWriter.write(value);
      }
   }
   
   public synchronized void write(byte[] bytes) throws IOException
   {
      if (!writing)
      {
         throw new IllegalStateException("this file is not available for writing.");
      }
      if (!isBinary)
      {
         throw new IllegalStateException("Cannot write bytes to a non-binary file.");
      }
      openOutput();
      output.write(bytes);
   }
   
   public synchronized void write(String value) throws IOException
   {
      if (!writing)
      {
         throw new IllegalStateException("this file is not available for writing.");
      }
      if (isBinary)
      {
         throw new IllegalStateException("Cannot write a string to a binary file.");
      }
      openOutput();
      outputWriter.write(value);
      outputWriter.flush();
   }
}
