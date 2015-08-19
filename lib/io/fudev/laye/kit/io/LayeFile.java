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
class LayeFile
   extends LayeObject
{
   public static final String DEFAULT_MODE = "r+";
   
   public static final LayeTypeDef TYPEDEF_FILE = new LayeTypeDef()
   {
      @Override
      public LayeObject instantiate(LayeVM vm, String ctorName, LayeObject... args)
      {
         try
         {
            if (ctorName == null)
            {
               return(LayeFile__ctor__default(vm, args));
            }
            else
            {
               switch(ctorName)
               {
                  default: break;
               }
            }
         }
         catch (Exception e)
         {
            throw new LayeException(vm, e.getMessage());
         }
         throw new LayeException(vm, "ctor '%s' does not exist.", ctorName);
      }
   };
   
   private static LayeFile LayeFile__ctor__default(LayeVM vm, LayeObject... args) throws IOException
   {
      String filePath = "";
      String mode = DEFAULT_MODE;
      Charset charset = Charset.defaultCharset();
      
      switch (args.length)
      {
         case 0: break;
         default:
         case 3: charset = Charset.forName(args[2].checkString(vm));
         case 2: mode = args[1].checkString(vm);
         case 1: filePath = args[0].checkString(vm);
                 break;
      }
      
      return(new LayeFile(vm, new File(filePath), mode, charset));
   }
   
   static
   {
      TYPEDEF_FILE.addMethod("read", new LayeFunction((vm, thisObject, args) ->
      {
         try
         {
            if (args.length >= 1)
            {
               int count = args[0].checkInt(vm);
               LayeFile f = (LayeFile)thisObject;
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
            return(LayeInt.valueOf(((LayeFile)thisObject).read()));
         }
         catch (Exception e)
         {
            throw new LayeException(vm, e.getMessage());
         }
      }));
      TYPEDEF_FILE.addMethod("readLine", new LayeFunction((vm, thisObject, args) ->
      {
         try
         {
            String line = ((LayeFile)thisObject).readLine();
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
      TYPEDEF_FILE.addMethod("readLines", new LayeFunction((vm, thisObject, args) ->
      {
         try
         {
            LayeFile file = (LayeFile)thisObject;
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
      TYPEDEF_FILE.addMethod("write", new LayeFunction((vm, thisObject, args) ->
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
               ((LayeFile)thisObject).write(value);
            }
            else if (args[0].isList(vm))
            {
               List<LayeObject> byteList = args[0].checkList(vm);
               byte[] bytes = new byte[byteList.size()];
               for (int i = 0; i < bytes.length; i++)
               {
                  bytes[i] = (byte)(byteList.get(i).checkInt(vm));
               }
               ((LayeFile)thisObject).write(bytes);
            }
            else
            {
               int value = args[0].checkInt(vm);
               ((LayeFile)thisObject).write(value);
            }
            return(NULL);
         }
         catch (Exception e)
         {
            throw new LayeException(vm, e.getMessage());
         }
      }));
      TYPEDEF_FILE.addMethod("close", new LayeFunction((vm, thisObject, args) ->
      {
         try
         {
            ((LayeFile)thisObject).close();
            return(NULL);
         }
         catch (Exception e)
         {
            throw new LayeException(vm, e.getMessage());
         }
      }));
   }
   
   private final boolean reading;
   private final boolean writing;
   
   private final boolean isBinary;
   private final boolean append;
   
   private final File file;
   private final Charset charset;
   
   private InputStream input = null;
   private Reader inputReader = null;
   
   private OutputStream output = null;
   private Writer outputWriter = null;
   
   public LayeFile(LayeVM vm, File file, String mode, Charset charset) throws IOException
   {
      super(TYPEDEF_FILE);
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
   
   @Override
   public String toString()
   {
      return("File(" + file.getPath() + ")");
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
