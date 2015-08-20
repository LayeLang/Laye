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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import io.fudev.laye.LayeException;
import io.fudev.laye.vm.LayeFunction;
import io.fudev.laye.vm.LayeObject;
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
      switch (args.length)
      {
         case 0: return(new LayeFile(vm, new File("")));
         default:
         case 2: return(new LayeFile(vm, new File(args[0].checkString(vm),
               args[1].checkString(vm))));
         case 1: return(new LayeFile(vm, new File(args[0].checkString(vm))));
      }
   }
   
   private final File file;
   
   public LayeFile(LayeVM vm, File file) throws IOException
   {
      super(TYPEDEF_FILE);
      this.file = file;
      
      setField(vm, "open", new LayeFunction(this::open));
   }
   
   public LayeFileStream open(LayeVM vm, LayeObject thisObject, LayeObject... args)
   {
      String mode = DEFAULT_MODE;
      Charset charset = Charset.defaultCharset();
      
      switch (args.length)
      {
         case 0: break;
         default:
         case 2: charset = Charset.forName(args[1].checkString(vm));
         case 1: mode = args[0].checkString(vm);
                 break;
      }
      
      return(new LayeFileStream(vm, file, mode, charset));
   }
   
   @Override
   public String toString()
   {
      return("File(" + file.getPath() + ")");
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((file == null) ? 0 : file.hashCode());
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
      if (!(obj instanceof LayeFile))
      {
         return false;
      }
      LayeFile other = (LayeFile) obj;
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
      return true;
   }
}
